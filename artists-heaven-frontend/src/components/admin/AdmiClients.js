import React, { useState, useEffect } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import NonAuthorise from '../NonAuthorise';
import { faUser, faMusic } from '@fortawesome/free-solid-svg-icons';
const AdminClient = () => {
    const currentYear = new Date().getFullYear();
    const [year, setYear] = useState(currentYear);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const role = localStorage.getItem("role");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 6;

    const [searchTerm, setSearchTerm] = useState("");

    const [data, setData] = useState({
        numUsers: 0,
        numArtists: 0,
        userDetails: { content: [] }
    });

    const [verifications, setVerifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [users, setUsers] = useState([]);


    useEffect(() => {
        if (!authToken || role !== 'ADMIN') return;

        const controller = new AbortController();

        const fetchData = async () => {
            const delayDebounce = setTimeout(async () => {
                try {
                    const query = searchTerm ? `&search=${encodeURIComponent(searchTerm)}` : "";
                    const [staticsRes, usersRes] = await Promise.all([
                        fetch(`/api/admin/staticsPerYear?year=${year}`, {
                            method: "GET",
                            headers: { 'Authorization': `Bearer ${authToken}` },
                            signal: controller.signal
                        }),
                        fetch(`/api/admin/users?page=${page}&size=${pageSize}${query}`, {
                            method: "GET",
                            headers: { 'Authorization': `Bearer ${authToken}` },
                            signal: controller.signal
                        })
                    ]);

                    if (!staticsRes.ok || !usersRes.ok) throw new Error('Error en una de las peticiones');

                    const staticsData = await staticsRes.json();
                    const usersData = await usersRes.json();

                    setData(prev => ({
                        ...prev,
                        ...staticsData,
                        incomePerYear: staticsData.incomePerYear ?? 0,
                        userDetails: usersData,
                    }));

                    setTotalPages(usersData.totalPages);
                } catch (error) {
                    if (error.name !== 'AbortError') {
                        console.error(error);
                    }
                }
            }, 400);

            return () => {
                clearTimeout(delayDebounce);
            };
        };


        fetchData();
        return () => {
            controller.abort();
        };
    }, [authToken, page, searchTerm]);

    useEffect(() => {
        if (role !== "ADMIN") {
            // Si el rol no es ADMIN, no hacemos la petición
            return;
        }
        const fetchVerifications = async () => {
            try {
                const response = await fetch("/api/admin/verification/pending", {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        'Authorization': `Bearer ${authToken}`
                    },
                });
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                const data = await response.json();
                setVerifications(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchVerifications();
    }, [authToken]);

    const fetchVideoBlob = async (videoUrl, authToken) => {
        try {
            if (role !== "ADMIN") {
                return;
            }
            const response = await fetch(`/api/admin${videoUrl}`, {
                method: "GET",
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch video: ${response.status}`);
            }

            const videoBlob = await response.blob();
            return URL.createObjectURL(videoBlob);
        } catch (error) {
            console.error("Error fetching video blob:", error);
            return null;
        }
    };

    const VideoLink = ({ videoUrl, authToken }) => {
        const [videoSrc, setVideoSrc] = useState(null);

        useEffect(() => {
            const fetchAndSetVideo = async () => {
                const videoBlobUrl = await fetchVideoBlob(videoUrl, authToken);
                setVideoSrc(videoBlobUrl);
            };

            fetchAndSetVideo();
        }, [videoUrl, authToken]);

        return (
            <div>
                {videoSrc ? (
                    <a href={videoSrc} target="_blank" rel="noopener noreferrer">
                        Open Video
                    </a>
                ) : (
                    <p>Loading video...</p>
                )}
            </div>
        )
    }

    const verifyArtist = (id, verificationId) => {
        if (role !== "ADMIN") {
            // Si el rol no es ADMIN, no hacemos la petición
            return;
        }
        fetch('/api/admin/validate_artist', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                id,
                verificationId
            }),
        })
            .then(response => {
                if (response.ok) {
                    setUsers(users.map(user =>
                        user.id === id ? { ...user, role: "ARTIST" } : user
                    ));
                    console.log("Artitsa verificado")
                    window.location.reload()
                } else {
                    console.log('Error al verificar al artista');
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    };

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [page]);

    const nextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    const prevPage = () => {
        if (page > 0) setPage(page - 1);
    };

    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
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

    console.log(data.userDetails);

    if (role !== 'ADMIN') {
        return <NonAuthorise />;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
            <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-4">
                {/*COLUMNA 1 */}
                <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
                    <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left mb-6">
                        Gestión de Clientes
                    </p>
                    <input
                        type="text"
                        placeholder="Buscar usuario..."
                        value={searchTerm}
                        onChange={handleSearchChange}
                        className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                    />

                    <div className="flex flex-col gap-6">
                        {Array.isArray(data.userDetails?.content) && data.userDetails.content.length > 0 ? (
                            data.userDetails.content.map((user) => (
                                <div key={user.id} className="bg-gray-50 rounded-lg shadow p-4 hover:shadow-md transition duration-300 border border-gray-200">
                                    <div className="flex items-center space-x-2">
                                        <p className="text-lg font-semibold text-gray-800">{user.firstName} {user.lastName}</p>
                                        <p className="text-sm text-gray-500">@{user.username}</p>
                                    </div>
                                    <div className="text-sm text-gray-600 space-y-1">
                                        <p><span className="font-medium text-gray-700">Email:</span> {user.email || 'N/A'}</p>
                                        <p><span className="font-medium text-gray-700">Ciudad:</span> {user.city || 'N/A'}</p>
                                        <p><span className="font-medium text-gray-700">Dirección:</span> {user.address || 'N/A'}</p>
                                        <p><span className="font-medium text-gray-700">Rol:</span>
                                            <span className={`ml-1 font-semibold px-2 py-1 rounded text-xs 
                                ${user.role === 'ADMIN' ? 'bg-red-100 text-red-600' : 'bg-blue-100 text-blue-600'}`}>
                                                {user.role}
                                            </span>
                                        </p>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-gray-500 col-span-full text-center">No hay usuarios disponibles.</p>
                        )}
                    </div>
                    <div className="flex flex-col sm:flex-row justify-center items-center mt-4 gap-2 sm:gap-4">
                        <button onClick={prevPage} disabled={page === 0} className="w-full sm:w-auto px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                            Anterior
                        </button>
                        <span className="font-semibold text-gray-700">Página {page + 1} de {totalPages}</span>
                        <button onClick={nextPage} disabled={page >= totalPages - 1} className="w-full sm:w-auto px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                            Siguiente
                        </button>
                    </div>
                </div>

                {/*COLUMNA 2 */}
                <div className="w-full">
                    <div className="bg-white p-4 rounded-lg mb-4 w-full">
                        <div className="flex flex-col md:flex-row justify-between gap-4">
                            <div className="w-full md:w-1/2">
                                <MetricCard
                                    icon={faUser}
                                    value={data.numUsers - data.numArtists}
                                    title="Usuarios Registrados"
                                    iconColor="text-orange-600"
                                    bgColor="bg-orange-300"
                                />
                            </div>
                            <div className="w-full md:w-1/2">
                                <MetricCard
                                    icon={faMusic}
                                    value={data.numArtists}
                                    title="Artistas Registrados"
                                    iconColor="text-yellow-600"
                                    bgColor="bg-yellow-300"
                                />
                            </div>
                        </div>
                    </div>
                    <div className="bg-white p-4 rounded-lg mb-4 w-full overflow-x-auto">
                    <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left mb-6">
                            Verificaciones Pendientes
                        </p>

                        {verifications.length > 0 ? (
                            <table className="min-w-full divide-y divide-gray-200 text-sm">
                                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                                    <tr>
                                        <th className="px-4 py-3 text-left">ID</th>
                                        <th className="px-4 py-3 text-left">Artista</th>
                                        <th className="px-4 py-3 text-left">Video</th>
                                        <th className="px-4 py-3 text-left">Fecha</th>
                                        <th className="px-4 py-3 text-left">Estado</th>
                                        <th className="px-4 py-3 text-left">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                    {verifications.map((verification) => (
                                        <tr key={verification.id} className="hover:bg-gray-50">
                                            <td className="px-4 py-3">{verification.id}</td>
                                            <td className="px-4 py-3 font-medium text-gray-800">
                                                {verification.artist?.artistName || "Desconocido"}
                                            </td>
                                            <td className="px-4 py-3">
                                                <VideoLink videoUrl={verification.videoUrl} authToken={authToken} />
                                            </td>
                                            <td className="px-4 py-3">
                                                {new Date(verification.date).toLocaleString()}
                                            </td>
                                            <td className="px-4 py-3">
                                                <span className={`px-2 py-1 rounded text-xs font-semibold
                                ${verification.status === 'PENDING'
                                                        ? 'bg-yellow-100 text-yellow-700'
                                                        : 'bg-green-100 text-green-700'}`}>
                                                    {verification.status}
                                                </span>
                                            </td>
                                            <td className="px-4 py-3">
                                                {!verification.artist?.isvalid ? (
                                                    <button
                                                        onClick={() => verifyArtist(verification.artist.id, verification.id)}
                                                        className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 transition"
                                                    >
                                                        Verificar
                                                    </button>
                                                ) : (
                                                    <span className="text-gray-500 italic text-sm">Ya verificado</span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p className="text-gray-500 text-sm text-center">No hay verificaciones pendientes.</p>
                        )}
                    </div>
                    <div>USUARIOS PENALIZADOS</div>
                </div>
            </div>

        </div>

    );

}

export default AdminClient;