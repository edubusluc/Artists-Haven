import React, { useContext, useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faShoppingCart, faBasketball } from "@fortawesome/free-solid-svg-icons";
import { faTachometerAlt, faBoxOpen, faUsers, faChartLine } from '@fortawesome/free-solid-svg-icons';

import { faUser } from "@fortawesome/free-solid-svg-icons";
import { CartContext } from "../context/CartContext";
import { Link } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import Logout from "./Logout";

const Header = () => {
    const { shoppingCart: contextShoppingCart, handleDeleteProduct } = useContext(CartContext);
    const [shoppingCart, setShoppingCart] = useState({ items: [] });
    const [isDropdownVisible, setDropdownVisible] = useState(false);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const [isSidebarVisible, setSidebarVisible] = useState(false);
    const [isSidebarVisibleLeft, setSidebarVisibleLeft] = useState(false);
    const { t, i18n } = useTranslation();

    const changeLanguage = (lng) => {
        i18n.changeLanguage(lng);
    };


    useEffect(() => {
        if (!authToken) {
            const storedCart = localStorage.getItem("shoppingCart");
            if (storedCart) {
                setShoppingCart(JSON.parse(storedCart));
            }
        } else {
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
        try {
            const response = await fetch("/api/payment_process/checkout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    'Authorization': `Bearer ${authToken}`,
                },
                body: JSON.stringify(shoppingCart.items),
            });

            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${await response.text()}`);
            }

            const data = await response.text();
            window.location.href = data;
            setShoppingCart({ items: [] });
            localStorage.removeItem("shoppingCart");

        } catch (error) {
            alert("Ocurrió un error: " + error);
        }
    };

    const toggleSidebar = () => {
        setSidebarVisible(!isSidebarVisible);
    };
    const closeSidebar = () => {
        setSidebarVisible(false);
    };

    const toggleSidebarLeft = () => {
        setSidebarVisibleLeft(!isSidebarVisibleLeft);
    };

    const closeSidebarLeft = () => {
        setSidebarVisibleLeft(false);
    };



    return (
        <div
            style={{
                display: "flex",
                alignItems: "center",
                padding: "10px 20px",
                backgroundColor: "#002f4c",
                boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                justifyContent: "space-around",
            }}
        >
            <Link to="/" style={{ fontSize: "20px", fontWeight: "bold" }}>
                Home
            </Link>
            <div>
                <Link to="/auth/login">
                    <FontAwesomeIcon icon={faUser} />
                </Link>
            </div>
            <div
                onClick={toggleSidebar}
                style={{
                    cursor: "pointer",
                    position: "relative",
                    display: "inline-block",
                }}
            >
                <FontAwesomeIcon icon={faShoppingCart} size="2x" />
            </div>
            <button onClick={() => changeLanguage('es')}>ES</button>
            <button onClick={() => changeLanguage('en')}>EN</button>

            {/* Overlay oscuro detrás del panel */}
            {isSidebarVisible && (
                <div
                    onClick={closeSidebar}
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        backgroundColor: "rgba(0, 0, 0, 0.5)", // Fondo oscuro
                        zIndex: 998, // Asegura que esté debajo del sidebar
                    }}
                />
            )}

            {/* Barra lateral (sidebar) */}
            <div
                style={{
                    position: "fixed",
                    top: 0,
                    right: isSidebarVisible ? 0 : "-400px", // Controla la visibilidad
                    width: "400px", // El ancho estándar
                    height: "100vh",
                    backgroundColor: "white",
                    boxShadow: "-2px 0 5px rgba(0,0,0,0.1)",
                    transition: "right 0.3s ease",
                    zIndex: 999,
                    padding: "20px",
                    overflowY: "auto",
                }}
            >
                <div>
                    <button
                        onClick={closeSidebar}
                        style={{
                            position: "absolute",
                            top: "20px",
                            right: "20px",
                            color: "black",
                            border: "none",
                            padding: "10px",
                            cursor: "pointer",
                        }}
                    >
                        X
                    </button>
                    <h3 className="p-4 custom-font-shop custom-font-shop-black">{t('ShoppingCart')}</h3>
                    {shoppingCart && shoppingCart.items && shoppingCart.items.length > 0 ? (
                        shoppingCart.items.map((item, index) => (
                            <div key={index} className="mb-4 border-b p-4">
                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                    {/* Contenedor imagen y texto lado a lado */}
                                    <div style={{ display: 'flex', alignItems: 'self-start' }}>
                                        <img
                                            src={`/api/product${item.product.imageUrl}`}
                                            alt={item.product.name}
                                            style={{
                                                height: '90px',  // Ajusta este valor según te guste
                                                width: 'auto',
                                                objectFit: 'contain',
                                                marginRight: '16px',

                                            }}
                                            loading="lazy"
                                        />
                                        <div>
                                            <p className="custom-font-shop-regular custom-font-shop-black">{item.product.name}</p>
                                            <p className="custom-font-shop-regular custom-font-shop-black">{item.product.price}€</p>
                                            <p className="custom-font-shop-regular custom-font-shop-black">Talla: {item.size}</p>
                                        </div>
                                    </div>

                                    {/* Cantidad */}
                                    <div
                                        style={{
                                            border: '1px solid black',
                                            padding: '4px 8px',
                                            borderRadius: '4px',
                                            minWidth: '40px',
                                            textAlign: 'center',
                                            fontWeight: 'bold',
                                        }}
                                    >
                                        {item.quantity}
                                    </div>
                                </div>

                                <button onClick={() => handleDeleteProduct(index)} style={{ marginTop: '12px' }}>
                                    <p className="custom-font-shop-regular custom-font-shop-black">Eliminar producto</p>
                                </button>
                            </div>
                        ))
                    ) : (
                        <p className="p-4 custom-font-shop-regular custom-font-shop-regular-black">{t('AddAProductToShopping')}</p>
                    )}



                    {shoppingCart && shoppingCart.items && shoppingCart.items.length > 0 && (
                        <><div className="p-4 flex justify-between">
                            <p className="custom-font-shop custom-font-shop-black">Total</p>
                            <p className="custom-font-shop custom-font-shop-black">{calculateTotalPrice()}€</p>
                        </div><div
                            className="p-4 border border-black flex justify-center cursor-pointer"
                            onClick={handleRedirectToPayment}
                        >
                                <p className="custom-font-shop custom-font-shop-black">Finalizar Compra</p>
                            </div></>)}



                </div>
            </div>
            <Logout/>
        </div>
    );
};

export default Header;
