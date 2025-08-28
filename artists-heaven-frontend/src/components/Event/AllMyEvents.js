import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import Footer from '../Footer';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendar, faTrash, faEdit } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';
import NonAuthorise from '../NonAuthorise';
import { checkTokenExpiration } from '../../utils/authUtils';
import SessionExpired from '../SessionExpired';
import { useTranslation } from "react-i18next";

const AllMyEvents = () => {
    const [events, setEvents] = useState([]);
    const [isVerified, setIsVerified] = useState(null);
    const [metrics, setMetrics] = useState({
        totalEvents: 0,
        upcomingEvents: 0,
        pastEvents: 0
    });
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();
    const token = localStorage.getItem("authToken");
    const role = localStorage.getItem("role");
    const pageSize = 6;
    const { t } = useTranslation();

    useEffect(() => {
        setPage(0); // reset page on searchTerm change
    }, [searchTerm]);



    useEffect(() => {
        if (!token) {
            setIsVerified(false);
            setLoading(false);
            return;
        }
        if (!checkTokenExpiration || role !== 'ARTIST') {
            setIsVerified(false);
            return;
        }


        const fetchVerificationStatus = async () => {

            try {
                const response = await fetch('/api/event/isVerified', {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                if (response.ok) {
                    const dataVerification = await response.json();
                    setIsVerified(dataVerification.data);
                } else {
                    setIsVerified(false);
                }
            } catch (error) {
                console.error("Error fetching verification status", error);
                setIsVerified(false);
            }
        };

        const fetchEvents = async () => {
            setLoading(true);
            try {
                const query = searchTerm ? `&search=${encodeURIComponent(searchTerm)}` : "";
                const response = await fetch(`/api/event/allMyEvents?page=${page}&size=${pageSize}${query}`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });

                if (!response.ok) throw new Error('Error al obtener los eventos');

                const events = await response.json();
                setEvents(events.data.content || []);
                setTotalPages(events.data.totalPages || 1);
                const now = new Date();

                const upcomingEvents = events.data.content.filter(event => new Date(event.date) > now).length;
                const pastEvents = events.data.content.filter(event => new Date(event.date) <= now).length;

                setMetrics({
                    totalEvents: events.data.totalElements || 0,
                    upcomingEvents,
                    pastEvents
                });
                setErrorMessage("");
            } catch (error) {
                if (error.name !== "AbortError") {
                    setErrorMessage(error.message);
                    console.error(error);
                }
            } finally {
                setLoading(false);
            }
        };

        fetchEvents();
        fetchVerificationStatus();

    }, [page, searchTerm, token, role]);

    const handleDelete = async (id) => {
        if (!window.confirm("¿Estás seguro de que quieres eliminar este evento?")) return;
        try {
            const response = await fetch(`/api/event/delete/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!response.ok) throw new Error('Error al eliminar el evento');

            setEvents(events.filter(event => event.id !== id));
            setMetrics(prev => ({ ...prev, totalEvents: prev.totalEvents - 1 }));
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };

    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
    };

    const nextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    const prevPage = () => {
        if (page > 0) setPage(page - 1);
    };

    const MetricCard = React.memo(({ icon, value, title, iconColor, bgColor }) => {
        return (
            <div className="flex-1 w-auto bg-white shadow-lg rounded-lg p-4 m-2 flex items-center">
                <div className={`flex items-center justify-center mr-4 w-12 h-12 rounded-full ${bgColor}`}>
                    <FontAwesomeIcon icon={icon} className={`${iconColor} text-xl`} />
                </div>
                <div>
                    <p className="text-3xl font-bold text-indigo-600 truncate">{value}</p>
                    <p className="text-sm font-semibold text-gray-400 truncate">{title}</p>
                </div>
            </div>
        );
    });

    console.log(isVerified)
    console.log(loading)

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 border-solid mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">{t('allMyEvents.verifiedAccess')}</p>
                </div>
            </div>
        );
    }

    if (isVerified === null) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 border-solid mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">{t('allMyEvents.verifiedAccess')}</p>
                </div>
            </div>
        );
    }

    // Si el rol no es "ARTIST" o no está verificado
    if (!role || role !== "ARTIST" || !isVerified) {
        return <NonAuthorise />;
    }

    // Si la sesión ha expirado
    if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }



    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-6">
                    {/* COLUMNA 1: Lista y búsqueda */}
                    <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
                        <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                            <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                {t('allMyEvents.eventsManagement')}
                            </p>
                            <Link to="/event/new" className="w-full md:w-auto">
                                <button
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    {t('allMyEvents.newEvent')}
                                </button>
                            </Link>
                        </div>

                        <input
                            type="text"
                            placeholder={t('allMyEvents.placeholder')}
                            value={searchTerm}
                            onChange={handleSearchChange}
                            className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                        />

                        {loading ? (
                            <div className="text-center py-10 text-gray-500">{t('allMyEvents.loadEvents')}</div>
                        ) : errorMessage ? (
                            <div className="text-center py-10 text-red-600 font-semibold">{errorMessage}</div>
                        ) : events.length === 0 ? (
                            <div className="text-center py-10 text-gray-600">{t('allMyEvents.notEventsAvailable')}</div>
                        ) : (
                            <ul className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                                {events.map(event => (
                                    <li key={event.id} className="bg-white rounded-lg shadow p-4 flex flex-col">
                                        <img
                                            src={`/api/event${event.image}`}
                                            alt={event.name}
                                            className="w-full h-40 object-cover rounded-md mb-3"
                                            loading="lazy"
                                        />
                                        <h2 className="font-semibold text-lg text-gray-800 truncate mb-1">{event.name}</h2>
                                        <p className="text-sm text-gray-600 mb-2 line-clamp-3">{event.description}</p>
                                        <p className="text-xs text-gray-500 mb-1"><strong>{t('allMyEvents.locate')}:</strong> {event.location}</p>
                                        <p className="text-xs text-gray-500 mb-3 truncate">{event.moreInfo}</p>
                                        <div className="mt-auto flex space-x-3">
                                            <button
                                                onClick={() => handleDelete(event.id)}
                                                className="flex-1 bg-red-500 hover:bg-red-600 text-white py-2 px-4 rounded-lg text-sm font-semibold flex items-center justify-center gap-2"
                                            >
                                                <FontAwesomeIcon icon={faTrash} />
                                                {t('allMyEvents.delete')}
                                            </button>
                                            <button
                                                onClick={() => navigate(`/event/edit/${event.id}`)}
                                                className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white py-2 px-4 rounded-lg text-sm font-semibold flex items-center justify-center gap-2"
                                            >
                                                <FontAwesomeIcon icon={faEdit} />
                                                {t('allMyEvents.edit')}
                                            </button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}

                        {/* Paginación */}
                        <div className="flex justify-center items-center mt-6 space-x-4">
                            <button
                                onClick={prevPage}
                                disabled={page === 0}
                                className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50 hover:bg-gray-400"
                            >
                                {t('allMyEvents.previous')}
                            </button>
                            <span className="font-semibold text-gray-700">
                                {t('allMyEvents.page')} {page + 1} {t('allMyEvents.of')} {totalPages}
                            </span>
                            <button
                                onClick={nextPage}
                                disabled={page >= totalPages - 1}
                                className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50 hover:bg-gray-400"
                            >
                                {t('allMyEvents.next')}
                            </button>
                        </div>
                    </div>

                    {/* COLUMNA 2 */}
                    <div className="w-full">
                        <div className="bg-gray-50 p-4 rounded-lg mb-4 flex justify-around">
                            <div className="flex flex-col sm:flex-row flex-wrap ">
                                <MetricCard icon={faCalendar} value={metrics.totalEvents} title={t('allMyEvents.totalEvents')} iconColor="text-blue-600" bgColor="bg-blue-300" />
                                <MetricCard icon={faCalendar} value={metrics.upcomingEvents} title={t('allMyEvents.nextEvents')} iconColor="text-green-600" bgColor="bg-green-300" />
                                <MetricCard icon={faCalendar} value={metrics.pastEvents} title={t('allMyEvents.pastEvents')} iconColor="text-red-600" bgColor="bg-red-300" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default AllMyEvents;
