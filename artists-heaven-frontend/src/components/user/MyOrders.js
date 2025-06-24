import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

const MyOrders = () => {
    const [orders, setOrders] = useState([]);
    const [productImages, setProductImages] = useState({});
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const authToken = localStorage.getItem("authToken");
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(false);
    const size = 3;

    const orderSteps = ["PAID", "IN_PREPARATION", "SENT", "DELIVERED"];

    const OrderProgressBar = ({ status }) => {
        if (status === "CANCELED") {
            return (
                <div className="p-4 border border-red-300 bg-red-50 rounded-lg text-red-600 text-center font-semibold">
                    ❌ Order has been canceled.
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
                {/* Step Circles with Connectors */}
                <div className="flex items-center justify-between relative mb-4 flex-wrap gap-2 sm:gap-0">
                    {orderSteps.map((step, index) => {
                        const isCompleted = index <= currentStepIndex;
                        const isLast = index === orderSteps.length - 1;

                        return (
                            <div key={step} className="flex-1 flex items-center min-w-[60px] relative">
                                {/* Left line (skip for first item) */}
                                {index !== 0 && (
                                    <div
                                        className={`h-1 flex-1 ${index <= currentStepIndex ? "bg-green-500" : "bg-gray-300"}`}
                                    />
                                )}

                                {/* Step Circle */}
                                <div
                                    className={`w-6 h-6 z-10 flex items-center justify-center rounded-full text-xs font-bold
                                ${isCompleted ? "bg-green-500 text-white" : "bg-gray-300 text-gray-600"}`}
                                >
                                    {index + 1}
                                </div>

                                {/* Right line (skip for last item) */}
                                {!isLast && (
                                    <div
                                        className={`h-1 flex-1 ${index < currentStepIndex ? "bg-green-500" : "bg-gray-300"}`}
                                    />
                                )}
                            </div>
                        );
                    })}
                </div>

                {/* Step Labels */}
                <div className="flex justify-between text-[10px] sm:text-xs text-gray-600 font-medium px-1 flex-wrap">
                    {orderSteps.map((step, index) => {
                        let alignment = "text-center"; // default

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



    const fetchOrders = () => {
        if (!authToken) {
            setError("User is not authenticated.");
            setLoading(false);
            return;
        }

        setLoading(true);
        fetch(`/api/orders/myOrders?page=${page}&size=${size}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${authToken}`,
            },
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch orders");
                return res.json();
            })
            .then((data) => {
                setOrders(data.orders || []);
                setProductImages(data.productImages || {});
                setHasMore((data.orders || []).length === size); // If less than `size`, no more pages
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchOrders();
    }, [page]);

    const handlePrev = () => {
        if (page > 0) setPage(page - 1);
    };

    const handleNext = () => {
        if (hasMore) setPage(page + 1);
    };

    if (loading) return <p className="text-gray-600">Loading your orders...</p>;

    if (error)
        return <p className="text-red-500 font-medium bg-red-100 p-4 rounded">{error}</p>;

    if (orders.length === 0)
        return (
            <div className="text-center text-gray-500 py-10">
                <p className="text-lg">🛒 You haven’t placed any orders yet.</p>
            </div>
        );

    return (
        <div className="space-y-6">
            {orders.map((order) => (
                <div key={order.id} className="bg-white p-6 rounded-xl shadow flex flex-col gap-4">
                    {order.status === "DELIVERED" && (
                        <div className="text-right">
                            <Link to={`/orders/return/${order.id}`}>
                                <button className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition hover:bg-yellow-500">
                                    Solicitar Devolución
                                </button>
                            </Link>
                        </div>
                    )}
                    <OrderProgressBar status={order.status} />
                    <div className="flex justify-between items-center">
                        <div>
                            <h3 className="text-xl font-semibold">Order #{order.identifier}</h3>
                            <p className="text-sm text-gray-500">Status: {order.status}</p>
                        </div>
                        <div className="text-right">
                            <p className="text-sm text-gray-500">Total:</p>
                            <p className="text-lg font-bold text-green-600">${order.totalPrice.toFixed(2)}</p>
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
                                            loading="lazy"
                                        />
                                    </div>
                                    <div className="flex flex-col justify-between w-full">
                                        <p className="font-medium">Product: {item.name}</p>
                                        <p>Size: {item.size}</p>
                                        <p>Qty: {item.quantity}</p>
                                        <p className="text-green-700 font-semibold">
                                            ${item.price.toFixed(2)}
                                        </p>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            ))}

            {/* Pagination Controls */}
            <div className="flex justify-between pt-6">
                <button
                    disabled={page === 0}
                    onClick={handlePrev}
                    className="px-4 py-2 rounded bg-gray-200 text-sm font-medium disabled:opacity-50"
                >
                    ⬅ Previous
                </button>
                <button
                    disabled={!hasMore}
                    onClick={handleNext}
                    className="px-4 py-2 rounded bg-gray-200 text-sm font-medium disabled:opacity-50"
                >
                    Next ➡
                </button>
            </div>
        </div>
    );
};

export default MyOrders;
