import React, { useContext, useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faShoppingCart } from "@fortawesome/free-solid-svg-icons";
import { CartContext } from "../context/CartContext";
import { Link } from "react-router-dom";
import { loadStripe } from "@stripe/stripe-js";

// Cargar Stripe con tu clave pública
const stripePromise = loadStripe("tu_clave_publica_de_stripe");

const Header = () => {
    const { shoppingCart: contextShoppingCart, handleDeleteProduct } = useContext(CartContext);
    const [shoppingCart, setShoppingCart] = useState({ items: [] });
    const [isDropdownVisible, setDropdownVisible] = useState(false);
    const [authToken] = useState(localStorage.getItem("authToken"));

    // Cargar carrito desde localStorage si el usuario no está autenticado
    useEffect(() => {
        if (!authToken) {
            const storedCart = localStorage.getItem("shoppingCart");
            if (storedCart) {
                setShoppingCart(JSON.parse(storedCart)); // Establecer carrito desde localStorage
            }
        } else {
            // Si el usuario está autenticado, usar el carrito desde el contexto
            setShoppingCart(contextShoppingCart);
        }
    }, [authToken, contextShoppingCart]);

    const handleMouseEnter = () => {
        setDropdownVisible(true);
    };

    const handleMouseLeave = () => {
        setDropdownVisible(false);
    };

    const calculateTotalPrice = () => {
        let total = 0;
        if (shoppingCart && shoppingCart.items) {
            shoppingCart.items.forEach((item) => {
                total += item.product.price * item.quantity;
            });
        }
        return total;
    };

    const handleRedirectToPayment = async () => {
        // Obtén la instancia de Stripe
        const stripe = await stripePromise;

        // Enviar el carrito al backend para generar la sesión de pago
        fetch("/api/payment_process/checkout", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                'Authorization': `Bearer ${authToken}`,
            },
            body: JSON.stringify(shoppingCart.items),
        })
            .then((response) => response.text())
            .then((data) => {
                window.location.href = data;
                setShoppingCart({ items: [] }); // Vaciar carrito   
                localStorage.removeItem("shoppingCart"); // Eliminar carrito de localStorage
            })
            .catch((error) => {
                console.error("Error al crear la sesión de pago: " + error);
            });
    };

    return (
        <div
            style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "10px 20px",
                backgroundColor: "#f8f9fa",
                borderBottom: "1px solid #ccc",
            }}
        >
            <Link to="/" style={{ fontSize: "20px", fontWeight: "bold" }}>
                Home
            </Link>

            <div
                onMouseEnter={handleMouseEnter}
                onMouseLeave={handleMouseLeave}
                style={{ position: "relative", display: "inline-block" }}
            >
                <FontAwesomeIcon icon={faShoppingCart} size="2x" />
                {isDropdownVisible && (
                    <div
                        style={{
                            position: "absolute",
                            top: "100%",
                            right: 0,
                            backgroundColor: "white",
                            border: "1px solid #ccc",
                            boxShadow: "0 8px 16px rgba(0,0,0,0.2)",
                            zIndex: 1,
                            width: "200px",
                            padding: "10px",
                        }}
                    >
                        {shoppingCart && shoppingCart.items && shoppingCart.items.length > 0 ? (
                            <ul>
                                {shoppingCart.items.map((item, index) => (
                                    <li key={`${item.product.id}-${item.size}`}>
                                        {item.product.name} - Talla: {item.size} - Cantidad: {item.quantity} - Precio: {item.product.price * item.quantity}€
                                        <button
                                            onClick={() => handleDeleteProduct(index)}
                                            className="btn btn-primary"
                                        >
                                            -
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>¡Añada algún producto a su carrito!</p>
                        )}
                        <p>Total: {calculateTotalPrice()}€</p>
                        {shoppingCart && shoppingCart.items && shoppingCart.items.length > 0 && (
                            <button onClick={handleRedirectToPayment}>Ir a la Pasarela de Pago</button>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Header;
