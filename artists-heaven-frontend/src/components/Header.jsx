import { useContext, useState, useEffect, useMemo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBars, faBagShopping, faSearch, faUser } from "@fortawesome/free-solid-svg-icons";
import { CartContext } from "../context/CartContext";
import { Link } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import Logout from "./Logout";
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';



// Componente reutilizable de panel deslizante

const SlidingPanel = ({ isOpen, position = "right", onClose, children, maxWidth = "400px" }) => {
    const sideStyles = position === "left"
        ? { left: 0 }
        : { right: 0 };

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        onClick={onClose}
                        className="fixed inset-0 bg-black bg-opacity-50 z-40"
                    />
                    <motion.div
                        initial={{ x: position === 'left' ? '-100%' : '100%' }}
                        animate={{ x: 0 }}
                        exit={{ x: position === 'left' ? '-100%' : '100%' }}
                        transition={{ duration: 0.3 }}
                        className="fixed top-0 h-full bg-white z-50 p-6 overflow-y-auto shadow-xl"
                        style={{ ...sideStyles, maxWidth, width: '100%' }}
                    >
                        <button onClick={onClose} className="absolute top-4 right-4">X</button>
                        {children}
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
}

const Header = () => {
    const { shoppingCart: contextShoppingCart, handleDeleteProduct } = useContext(CartContext);
    const [shoppingCart, setShoppingCart] = useState({ items: [] });
    const authToken = useMemo(() => localStorage.getItem("authToken"), []);
    const [isLeftPanelOpen, setLeftPanelOpen] = useState(false);
    const [isCartPanelOpen, setCartPanelOpen] = useState(false);
    const [isSearchPanelOpen, setSearchPanelOpen] = useState(false);
    const { t, i18n } = useTranslation();
    const [isHeaderVisible, setHeaderVisible] = useState(true);
    const [lastScrollY, setLastScrollY] = useState(0);
    const [searchValue, setSearchValue] = useState("");
    const navigate = useNavigate();

    const changeLanguage = (lng) => i18n.changeLanguage(lng);

    useEffect(() => {
        if (!authToken) {
            const storedCart = localStorage.getItem("shoppingCart");
            if (storedCart) setShoppingCart(JSON.parse(storedCart));
        } else {
            setShoppingCart(contextShoppingCart);
        }
    }, [authToken, contextShoppingCart]);

    const links = [
        { to: "users/mySpace", label: "MY SPACE" },
        { to: "admin/products/store", label: "BEST SELLERS" },
        { to: "admin/orders", label: "WHO ARE WE" },
    ];

    const handleScroll = () => {
        setHeaderVisible(window.scrollY < lastScrollY);
        setLastScrollY(window.scrollY);
    };

    useEffect(() => {
        let timeout = null;
        const handleScroll = () => {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                setHeaderVisible(window.scrollY < lastScrollY);
                setLastScrollY(window.scrollY);
            }, 100);
        };
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, [lastScrollY]);

    const calculateTotalPrice = () => {
        return shoppingCart.items?.reduce((total, item) => total + item.product.price * item.quantity, 0) || 0;
    };

    const handleSearchByIdentifier = async ({ identifier }) => {

        if (searchValue.trim() === '') {
            alert('Por favor, escribe un valor para buscar.');
            return;
        }

        try {
            const response = await fetch(`/api/orders/by-identifier?identifier=${encodeURIComponent(identifier)}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                }
            });

            if (!response.ok) throw new Error(`Error ${response.status}: ${await response.text()}`);

            const order = await response.json();
            setSearchPanelOpen(false);
            navigate('/orders/by-identifier', { state: { order } });

        } catch (error) {
            alert("Ocurrió un error: " + error.message);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === "Enter") {
            handleSearchByIdentifier({ identifier: searchValue });
        }
    };


    const handleRedirectToPayment = async () => {
        try {
            const response = await fetch("/api/payment_process/checkout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${authToken}`,
                },
                body: JSON.stringify(shoppingCart.items),
            });

            if (!response.ok) throw new Error(`Error ${response.status}: ${await response.text()}`);

            const data = await response.text();
            window.location.href = data;
            setShoppingCart({ items: [] });
            localStorage.removeItem("shoppingCart");
        } catch (error) {
            alert("Ocurrió un error: " + error);
        }
    };

    return (
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
                top: isHeaderVisible && !isLeftPanelOpen && !isCartPanelOpen && !isSearchPanelOpen ? "0" : "-80px",
            }}
        >
            <div className="grid grid-cols-3 w-full">
                <div className="col-span-1">
                    <div onClick={() => setLeftPanelOpen(true)} style={{ cursor: "pointer" }}>
                        <FontAwesomeIcon icon={faBars} size="xl" />
                    </div>
                </div>
                <div className="col-span-1 flex justify-center items-center">
                    <Link to="/"><p className="custom-font-shop custom-font-shop-black">ARTISTS HEAVEN</p></Link>
                </div>
                <div className="col-span-1 flex justify-end space-x-4">
                    <button onClick={() => changeLanguage('es')}><span className="fi fi-es"></span></button>
                    <button onClick={() => changeLanguage('en')}><span className="fi fi-gb"></span></button>

                    {!authToken && (
                        <Link to="/auth/login">
                            <FontAwesomeIcon icon={faUser} />
                        </Link>
                    )}
                    <div onClick={() => setCartPanelOpen(true)} style={{ cursor: "pointer" }}>
                        <FontAwesomeIcon icon={faBagShopping} />
                    </div>
                    <div onClick={() => setSearchPanelOpen(true)} style={{ cursor: "pointer" }}>
                        <FontAwesomeIcon icon={faSearch} />
                    </div>
                </div>
            </div>

            {/* Sidebar Izquierdo (Menú) */}
            <SlidingPanel isOpen={isLeftPanelOpen} position="left" onClose={() => setLeftPanelOpen(false)} maxWidth="500px">
                <h3 className="p-4 custom-font-shop custom-font-shop-black" style={{ borderBottom: "1px solid #e5e7eb" }}>
                    ARTISTS HEAVEN
                </h3>
                <div className="p-4 custom-font-footer text-3xl" style={{ color: "black" }}>
                    {links
                        .filter(link => authToken || !["MY ORDERS", "MY PROFILE", "MY SPACE"].includes(link.label))
                        .map((link, index) => (
                            <Link to={link.to} key={index} onClick={() => setLeftPanelOpen(false)}>
                                <p>{link.label}</p>
                            </Link>
                        ))}
                    {authToken && <Logout />}
                </div>
            </SlidingPanel>

            {/* Sidebar Derecho (Carrito) */}
            <SlidingPanel isOpen={isCartPanelOpen} onClose={() => setCartPanelOpen(false)}>
                <h3 className="p-4 custom-font-shop custom-font-shop-black">{t('ShoppingCart')}</h3>
                {shoppingCart.items?.length > 0 ? (
                    shoppingCart.items.map((item, index) => (
                        <div key={index} className="mb-4 border-b p-4">
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <div style={{ display: 'flex' }}>
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
                                <div style={{ border: '1px solid black', padding: '4px 8px', borderRadius: '4px', minWidth: '40px', textAlign: 'center', fontWeight: 'bold' }}>
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
                {shoppingCart.items?.length > 0 && (
                    <>
                        <div className="p-4 flex justify-between">
                            <p className="custom-font-shop custom-font-shop-black">Total</p>
                            <p className="custom-font-shop custom-font-shop-black">{calculateTotalPrice()}€</p>
                        </div>
                        <div className="p-4 border border-black flex justify-center cursor-pointer" onClick={handleRedirectToPayment}>
                            <p className="custom-font-shop custom-font-shop-black">Finalizar Compra</p>
                        </div>
                    </>
                )}
            </SlidingPanel>

            {/* Sidebar Derecho (Búsqueda) */}
            <SlidingPanel isOpen={isSearchPanelOpen} onClose={() => setSearchPanelOpen(false)}>
                <h3 className="p-4 custom-font-shop custom-font-shop-black">Buscar</h3>
                <div className="p-4 flex items-center gap-2">
                    <input
                        type="number"
                        placeholder="Buscar pedidos..."
                        className="flex-1 border p-2 rounded"
                        value={searchValue}
                        onChange={(e) => setSearchValue(e.target.value)}
                        onKeyDown={handleKeyDown}
                        required
                    />
                    <button
                        onClick={() => handleSearchByIdentifier({ identifier: searchValue })}
                        className=" text-black px-4 py-2 rounded"
                    >
                        <FontAwesomeIcon icon={faSearch} />
                    </button>
                </div>
            </SlidingPanel>
        </div>
    );
};

export default Header;
