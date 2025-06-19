import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import Footer from '../Footer';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendar, faTrash, faEdit } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';
import NonAuthorise from '../NonAuthorise';

const AllMyEvents = () => {
    const [events, setEvents] = useState([]);
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

     const [authToken] = useState(localStorage.getItem("authToken"));

    useEffect(() => {
        setPage(0); // reset page on searchTerm change
    }, [searchTerm]);

    useEffect(() => {
        if (!authToken || role !== 'ARTIST') return;

        const controller = new AbortController();

        const fetchEvents = async () => {
            setLoading(true);
            try {
                const query = searchTerm ? `&search=${encodeURIComponent(searchTerm)}` : "";
                const response = await fetch(`/api/event/allMyEvents?page=${page}&size=${pageSize}${query}`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                    signal: controller.signal,
                });

                if (!response.ok) throw new Error('Error al obtener los eventos');

                const data = await response.json();
                setEvents(data.content || []);
                setTotalPages(data.totalPages || 1);
                const now = new Date();

                const upcomingEvents = data.content.filter(event => new Date(event.date) > now).length;
                const pastEvents = data.content.filter(event => new Date(event.date) <= now).length;

                setMetrics({
                    totalEvents: data.totalElements || 0,
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

        return () => {
            controller.abort();
        };
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

    if (role !== 'ARTIST') {
        return <NonAuthorise />;
    }

    console.log(role)

    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-6">
                    {/* COLUMNA 1: Lista y búsqueda */}
                    <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
                        <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                            <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                Gestión de Eventos
                            </p>
                            <Link to="/event/new" className="w-full md:w-auto">
                                <button
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    Crear nuevo evento
                                </button>
                            </Link>
                        </div>

                        <input
                            type="text"
                            placeholder="Buscar evento..."
                            value={searchTerm}
                            onChange={handleSearchChange}
                            className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                        />

                        {loading ? (
                            <div className="text-center py-10 text-gray-500">Cargando eventos...</div>
                        ) : errorMessage ? (
                            <div className="text-center py-10 text-red-600 font-semibold">{errorMessage}</div>
                        ) : events.length === 0 ? (
                            <div className="text-center py-10 text-gray-600">No se encontraron eventos.</div>
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
                                        <p className="text-xs text-gray-500 mb-1"><strong>Ubicación:</strong> {event.location}</p>
                                        <p className="text-xs text-gray-500 mb-3 truncate">{event.moreInfo}</p>
                                        <div className="mt-auto flex space-x-3">
                                            <button
                                                onClick={() => handleDelete(event.id)}
                                                className="flex-1 bg-red-500 hover:bg-red-600 text-white py-2 px-4 rounded-lg text-sm font-semibold flex items-center justify-center gap-2"
                                            >
                                                <FontAwesomeIcon icon={faTrash} />
                                                Eliminar
                                            </button>
                                            <button
                                                onClick={() => navigate(`/event/edit/${event.id}`)}
                                                className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white py-2 px-4 rounded-lg text-sm font-semibold flex items-center justify-center gap-2"
                                            >
                                                <FontAwesomeIcon icon={faEdit} />
                                                Editar
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
                                Anterior
                            </button>
                            <span className="font-semibold text-gray-700">
                                Página {page + 1} de {totalPages}
                            </span>
                            <button
                                onClick={nextPage}
                                disabled={page >= totalPages - 1}
                                className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50 hover:bg-gray-400"
                            >
                                Siguiente
                            </button>
                        </div>
                    </div>

                    {/* COLUMNA 2 */}
                    <div className="w-full">
                        <div className="bg-gray-50 p-4 rounded-lg mb-4 flex justify-around">
                            <div className="flex flex-col sm:flex-row flex-wrap ">
                                <MetricCard icon={faCalendar} value={metrics.totalEvents} title="Eventos Totales" iconColor="text-blue-600" bgColor="bg-blue-300" />
                                <MetricCard icon={faCalendar} value={metrics.upcomingEvents} title="Eventos Por Realizar" iconColor="text-green-600" bgColor="bg-green-300" />
                                <MetricCard icon={faCalendar} value={metrics.pastEvents} title="Eventos Pasados" iconColor="text-red-600" bgColor="bg-red-300" />
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
