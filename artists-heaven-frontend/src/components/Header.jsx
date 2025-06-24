import { useContext, useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBars, faBagShopping } from "@fortawesome/free-solid-svg-icons";
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
    const [isHeaderVisible, setHeaderVisible] = useState(true);
    const [lastScrollY, setLastScrollY] = useState(0);

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

    const toggleSidebarLeft = () => {
        setSidebarVisibleLeft(!isSidebarVisibleLeft);
    };

    const closeSidebarLeft = () => {
        setSidebarVisibleLeft(false);
    };

    const links = [
        { to: "users/mySpace", label: "MY SPACE" },
        { to: "admin/products/store", label: "BEST SELLERS" },
        { to: "admin/orders", label: "WHO ARE WE" },
    ];

    const handleScroll = () => {
        if (window.scrollY > lastScrollY) {
            setHeaderVisible(false);
        } else {
            setHeaderVisible(true);
        }
        setLastScrollY(window.scrollY);
    };

    useEffect(() => {
        window.addEventListener("scroll", handleScroll);

        return () => {
            window.removeEventListener("scroll", handleScroll);
        };
    }, [lastScrollY]);

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

    return (
        // HEADER
        <div
            style={{
                display: "flex",
                alignItems: "center",
                padding: "10px 20px",
                backgroundColor: "white",
                boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                justifyContent: "space-around",
                position: "fixed",
                left: 0,
                right: 0,
                zIndex: 1000,
                transition: "top 0.3s",
                top: isHeaderVisible && !isSidebarVisible && !isSidebarVisibleLeft ? "0" : "-80px",
            }}
        >
            {/* Barra lateral izquierda*/}
            {isSidebarVisibleLeft && (
                <div
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        width: "100%", 
                        maxWidth: "500px",
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
                        ARTISTS HEAVEN
                    </h3>
                    <div className="p-4 custom-font-footer text-3xl" style={{ color: "black" }}>
                        {links
                            .filter(link =>
                                authToken || (link.label !== "MY ORDERS" && link.label !== "MY PROFILE" && link.label !== "MY SPACE")
                            )
                            .map((link, index) => (
                                <Link to={link.to} key={index} onClick={closeSidebarLeft}>
                                    <p>{link.label}</p>
                                </Link>
                            ))
                        }
                        {authToken && <Logout />}
                    </div>


                </div>
            )}

            {/* Overlay borroso detrás del panel */}
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
                        backdropFilter: "blur(10px)",
                        WebkitBackdropFilter: "blur(10px)",
                    }}
                />
            )}

            {isSidebarVisible && (
                <div
                    onClick={closeSidebar}
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        backgroundColor: "rgba(0, 0, 0, 0.5)",
                        zIndex: 998,
                        backdropFilter: "blur(10px)",
                        WebkitBackdropFilter: "blur(10px)",
                    }}
                />
            )}


            <div className="grid grid-cols-3 w-full">
                <div className="col-span-1">
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
                </div>
                <div className="col-span-1 flex justify-center items-center">
                    <Link to="/">
                        <p className="custom-font-shop custom-font-shop-black">ARTISTS HEAVEN</p>
                    </Link>
                </div>
                <div className="col-span-1 flex justify-end space-x-4">
                    <button onClick={() => changeLanguage('es')}>
                        <span className="fi fi-es"></span>
                    </button>

                    <button onClick={() => changeLanguage('en')}>
                        <span className="fi fi-gb"></span>
                    </button>

                    {!authToken && (
                        <div>
                            <Link to="/auth/login">
                                <FontAwesomeIcon icon={faUser} />
                            </Link>
                        </div>
                    )}

                    <div
                        onClick={toggleSidebar}
                        style={{
                            cursor: "pointer",
                            position: "relative",
                            display: "inline-block",
                        }}
                    >
                        <FontAwesomeIcon icon={faBagShopping} />
                    </div>
                </div>
            </div>

            {/* Sidebar */}
            <div
                style={{
                    position: "fixed",
                    top: 0,
                    right: isSidebarVisible ? 0 : "-400px",
                    width: "400px",
                    height: "100vh",
                    backgroundColor: "white",
                    transition: "right 0.3s ease",
                    zIndex: 999,
                    padding: "20px",
                    overflowY: "auto",
                    backdropFilter: "blur(10px)",
                    WebkitBackdropFilter: "blur(10px)",
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
                                    <div style={{ display: 'flex', alignItems: 'self-start' }}>
                                        <img
                                            src={`/api/product${item.product.imageUrl}`}
                                            alt={item.product.name}
                                            style={{
                                                height: '90px',
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
                        <>
                            <div className="p-4 flex justify-between">
                                <p className="custom-font-shop custom-font-shop-black">Total</p>
                                <p className="custom-font-shop custom-font-shop-black">{calculateTotalPrice()}€</p>
                            </div>
                            <div
                                className="p-4 border border-black flex justify-center cursor-pointer"
                                onClick={handleRedirectToPayment}
                            >
                                <p className="custom-font-shop custom-font-shop-black">Finalizar Compra</p>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Header;
