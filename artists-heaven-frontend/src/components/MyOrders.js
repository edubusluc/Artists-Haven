import React, { useEffect, useState } from "react";

const MyOrders = () => {
    const [orders, setOrders] = useState([]);
    const [error, setError] = useState(null);
    const authToken = localStorage.getItem("authToken");

    useEffect(() => {
        if (!authToken) {
            setError("User is not authenticated.");
            return;
        }

        fetch("/api/orders/myOrders", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${authToken}`,
            },
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Failed to fetch orders");
                }
                return response.json();
            })
            .then((data) => setOrders(data))
            .catch((error) => setError(error.message));
    }, [authToken]);

    return (
        <div>
            <h1>My Orders</h1>
            {error && <p style={{ color: "red" }}>{error}</p>}
            {orders.length === 0 ? (
                <p>No orders found.</p>
            ) : (
                <ul>
                    {orders.map((order) => (
                        <li key={order.id}>
                            <p>Identifier: {order.identifier}</p>
                            <p>Total: ${order.totalPrice}</p>
                            <p>Status: {order.status}</p>
                            <p>Address: {order.addressLine1}</p>
                            <ul>
                                {order.items.map((item) => (
                                    <li key={item.id}>
                                        {item.name} {item.size} - {item.quantity} - ${item.price}
                                    </li>
                                ))}
                            </ul>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default MyOrders;