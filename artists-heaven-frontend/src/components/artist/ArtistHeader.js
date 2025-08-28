import { useState, useCallback } from "react";
import { useNavigate, Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
    faTachometerAlt,
    faCalendar,
    faBars,
    faUser,
    faRightFromBracket
} from '@fortawesome/free-solid-svg-icons';
import Logout from "../Logout";
import { useTranslation } from 'react-i18next';

const ArtistHeader = () => {
    const [isSidebarVisible, setSidebarVisible] = useState(false);
    const [isSidebarVisibleLeft, setSidebarVisibleLeft] = useState(false);
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const language = i18n.language;
    const changeLanguage = useCallback((lng) => i18n.changeLanguage(lng), [i18n]);

    const handleLinkClick = (path) => {
        closeSidebar();
        navigate(path);
    };

    const links = [
        { to: "artist/dashboard", icon: faTachometerAlt, label: "Dashboard" },
        { to: "/event/all-my-events", icon: faCalendar, label: t('artistHeader.events') },
    ];

    const toggleSidebar = () => setSidebarVisible(!isSidebarVisible);
    const closeSidebar = () => setSidebarVisible(false);

    const toggleSidebarLeft = () => setSidebarVisibleLeft(!isSidebarVisibleLeft);
    const closeSidebarLeft = () => setSidebarVisibleLeft(false);

    return (
        <header className="flex items-center justify-between px-6 py-3 bg-white shadow-md fixed top-0 left-0 right-0 z-50">
            {/* IZQUIERDA - Botón Menú */}
            <div>
                <button onClick={toggleSidebarLeft} className="text-gray-700">
                    <FontAwesomeIcon icon={faBars} className="w-5 h-5" />
                </button>
            </div>

            {/* CENTRO - Logo */}
            <div className="absolute left-1/2 transform -translate-x-1/2">
                <Link to="/" className="text-lg font-bold logo-font">
                    Artists’ Heaven
                </Link>
            </div>

            {/* DERECHA - Usuario */}
            <div className="flex items-center gap-2">
                <button onClick={toggleSidebar} className="text-gray-700">
                    <FontAwesomeIcon icon={faUser} />
                </button>

                <div className="flex gap-2">
                    <button onClick={() => changeLanguage('es')}>
                        <span className="fi fi-es"></span>
                    </button>
                    <button onClick={() => changeLanguage('en')}>
                        <span className="fi fi-gb"></span>
                    </button>
                </div>
            </div>

            {/* Overlay y sidebar derecho (perfil) */}
            {isSidebarVisible && (
                <div
                    onClick={closeSidebar}
                    className="fixed inset-0 bg-black bg-opacity-50 z-40"
                />
            )}
            <div
                className={`p-4 fixed top-0 right-0 w-[400px] h-full bg-white shadow-lg z-50 transition-transform duration-300 transform ${isSidebarVisible ? "translate-x-0" : "translate-x-full"}`}
            >
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
                    ✕
                </button>
                <h3 className="p-4 font-semibold border-b">{t('artistHeader.myProfile')}</h3>
                <div className="p-4 space-y-2">
                    <Link to="/users/profile" onClick={closeSidebar} className="block">
                        <FontAwesomeIcon icon={faUser} className="mr-2" />
                        {t('artistHeader.myProfile')}
                    </Link>
                    <div className="flex items-center">
                        <FontAwesomeIcon icon={faRightFromBracket} className="mr-2" />
                        <Logout />
                    </div>
                </div>
            </div>

            {/* Overlay y sidebar izquierdo (links) */}
            {isSidebarVisibleLeft && (
                <div
                    onClick={closeSidebarLeft}
                    className="fixed inset-0 bg-black bg-opacity-50 z-40"
                />
            )}
            <div
                className={`fixed top-0 left-0 w-[400px] h-full p-4
                    bg-white shadow-lg z-50 transition-transform duration-300
                    transform ${isSidebarVisibleLeft ? "translate-x-0" : "-translate-x-full"}`}
            >
                <button
                    onClick={closeSidebarLeft}
                    className="absolute top-4 right-4 text-gray-600"
                >
                    ✕
                </button>
                <h3
                    className="p-4 custom-font-shop custom-font-shop-black"
                    style={{ borderBottom: "1px solid #e5e7eb" }}
                >
                    {t('artistHeader.panelArtist')}</h3>
                <div className="p-4 space-y-4 custom-font-shop-regular custom-font-shop-black">
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
        </header>
    );
};

export default ArtistHeader;
