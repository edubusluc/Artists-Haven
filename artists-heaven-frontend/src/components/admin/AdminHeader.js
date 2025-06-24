import React, { useContext, useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTachometerAlt, faBoxOpen, faUsers, faBars, faShoppingCart, faUser, faRightFromBracket } from '@fortawesome/free-solid-svg-icons';
import { Link } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import Logout from "../Logout";

const AdminHeader = () => {
    const [shoppingCart, setShoppingCart] = useState({ items: [] });
    const [isDropdownVisible, setDropdownVisible] = useState(false);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const [isSidebarVisible, setSidebarVisible] = useState(false);
    const [isSidebarVisibleLeft, setSidebarVisibleLeft] = useState(false);
    const { t, i18n } = useTranslation();

    const changeLanguage = (lng) => {
        i18n.changeLanguage(lng);
    };

    const handleMouseEnter = () => {
        setDropdownVisible(true);
    };

    const handleMouseLeave = () => {
        setDropdownVisible(false);
    };

    const links = [
        { to: "admin/dashboard", icon: faTachometerAlt, label: "Dashboard" },
        { to: "admin/products/store", icon: faBoxOpen, label: "Gestión de Productos" },
        { to: "admin/orders", icon: faShoppingCart, label: "Órdenes" },
        { to: "admin/clients", icon: faUsers, label: "Clientes" },
    ];

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
        <div className="p-4"
            style={{
                display: "flex",
                justifyContent: "space-between",
                background: "white"
            }}
        >
            <div
                onClick={toggleSidebarLeft}
                style={{
                    cursor: "pointer",
                    position: "relative",
                    display: "inline-block",
                }}
            >

                <FontAwesomeIcon icon={faBars} size="xl" />
            </div>
            <p className="custom-font-shop custom-font-shop-black">ARTISTS HEAVEN</p>
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
                    <h3 className="p-4 custom-font-shop custom-font-shop-black">My Profile</h3>
                    <div className="p-4 ustom-font-shop-regular custom-font-shop-black">
                        <Link to="/users/profile">
                            <p>
                                <FontAwesomeIcon icon={faUser} /> My Profile
                            </p>
                        </Link>
                        <div className="flex items-center">
                            <FontAwesomeIcon icon={faRightFromBracket} className="mr-1" /> 
                            <Logout />
                        </div>
                    </div>
                </div>
            </div>
            <div
                onClick={toggleSidebar}
                style={{
                    cursor: "pointer",
                    position: "relative",
                    display: "inline-block",
                }}
            >
                <FontAwesomeIcon icon={faUser} size="xl" />
            </div>


            {/* Overlay oscuro detrás del panel */}
            {isSidebarVisibleLeft && (
                <div
                    onClick={closeSidebarLeft}
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        backgroundColor: "rgba(0, 0, 0, 0.5)",
                        zIndex: 998,
                    }}
                />
            )}

            {/* Barra lateral (sidebar) */}
            <div
                style={{
                    position: "fixed",
                    top: 0,
                    left: isSidebarVisibleLeft ? 0 : "-400px",
                    width: "400px",
                    height: "100vh",
                    backgroundColor: "white",
                    boxShadow: "-2px 0 5px rgba(0,0,0,0.1)",
                    transition: "left 0.3s ease",
                    zIndex: 999,
                    padding: "20px",
                    overflowY: "auto",
                }}
            >
                <button
                    onClick={closeSidebarLeft}
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
                <h3
                    className="p-4 custom-font-shop custom-font-shop-black"
                    style={{ borderBottom: "1px solid #e5e7eb" }}
                >
                    Admin Panel
                </h3>
                <div className="p-4 custom-font-shop-regular custom-font-shop-black">
                    {links.map((link, index) => (
                        <Link to={link.to} key={index} onClick={closeSidebarLeft}>
                            <p className="mt-4">
                                <FontAwesomeIcon icon={link.icon} style={{ marginRight: "8px" }} />
                                {link.label}
                            </p>
                        </Link>
                    ))}
                </div>
            </div>
        </div>
    );

};

export default AdminHeader;
