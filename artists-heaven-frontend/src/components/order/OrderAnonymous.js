import { useLocation } from 'react-router-dom';
import { useEffect, useState } from "react";
import ReturnRequestModal from '../user/ReturnRequestModal';

const OrderAnonymous = () => {
    const location = useLocation();
    const response = location.state?.order;

    const [order, setOrder] = useState(null);
    const [productImages, setProductImages] = useState({});
    const authToken = localStorage.getItem("authToken");
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedOrderId, setSelectedOrderId] = useState(null);

    const orderSteps = ["PAID", "IN_PREPARATION", "SENT", "DELIVERED"];

    useEffect(() => {
        if (response && response.orders) {
            setOrder(response.orders);
            setProductImages(response.productImages);
        } else {
            console.log("Response is falsy:", response);
        }
    }, [response]);

    if (!order) {
        return <div className="p-8 text-center text-gray-500">Loading order details...</div>;
    }

    const OrderProgressBar = ({ status }) => {
        if (status === "CANCELED") {
            return (
                <div className="p-4 border border-red-300 bg-red-50 rounded-lg text-red-600 text-center font-semibold">
                    ❌ Order has been canceled.
                </div>
            );
        }

        if (status === "RETURN_REQUEST") {
            return (
                <div className="p-4 border border-yellow-300 bg-yellow-50 rounded-lg text-yellow-600 text-center font-semibold">
                    Return of the order has been requested
                </div>
            );
        }

        const displayStatus =
            status === "RETURN_REQUEST" || status === "RETURN_ACCEPTED"
                ? "DELIVERED"
                : status;

        const currentStepIndex = orderSteps.indexOf(displayStatus);

        return (
            <div className="w-full">
                <div className="flex items-center justify-between relative mb-4 flex-wrap gap-2 sm:gap-0">
                    {orderSteps.map((step, index) => {
                        const isCompleted = index <= currentStepIndex;
                        const isLast = index === orderSteps.length - 1;

                        return (
                            <div key={step} className="flex-1 flex items-center min-w-[60px] relative">
                                {index !== 0 && (
                                    <div
                                        className={`h-1 flex-1 ${index <= currentStepIndex ? "bg-green-500" : "bg-gray-300"}`}
                                    />
                                )}

                                <div
                                    className={`w-6 h-6 z-10 flex items-center justify-center rounded-full text-xs font-bold
                                ${isCompleted ? "bg-green-500 text-white" : "bg-gray-300 text-gray-600"}`}
                                >
                                    {index + 1}
                                </div>

                                {!isLast && (
                                    <div
                                        className={`h-1 flex-1 ${index < currentStepIndex ? "bg-green-500" : "bg-gray-300"}`}
                                    />
                                )}
                            </div>
                        );
                    })}
                </div>

                <div className="flex justify-between text-[10px] sm:text-xs text-gray-600 font-medium px-1 flex-wrap">
                    {orderSteps.map((step, index) => {
                        let alignment = "text-center";
                        if (step === "PAID") alignment = "text-left";
                        else if (step === "DELIVERED") alignment = "text-right";

                        return (
                            <div
                                key={index}
                                className={`flex-1 min-w-[60px] ${alignment} whitespace-normal break-words`}
                            >
                                {step.replace("_", " ")}
                            </div>
                        );
                    })}
                </div>
            </div>
        );
    };

    const handleOpenModal = (orderId) => {
        setSelectedOrderId(orderId);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedOrderId(null);
    };

    const handleSubmitReason = (reason, email) => {
        fetch(`/api/returns/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${authToken}`,
            },
            body: JSON.stringify({
                orderId: selectedOrderId,
                reason: reason,
                email: email,
            }),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to create return request");
                return res.text();
            })
            .then((msg) => {
                alert(msg);
                setOrder((prev) => ({ ...prev, status: "RETURN_REQUEST" }));
                handleCloseModal();
            })
            .catch((err) => alert(err.message));
    };

    const handleDownloadLabel = async (orderId) => {
        let emailParam = "";

        if (!authToken) {
            const email = prompt("Por favor, introduce el correo electrónico de la compra:");

            if (!email || !email.trim()) {
                alert("Correo electrónico requerido para continuar.");
                return;
            }

            emailParam = `?email=${encodeURIComponent(email.trim())}`;
        }

        const response = await fetch(`/api/returns/${orderId}/label${emailParam}`, {
            method: "GET",
            headers: {
                ...(authToken && { Authorization: `Bearer ${authToken}` }),
            },
        });

        if (!response.ok) {
            alert("Error al descargar la etiqueta de devolución");
            return;
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `RETURN_LABEL_${orderId}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
    };


    return (
        <>
            <div className="bg-white p-8 rounded-xl shadow flex flex-col gap-4">
                {order.status === "DELIVERED" && (
                    <div className="text-right">
                        <button
                            onClick={() => handleOpenModal(order.id)}
                            className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition hover:bg-yellow-500"
                        >
                            Solicitar Devolución
                        </button>
                    </div>
                )}
                {order.status === "RETURN_REQUEST" && (
                    <div className="text-right">
                        <button
                            onClick={() => handleDownloadLabel(order.id)}
                            className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition hover:bg-yellow-500"
                        >
                            📄 Descargar Etiqueta de Devolución
                        </button>
                    </div>
                )}
                <OrderProgressBar status={order.status} />
                <div className="flex justify-between items-center">
                    <div>
                        <h3 className="text-xl font-semibold">Order #{order.identifier}</h3>
                        <p className="text-sm text-gray-500">Status: {order.status}</p>
                    </div>
                    <div className="text-right">
                        <p className="text-lg font-bold text-green-600">€{order.totalPrice.toFixed(2)}</p>
                    </div>
                </div>

                <div className="text-sm text-gray-600">
                    <p>📍 Shipping to: {order.addressLine1}</p>
                </div>

                <div className="flex flex-col gap-4 text-sm text-gray-700">
                    {order.items.map((item) => {
                        const imagePath = productImages[item.productId];
                        return (
                            <div key={item.id} className="border rounded-lg p-4 bg-gray-50 shadow-sm flex flex-col md:flex-row gap-4">
                                <div className="w-full md:w-32 flex-shrink-0">
                                    <img
                                        src={imagePath ? `/api/product${imagePath}` : "/placeholder.jpg"}
                                        alt={item.name}
                                        className="w-full h-32 object-contain rounded-md"
                                        onError={(e) => (e.target.src = "/placeholder.jpg")}
                                        loading="lazy" />
                                </div>
                                <div className="flex flex-col justify-between w-full">
                                    <p className="font-medium">Product: {item.name}</p>
                                    <p>Size: {item.size}</p>
                                    <p>Qty: {item.quantity}</p>
                                    <p className="text-green-700 font-semibold">
                                        €{item.price.toFixed(2)}
                                    </p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
            <ReturnRequestModal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                onSubmit={handleSubmitReason} />
        </>
    );
};

export default OrderAnonymous;
