import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { updateOrderStatus } from "../../services/adminServices";
import { checkTokenExpiration } from "../../utils/authUtils";
import NonAuthorise from "../NonAuthorise";
import SessionExpired from "../SessionExpired";
import { useTranslation } from "react-i18next";

const OrderDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [authToken] = useState(localStorage.getItem("authToken"));
    const rol = localStorage.getItem("role");
    const { t } = useTranslation();
    const [orderReturn, setOrderReturn] = useState(null);

    const orderStatuses = [
        "PAID",
        "IN_PREPARATION",
        "SENT",
        "DELIVERED",
        "CANCELED",
        "RETURN_REQUEST",
        "RETURN_ACCEPTED",
    ];

    useEffect(() => {
        const fetchOrder = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const res = await fetch(`/api/orders/${id}`, {
                    headers: {
                        Authorization: `Bearer ${authToken}`,
                    },
                });

                if (res.status === 403) {
                    setErrorMessage("No tienes permisos para acceder a este pedido.");
                    setLoading(false);
                    return;
                }

                if (res.status === 404) {
                    setErrorMessage("Pedido no encontrado.");
                    setLoading(false);
                    return;
                }

                if (!res.ok) {
                    throw new Error("Error al obtener el pedido");
                }

                const response = await res.json();
                setOrder(response.data);
            } catch (err) {
                console.error(err);
                setErrorMessage("No se pudo cargar el pedido.");
            } finally {
                setLoading(false);
            }
        };

        // Solo permitir acceso si el rol es ADMIN
        if (rol === "ADMIN" && checkTokenExpiration()) {
            fetchOrder();
        } else {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            setLoading(false);
        }
    }, [id, rol, authToken]);

    useEffect(() => {
        if (!order) return;
        const fetchReturn = async () => {
            setLoading(true);
            setErrorMessage("");
            try {
                const res = await fetch(`/api/returns/${order.returnId}/return`, {
                    headers: {
                        Authorization: `Bearer ${authToken}`,
                    },
                });
                if (!res.ok) {
                    throw new Error("Error al obtener la devolución");
                }
                const response = await res.json();
                setOrderReturn(response.data);
            } catch (err) {
                setErrorMessage("No se pudo cargar la devolución.");
            } finally {
                setLoading(false);
            }
        };

        if (rol === "ADMIN" && checkTokenExpiration()) {
            fetchReturn();
        } else {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            setLoading(false);
        }
    }, [id, rol, authToken, order]);

    console.log(order)
    console.log("MI ORDER RETURN" + JSON.stringify(orderReturn));


    const handleUpdateOrdeStatus = async (orderId, newStatus) => {
        try {
            await updateOrderStatus(authToken, orderId, newStatus);
            setOrder((prev) => ({ ...prev, status: newStatus }));
        } catch (error) {
            console.error("Error actualizando estado:", error);
            alert("No se pudo actualizar el estado. Intenta de nuevo.");
        }
    };

    if (!rol || rol !== 'ADMIN') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    if (loading)
        return (
            <div className="p-6 text-center text-gray-500 text-lg animate-pulse">{t('adminOrderDetails.loadOrder')}</div>
        );

    if (!order)
        return (
            <div className="p-6 text-center text-red-500 font-semibold">{errorMessage}</div>
        );

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col p-6 lg:p-12 space-y-8">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* COLUMNA 1 */}
                <div className="w-full bg-white rounded-xl shadow-2xl p-8 space-y-6 ring-1 ring-gray-200">
                    <p className="text-3xl font-bold text-gray-800">
                        {t('adminOrderDetails.orderManagemente')} - {order.identifier} - {order.status}
                    </p>

                    {/* Card de Información del Pedido */}
                    <div className="bg-gray-50 p-6 rounded-xl shadow-lg space-y-4">
                        <h3 className="text-xl font-semibold text-gray-800">{t('adminOrderDetails.orderDetails')}</h3>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                            <p className="text-gray-700">{t('adminOrderDetails.internalId')}: <span className="font-medium">{order.id}</span></p>
                            <p className="text-gray-700">{t('adminOrderDetails.identifier')}: <span className="font-medium">{order.identifier}</span></p>
                            <p className="text-gray-700">{t('adminOrderDetails.totalPrice')}: <span className="font-medium">€{order.totalPrice.toFixed(2)}</span></p>
                            <p className="text-gray-700">{t('adminOrderDetails.createdDate')}: <span className="font-medium">{new Date(order.createdDate).toLocaleDateString()}</span></p>
                        </div>
                    </div>

                    {/* Card de Dirección */}
                    <div className="bg-gray-50 p-6 rounded-xl shadow-lg space-y-4">
                        <h3 className="text-xl font-semibold text-gray-800">{t('adminOrderDetails.buyerInformation')}</h3>
                        <p className="text-gray-700">
                            {t('adminOrderDetails.shippingAddress')}: <span className="font-medium">{order.addressLine1} - {order.addressLine2} {order.postalCode}, {order.city}, {order.country}</span>
                        </p>
                        <p className="text-gray-700">{t('adminOrderDetails.email')}: <span className="font-medium">{order.email}</span></p>
                        <p className="text-gray-700">{t('adminOrderDetails.phone')}: <span className="font-medium">{order.phone}</span></p>
                    </div>

                    {/* Actualizar Estado */}
                    <div className="bg-gray-50 p-6 rounded-xl shadow-lg mt-6">
                        <label htmlFor="status-select" className="block text-sm font-semibold text-gray-800 mb-2">
                            {t('adminOrderDetails.updateStatus')}
                        </label>
                        <select
                            id="status-select"
                            value={order.status}
                            onChange={(e) => handleUpdateOrdeStatus(order.id, e.target.value)}
                            className="w-full sm:w-auto border border-gray-300 rounded-xl px-4 py-2 text-gray-700 bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all duration-300"
                        >
                            {orderStatuses.map((status) => (
                                <option key={status} value={status}>
                                    {status.replaceAll("_", " ")}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Botón de Volver */}
                    <div className="mt-8 flex justify-center sm:justify-start">
                        <button
                            onClick={() => navigate("/admin/orders")}
                            className="bg-yellow-500 hover:bg-yellow-600 text-white font-medium py-2 px-6 rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
                        >
                            {t('adminOrderDetails.returnToOrders')}
                        </button>
                    </div>
                </div>

                {/* COLUMNA 2 */}
                <section className="bg-white rounded-3xl shadow-xl p-10 max-h-[600px] overflow-y-auto ring-1 ring-gray-100">
                    <h3 className="text-2xl font-bold text-gray-800 mb-3">{t('adminOrderDetails.orderBreakdown')}</h3>
                    <ul className="space-y-4">
                        {order.items.map((item, idx) => (
                            <li
                                key={idx}
                                className="border border-gray-200 p-5 rounded-xl bg-gray-50 flex flex-col sm:flex-row sm:justify-between sm:items-center shadow-md hover:shadow-xl transition-all duration-200"
                            >
                                <div className="mb-3 sm:mb-0">
                                    <p className="text-gray-800 font-semibold">{item.name}</p>
                                    <p className="text-sm text-gray-600">{t('adminOrderDetails.size')}: {item.size}</p>
                                </div>
                                <div className="flex gap-6 text-sm text-gray-700">
                                    <p><span className="font-medium">{t('adminOrderDetails.quantity')}:</span> {item.quantity}</p>
                                    <p><span className="font-medium">{t('adminOrderDetails.price')}:</span> €{item.price.toFixed(2)}</p>
                                </div>
                            </li>
                        ))}
                    </ul>
                    {orderReturn &&
                        <div className="mt-2">
                            <h3 className="text-2xl font-bold text-gray-800 mb-3">{t('adminOrderDetails.orderBreakdown')}</h3>
                            <div className="border border-gray-200 p-6 rounded-2xl bg-gray-50 shadow-md hover:shadow-lg transition-shadow duration-200">
                                <div className="text-gray-600 text-sm inter-400">
                                    <span className="font-semibold text-gray-800">{t('adminOrderDetails.creationDate')}:</span>
                                    <time
                                        className="text-gray-700 px-1 rounded-full text-sm font-medium"
                                        dateTime={orderReturn.returnDate}
                                    >
                                        {new Date(orderReturn.returnDate).toLocaleDateString('es-ES', {
                                            weekday: 'long',
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                        })}
                                    </time>
                                </div>
                                <div className="text-gray-600 text-sm inter-400">
                                    <span className="font-semibold text-gray-800">{t('adminOrderDetails.details')}:</span> {orderReturn.reason}
                                </div>
                            </div>

                        </div>
                    }
                </section>
            </div>
        </div>
    );
};

export default OrderDetails;
