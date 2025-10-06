import React, { createContext, useState, useEffect } from "react";

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [shoppingCart, setShoppingCart] = useState({ items: [] });
    const authToken = localStorage.getItem("authToken"); // ✅ no usamos useState

    // Cargar carrito desde localStorage si NO hay authToken (solo una vez al montar)
    useEffect(() => {
        if (!authToken) {
            const storedCart = localStorage.getItem("shoppingCart");
            if (storedCart) {
                setShoppingCart(JSON.parse(storedCart));
            }
        }
    }, []); // ✅ se ejecuta solo una vez

    // Cargar carrito desde la API si hay authToken
    useEffect(() => {
        if (authToken) {
            const fetchShoppingCart = async () => {
                try {
                    const response = await fetch(`/api/myShoppingCart`, {
                        method: "GET",
                        headers: { Authorization: `Bearer ${authToken}` },
                    });

                    if (!response.ok) {
                        throw new Error("Error al obtener el carrito de compras");
                    }

                    const text = await response.text();
                    const data = text ? JSON.parse(text) : { items: [] };

                    // ✅ Evitamos re-render innecesario
                    setShoppingCart((prev) => {
                        if (JSON.stringify(prev) === JSON.stringify(data)) {
                            return prev;
                        }
                        return data;
                    });
                } catch (error) {
                    console.error("Error:", error);
                }
            };

            fetchShoppingCart();
        }
    }, [authToken]);

    // Actualizar carrito tanto en el estado como en localStorage
    const updateShoppingCart = (data) => {
        const newCart = Array.isArray(data) ? { items: data } : data;

        setShoppingCart((prev) => {
            if (JSON.stringify(prev) === JSON.stringify(newCart)) {
                return prev;
            }
            return newCart;
        });

        localStorage.setItem("shoppingCart", JSON.stringify(newCart));
    };

    const handleDeleteProduct = async (id, size) => {
        const endpoint = authToken
            ? `/api/myShoppingCart/deleteProducts`
            : `/api/myShoppingCart/deleteProductsNonAuthenticated`;

        const payload = authToken
            ? { itemId: id }
            : { shoppingCart: shoppingCart, productId: id };

        try {
            const response = await fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(authToken && { Authorization: `Bearer ${authToken}` }),
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error("Error al eliminar el producto del carrito");
            }

            const data = await response.json();
            updateShoppingCart(data);
        } catch (error) {
            console.error("Error al eliminar el producto del carrito:", error);
        }
    };

    return (
        <CartContext.Provider value={{ shoppingCart, setShoppingCart: updateShoppingCart, handleDeleteProduct }}>
            {children}
        </CartContext.Provider>
    );
};
