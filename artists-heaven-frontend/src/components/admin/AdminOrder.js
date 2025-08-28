import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import CountStatusPieChart from '../charts/CountPieChart';
import NonAuthorise from '../NonAuthorise';
import { getStatisticsPerYear, getOrders } from '../../services/adminServices';
import { checkTokenExpiration } from '../../utils/authUtils';
import SessionExpired from '../SessionExpired';
import { useTranslation } from "react-i18next";

const AdminOrder = () => {
    const currentYear = new Date().getFullYear();
    const [year, setYear] = useState(currentYear);

    const role = localStorage.getItem("role");
    const [authToken] = useState(localStorage.getItem("authToken"));

    const [data, setData] = useState({
        numOrders: 0,
        orderStatusCounts: {},
        incomePerYear: 0,
    });

    const [orders, setOrders] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 6;

    const returnRequestOrders = orders.filter(order => order.status === 'RETURN_REQUEST');
    const filteredOrders = orders.filter(order => order.status !== 'RETURN_REQUEST');

    const [sortBy, setSortBy] = useState(null);
    const [sortOrder, setSortOrder] = useState('asc');

    const navigate = useNavigate();
    const {t} = useTranslation();

    const handleSort = (column) => {
        if (sortBy === column) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            setSortBy(column);
            setSortOrder('asc');
        }
    };

    const sortedOrders = React.useMemo(() => {
        if (!sortBy) return filteredOrders;
        return [...filteredOrders].sort((a, b) => {
            let aVal = a[sortBy];
            let bVal = b[sortBy];
            if (typeof aVal === 'string') aVal = aVal.toLowerCase();
            if (typeof bVal === 'string') bVal = bVal.toLowerCase();
            if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
            if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
            return 0;
        });
    }, [filteredOrders, sortBy, sortOrder]);

    const renderSortIcon = (column) => {
        if (sortBy !== column) return null;
        return sortOrder === 'asc' ? ' ▲' : ' ▼';
    };

    useEffect(() => {
        if (!checkTokenExpiration || role !== 'ADMIN') return;

        const controller = new AbortController();

        const fetchData = async () => {
            try {
                const staticsData = await getStatisticsPerYear(authToken, year);
                console.log(staticsData)
                setData(prev => ({
                    ...prev,
                    ...staticsData.data,
                    incomePerYear: staticsData.data.incomePerYear ?? 0,
                }));
            } catch (error) {
                if (error.name !== 'AbortError') {
                    console.error('Error al obtener datos de staticsPerYear:', error);
                }
            }
        };

        const fetchOrders = async () => {
            try {
                const ordersData = await getOrders(authToken, page, pageSize)
                setOrders(ordersData.content || []);
                setTotalPages(ordersData.totalPages || 1);
            } catch (error) {
                if (error.name !== 'AbortError') {
                    console.error('Error al obtener órdenes:', error);
                }
            }
        };

        fetchData();
        fetchOrders();

        return () => {
            controller.abort();
        };
    }, [authToken, year, page]);

    const nextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    const prevPage = () => {
        if (page > 0) setPage(page - 1);
    };

    if (!role || role !== 'ADMIN') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
            <div className="grid grid-cols-1 lg:grid-cols-[2fr_1fr] p-4 m-4 gap-4">

                {/* Tabla principal de pedidos */}
                <div className='col-start-1 col-span-1'>
                    <div className="w-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4 overflow-x-auto">
                        <p className="text-2xl font-bold mb-6">{t('adminOrders.ordersManagement')}</p>

                        {orders.length > 0 ? (
                            <table className="min-w-full text-sm text-left text-gray-600 border border-gray-200 rounded-lg">
                                <thead className="bg-gray-100 text-xs text-gray-700 uppercase">
                                    <tr>
                                        <th className="px-4 py-3 cursor-pointer" onClick={() => handleSort('id')}>ID{renderSortIcon('id')}</th>
                                        <th className="px-4 py-3 cursor-pointer" onClick={() => handleSort('identifier')}>{t('adminOrders.identifier')}{renderSortIcon('identifier')}</th>
                                        <th className="px-4 py-3 cursor-pointer" onClick={() => handleSort('phone')}>{t('adminOrders.phone')}{renderSortIcon('phone')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.address')}</th>
                                        <th className="px-4 py-3 cursor-pointer" onClick={() => handleSort('totalPrice')}>{t('adminOrders.totalPrice')}{renderSortIcon('totalPrice')}</th>
                                        <th className="px-4 py-3 cursor-pointer" onClick={() => handleSort('createdDate')}>{t('adminOrders.createdDate')}{renderSortIcon('createdDate')}</th>
                                        <th className="px-4 py-3 cursor-pointer" onClick={() => handleSort('paymentIntent')}>{t('adminOrders.paymentIntent')}{renderSortIcon('paymentIntent')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.status')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {sortedOrders.map(order => (
                                        <tr
                                            key={order.id}
                                            className="border-t hover:bg-gray-100 cursor-pointer transition"
                                            onClick={() => navigate(`/admin/orderDetails/${order.id}`)}>
                                            <td className="px-4 py-3 font-medium">{order.id}</td>
                                            <td className="px-4 py-3">{order.identifier}</td>
                                            <td className="px-4 py-3">{order.phone}</td>
                                            <td className="px-4 py-3">{order.addressLine1}, {order.postalCode}, {order.city}, {order.country}</td>
                                            <td className="px-4 py-3">€{order.totalPrice}</td>
                                            <td className="px-4 py-3">{order.createdDate}</td>
                                            <td className="px-4 py-3">{order.paymentIntent}</td>
                                            <td className="px-4 py-3">{order.status}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p className="text-gray-500 text-center">{t('adminOrders.noOrdersAvailable')}</p>
                        )}

                        <div className="flex justify-center items-center mt-4 gap-4">
                            <button onClick={prevPage} disabled={page === 0} className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50">{t('adminOrders.previous')}</button>
                            <span className="font-semibold text-gray-700">{t('adminOrders.page')} {page + 1} {t('adminOrders.of')} {totalPages}</span>
                            <button onClick={nextPage} disabled={page >= totalPages - 1} className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50">{t('adminOrders.next')}</button>
                        </div>
                    </div>

                    {/* Tabla de devoluciones */}
                    <div className="mt-4 w-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4 overflow-x-auto">
                        <p className="text-2xl font-bold mb-6">{t('adminOrders.pendingReturns')}</p>

                        {returnRequestOrders.length > 0 ? (
                            <table className="min-w-full text-sm text-left text-gray-600 border border-gray-200 rounded-lg">
                                <thead className="bg-gray-100 text-xs text-gray-700 uppercase">
                                    <tr>
                                        <th className="px-4 py-3">ID</th>
                                        <th className="px-4 py-3">{t('adminOrders.identifier')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.phone')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.address')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.totalPrice')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.createdDate')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.paymentIntent')}</th>
                                        <th className="px-4 py-3">{t('adminOrders.status')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {returnRequestOrders.map(order => (
                                        <tr
                                            key={order.id}
                                            className="border-t hover:bg-gray-50"
                                            onClick={() => navigate(`/admin/orderDetails/${order.id}`)}>
                                            <td className="px-4 py-3 font-medium">{order.id}</td>
                                            <td className="px-4 py-3">{order.identifier}</td>
                                            <td className="px-4 py-3">{order.phone}</td>
                                            <td className="px-4 py-3">{order.addressLine1}, {order.postalCode}, {order.city}, {order.country}</td>
                                            <td className="px-4 py-3">€{order.totalPrice}</td>
                                            <td className="px-4 py-3">{order.createdDate}</td>
                                            <td className="px-4 py-3">{order.paymentIntent}</td>
                                            <td className="px-4 py-3">{order.status}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p className="text-gray-500 text-center">{t('adminOrders.noReturnsAvailabkle')}</p>
                        )}
                    </div>
                </div>

                {/* Gráfico de resumen */}
                <div className="flex flex-col md:flex-row gap-4">
                    <div className="bg-white shadow-lg rounded-lg p-6 flex-1">
                        <p className="text-lg font-semibold text-gray-600 mb-4">{t('adminOrders.ordersStatusSummary')}</p>
                        {data.orderStatusCounts && Object.keys(data.orderStatusCounts).length > 0 ? (
                            <CountStatusPieChart orderStatusCounts={data.orderStatusCounts} />
                        ) : (
                            <p className="text-gray-400 italic text-center">{t('adminOrders.noDataAvailable')}</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminOrder;
