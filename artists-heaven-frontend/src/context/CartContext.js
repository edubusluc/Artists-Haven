import React, { createContext, useState, useEffect } from "react";

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [shoppingCart, setShoppingCart] = useState({ items: [] });
    const [authToken] = useState(localStorage.getItem("authToken"));

    // Cargar carrito desde localStorage si no hay authToken
    useEffect(() => {
        if (!authToken) {
            const storedCart = localStorage.getItem("shoppingCart");
            if (storedCart) {
                setShoppingCart(JSON.parse(storedCart));
            }
        } else {
            const fetchShoppingCart = async () => {
                try {
                    const response = await fetch(`/api/myShoppingCart`, {
                        method: "GET",
                        headers: {
                            Authorization: `Bearer ${authToken}`,
                        },
                    });

                    if (!response.ok) {
                        throw new Error("Error al obtener el carrito de compras");
                    }

                    const text = await response.text();
                    const data = text ? JSON.parse(text) : { items: [] };
                    setShoppingCart(data);
                } catch (error) {
                    console.error("Error:", error);
                }
            };

            fetchShoppingCart();
        }
    }, [authToken]);

    // Actualizar carrito tanto en el estado como en localStorage
    const updateShoppingCart = (data) => {
        if (Array.isArray(data)) {
            setShoppingCart({ items: data });
            localStorage.setItem("shoppingCart", JSON.stringify({ items: data }));
        } else {
            setShoppingCart(data);
            localStorage.setItem("shoppingCart", JSON.stringify(data));
        }
    };

    const handleDeleteProduct = async (id, size) => {
        const endpoint = authToken 
            ? `/api/myShoppingCart/deleteProducts` 
            : `/api/myShoppingCart/deleteProductsNonAuthenticated`;
    
        const payload = authToken ? {
            itemId: id,
        }
        :{
            shoppingCart: shoppingCart,
            productId: id,
        };
    
        try {
            const response = await fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(authToken && { Authorization: `Bearer ${authToken}` }), // Solo añade Authorization si hay token
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
