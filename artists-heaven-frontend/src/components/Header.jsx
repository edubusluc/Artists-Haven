import React, { useContext, useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faShoppingCart } from "@fortawesome/free-solid-svg-icons";
import { CartContext } from "../context/CartContext";
import { Link } from "react-router-dom";

const Header = () => {
    const { shoppingCart, handleDeleteProduct } = useContext(CartContext);
    const [isDropdownVisible, setDropdownVisible] = useState(false);
    const [authToken] = useState(localStorage.getItem("authToken"));

    useEffect(() => { }, [shoppingCart]);

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

    console.log(shoppingCart);

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
                                        {index}
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
                    </div>
                )}
            </div>
        </div>
    );
};

export default Header;
