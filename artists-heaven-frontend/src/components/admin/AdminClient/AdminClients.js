import React, { useState, useEffect, useCallback } from 'react';

import NonAuthorise from '../../NonAuthorise';
import { faUser, faMusic } from '@fortawesome/free-solid-svg-icons';
import {
    getStatisticsPerYear,
    getUsers,
    getPendingVerifications,
    getPendingUserProducts,
    getVerifyArtist,
    doRefuseArtist,
    approveUserProduct,
    rejectUserProduct
} from '../../../services/adminServices';

import { checkTokenExpiration } from '../../../utils/authUtils';
import SessionExpired from '../../SessionExpired';
import { useTranslation } from "react-i18next";
import { UserProductImages, MetricCard } from './UserProductImages';

const AdminClient = () => {
    const currentYear = new Date().getFullYear();
    const [authToken] = useState(localStorage.getItem("authToken"));
    const role = localStorage.getItem("role");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [searchTerm, setSearchTerm] = useState("");

    const [data, setData] = useState({ userDetails: { content: [] } });
    const [verifications, setVerifications] = useState([]);
    const [error, setError] = useState(null);

    const [userProducts, setUserProducts] = useState([]);

    const [videoBlobsCache, setVideoBlobsCache] = useState({});
    const pendingFetches = React.useRef({});
    const { t } = useTranslation();

    // Fetch statistics and users
    useEffect(() => {
        if (!checkTokenExpiration || role !== 'ADMIN') return;

        const fetchData = async () => {
            try {
                const [staticsRes, usersRes] = await Promise.all([
                    getStatisticsPerYear(authToken, currentYear),
                    getUsers(authToken, page, 6, searchTerm),
                ]);

                setData(prev => ({ ...prev, ...staticsRes.data, userDetails: usersRes }));
                setTotalPages(usersRes.totalPages);
            } catch (error) {
                setError(error.message || "Error fetching data");
            }
        };

        const delayDebounce = setTimeout(fetchData, 400);

        return () => clearTimeout(delayDebounce);
    }, [authToken, currentYear, page, searchTerm, role]);

    // Fetch pending verifications
    useEffect(() => {
        if (!checkTokenExpiration || role !== "ADMIN") return;

        const fetchVerifications = async () => {
            try {
                const verificationsData = await getPendingVerifications(authToken);
                const userProductsData = await getPendingUserProducts(authToken);
                setVerifications(verificationsData.data);
                setUserProducts(userProductsData.data)
            } catch (err) {
                setError(err.message);

            };
        }

        fetchVerifications();
    }, [authToken, role]);

    // Video blob fetch
    const fetchVideoBlob = useCallback(async (videoUrl) => {
        if (videoBlobsCache[videoUrl]) return videoBlobsCache[videoUrl];
        if (pendingFetches.current[videoUrl]) return pendingFetches.current[videoUrl];

        const fetchPromise = fetch(`http://localhost:8080/api/admin${videoUrl}`, { method: "GET", headers: { 'Authorization': `Bearer ${authToken}` } })
            .then(response => {
                if (!response.ok) throw new Error(`Failed to fetch video: ${response.status}`);
                return response.blob();
            })
            .then(videoBlob => {
                const blobUrl = URL.createObjectURL(videoBlob);
                setVideoBlobsCache(prev => ({ ...prev, [videoUrl]: blobUrl }));
                delete pendingFetches.current[videoUrl];
                return blobUrl;
            })
            .catch(error => {
                console.error("Error fetching video blob:", error);
                delete pendingFetches.current[videoUrl];
                return null;
            });

        pendingFetches.current[videoUrl] = fetchPromise;
        return fetchPromise;
    }, [authToken, videoBlobsCache]);

    // Video Link
    const VideoLink = ({ videoUrl }) => {
        const [videoSrc, setVideoSrc] = useState(null);

        useEffect(() => {
            if (!checkTokenExpiration || role !== "ADMIN") return;
            let isMounted = true;

            fetchVideoBlob(videoUrl).then(blobUrl => {
                if (isMounted) setVideoSrc(blobUrl);
            });

            return () => {
                isMounted = false;
            };
        }, [videoUrl]);

        return (
            <div>
                {videoSrc ? (
                    <a href={videoSrc} target="_blank" rel="noopener noreferrer">
                        {t('adminclient.openVideo')}
                    </a>
                ) : (
                    <p>{t('adminclient.loadVideo')}</p>
                )}
            </div>
        );
    };

    // Verify artist
    const verifyArtist = async (id, verificationId) => {
        try {
            await getVerifyArtist(authToken, id, verificationId);
            window.location.reload();
        } catch (error) {
            console.error('Error:', error);
        }
    };

    // Refuse artist
    const refuseArtist = async (verificationId) => {
        try {
            await doRefuseArtist(authToken, verificationId);
            window.location.reload();
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const nextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    const prevPage = () => {
        if (page > 0) setPage(page - 1);
    };

    const handleSearchChange = (e) => {
        setSearchTerm(e.target.value);
    };

    // Aprobar producto
    const approveProduct = async (productId) => {
        try {
            await approveUserProduct(authToken, productId);
            // Actualizar lista de productos localmente
            setUserProducts(prev => prev.filter(p => p.id !== productId));
        } catch (err) {
            console.error("Error aprobando producto:", err);
            alert("No se pudo aprobar el producto");
        }
    };

    // Rechazar producto
    const rejectProduct = async (productId) => {
        try {
            await rejectUserProduct(authToken, productId);
            // Actualizar lista de productos localmente
            setUserProducts(prev => prev.filter(p => p.id !== productId));
        } catch (err) {
            console.error("Error rechazando producto:", err);
            alert("No se pudo rechazar el producto");
        }
    };

    if (!role || role !== 'ADMIN') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
            <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-4">
                {/* First Column */}
                <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
                    <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left mb-6">
                        {t('adminclient.userManagement')}
                    </p>
                    <input
                        type="text"
                        placeholder="Buscar usuario..."
                        value={searchTerm}
                        onChange={handleSearchChange}
                        className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                    />
                    {/* User list */}
                    <div className="flex flex-col gap-6">
                        {Array.isArray(data.userDetails?.content) && data.userDetails.content.length > 0 ? (
                            data.userDetails.content.map((user) => (
                                <div key={user.id} className="bg-gray-50 rounded-lg shadow p-4 hover:shadow-md transition duration-300 border border-gray-200">
                                    <div className="flex items-center space-x-2">
                                        <p className="text-lg font-semibold text-gray-800">{user.firstName} {user.lastName}</p>
                                        <p className="text-sm text-gray-500">@{user.username}</p>
                                    </div>
                                    <div className="text-sm text-gray-600 space-y-1">
                                        <p><span className="font-medium text-gray-700">{t('adminclient.email')}:</span> {user.email || 'N/A'}</p>
                                        <p><span className="font-medium text-gray-700">{t('adminclient.city')}:</span> {user.city || 'N/A'}</p>
                                        <p><span className="font-medium text-gray-700">{t('adminclient.address')}:</span> {user.address || 'N/A'}</p>
                                        <p><span className="font-medium text-gray-700">{t('adminclient.rol')}:</span>
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
                    {/* Pagination */}
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

                {/* Second Column */}
                <div className="w-full">
                    <div className="bg-white p-4 rounded-lg mb-4 w-full">
                        <div className="flex flex-col md:flex-row justify-between gap-4">
                            <div className="w-full md:w-1/2">
                                <MetricCard
                                    icon={faUser}
                                    value={data.numUsers}
                                    title={t('adminclient.registerdUsers')}
                                    iconColor="text-orange-600"
                                    bgColor="bg-orange-300"
                                />
                            </div>
                            <div className="w-full md:w-1/2">
                                <MetricCard
                                    icon={faMusic}
                                    value={data.numArtists}
                                    title={t('adminclient.registerdArtists')}
                                    iconColor="text-yellow-600"
                                    bgColor="bg-yellow-300"
                                />
                            </div>
                        </div>
                    </div>
                    {/* Verifications */}
                    <div className="bg-white p-4 rounded-lg mb-4 w-full overflow-x-auto">
                        <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left mb-6">
                            {t('adminclient.artistVerifications')}
                        </p>
                        {verifications.length > 0 ? (
                            <table className="min-w-full divide-y divide-gray-200 text-sm">
                                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                                    <tr>
                                        <th className="px-4 py-3 text-left">ID</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.artist')}</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.video')}</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.date')}</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.status')}</th>
                                        <th className="px-4 py-3 text-center">{t('adminclient.actions')}</th>
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
                                                <VideoLink videoUrl={verification.videoUrl} />
                                            </td>
                                            <td className="px-4 py-3">
                                                {new Date(verification.date).toLocaleString()}
                                            </td>
                                            <td className="px-4 py-3">
                                                <span className={`px-2 py-1 rounded text-xs font-semibold
                                                    ${verification.status === 'PENDING'
                                                        ? 'bg-yellow-100 text-yellow-700'
                                                        : verification.status === 'REJECTED'
                                                            ? 'bg-red-100 text-red-700'
                                                            : 'bg-green-100 text-green-700'
                                                    }`}>
                                                    {verification.status}
                                                </span>
                                            </td>
                                            <td className="px-4 py-3 text-center">
                                                {verification.status === 'PENDING' && (
                                                    <>
                                                        <button
                                                            onClick={() => verifyArtist(verification.artist.id, verification.id)}
                                                            className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 transition mr-2"
                                                        >
                                                            {t('adminclient.verifyAritst')}
                                                        </button>
                                                        <button
                                                            onClick={() => refuseArtist(verification.id)}
                                                            className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600 transition"
                                                        >
                                                            {t('adminclient.refuseArtist')}
                                                        </button>
                                                    </>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p className="text-gray-500 text-sm text-center">{t('adminclient.noPendingVerification')}</p>
                        )}
                    </div>

                    {/* USER PRODUCT TABLE */}
                    <div className="bg-white p-4 rounded-lg mb-4 w-full overflow-x-auto">
                        <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left mb-6">
                            {t('adminclient.userProducts')}
                        </p>
                        {userProducts.length > 0 ? (
                            <table className="min-w-full divide-y divide-gray-200 text-sm">
                                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                                    <tr>
                                        <th className="px-4 py-3 text-left">ID</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.name')}</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.username')}</th>
                                        <th className="px-4 py-3 text-left">{t('adminclient.image')}</th>
                                        <th className="px-4 py-3 text-center">{t('adminclient.actions')}</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                    {userProducts.map((product) => (
                                        <tr key={product.id} className="hover:bg-gray-50">
                                            <td className="px-4 py-3">{product.id}</td>
                                            <td className="px-4 py-3 font-medium text-gray-800">{product.name}</td>
                                            <td className="px-4 py-3">{product.username}</td>
                                            <td className="px-4 py-3">
                                                <UserProductImages images={product.images} productName={product.name} />
                                            </td>
                                            <td className="px-4 py-3 text-center">
                                                {product.status === "PENDING" ? (
                                                    <>
                                                        <button
                                                            onClick={() => approveProduct(product.id)}
                                                            className="px-3 py-1 text-sm bg-green-500 text-white rounded hover:bg-green-600 transition mr-2"
                                                        >
                                                            {t('adminclient.approve')}
                                                        </button>
                                                        <button
                                                            onClick={() => rejectProduct(product.id)}
                                                            className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600 transition"
                                                        >
                                                            {t('adminclient.reject')}
                                                        </button>
                                                    </>
                                                ) : (
                                                    <span className={`px-2 py-1 rounded text-xs font-semibold 
            ${product.status === 'APPROVED' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                                                        {product.status}
                                                    </span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p className="text-gray-500 text-sm text-center">{t('adminclient.noProducts')}</p>
                        )}
                    </div>

                </div>
            </div>
        </div>
    );
};

export default AdminClient;
