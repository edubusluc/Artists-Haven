import React, { useContext, useState, useEffect, useMemo, useCallback, lazy, Suspense } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBars, faBagShopping, faSearch, faUser } from "@fortawesome/free-solid-svg-icons";
import { CartContext } from "../context/CartContext";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import Logout from "./Logout";
import { checkTokenExpiration } from "../utils/authUtils";
import { HashLink } from 'react-router-hash-link';

// Lazy load de paneles para optimizar carga inicial
const SlidingPanel = lazy(() => import("../utils/SlidingPanel"));

const Header = () => {
  const { shoppingCart, setShoppingCart, handleDeleteProduct } = useContext(CartContext);
  const authToken = useMemo(() => localStorage.getItem("authToken"), []);
  const [isLeftPanelOpen, setLeftPanelOpen] = useState(false);
  const [isCartPanelOpen, setCartPanelOpen] = useState(false);
  const [isSearchPanelOpen, setSearchPanelOpen] = useState(false);
  const { t, i18n } = useTranslation();
  const language = i18n.language;

  const [expandedSearchSection, setExpandedSearchSection] = useState(null);
  const [searchPedidoValue, setSearchPedidoValue] = useState("");
  const [searchProductoValue, setSearchProductoValue] = useState("");

  const [promotedCollections, setPromotedCollections] = useState([]);
  const [activeRewardCard, setActiveRewardCard] = useState(null);

  const [windowWidth, setWindowWidth] = useState(window.innerWidth);
  const isMobile = windowWidth <=502;


  // states nuevos para controlar transiciones y primer scroll
  const [headerLevel, setHeaderLevel] = useState("full");
  const [lastScrollY, setLastScrollY] = useState(0);
  const [initialScrollDone, setInitialScrollDone] = useState(false);

  const navigate = useNavigate();

  const totalProducts = useMemo(() =>
    shoppingCart.items?.reduce((total, item) => total + item.quantity, 0) || 0,
    [shoppingCart.items]
  );

  useEffect(() => {
    const handleResize = () => setWindowWidth(window.innerWidth);

    window.addEventListener("resize", handleResize);

    // Cleanup
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  // Fetch colecciones solo una vez
  useEffect(() => {
    const fetchCollections = async () => {
      try {
        const response = await fetch('/api/product/promoted-collections');
        if (!response.ok) throw new Error(`Error ${response.status}: ${await response.text()}`);
        const responseCollections = await response.json();
        setPromotedCollections(responseCollections.data);
      } catch (error) {
        console.error("Error al obtener las colecciones:", error);
      }
    };
    fetchCollections();
  }, []);

  // Manejo del carrito dependiendo del auth
  useEffect(() => {
    if (!authToken) {
      const storedCart = localStorage.getItem("shoppingCart");
      if (storedCart) setShoppingCart(JSON.parse(storedCart));
    }
  }, [authToken, setShoppingCart]);


  useEffect(() => {
    const openCartHandler = () => setCartPanelOpen(true);
    window.addEventListener("openCartPanel", openCartHandler);
    return () => window.removeEventListener("openCartPanel", openCartHandler);
  }, []);

  // Nuevo handleScroll con lógica para el primer scroll
  useEffect(() => {
    const handleScroll = () => {
      const currentScrollY = window.scrollY;

      // Si todavía no hemos hecho el primer desplazamiento significativo, lo tratamos como 'primer scroll'
      if (!initialScrollDone && currentScrollY > 0) {
        // primer scroll: forzamos el cambio inmediato a 'middle' SIN transiciones
        setInitialScrollDone(true);
        setHeaderLevel("middle");
        setLastScrollY(currentScrollY);
        return;
      }

      if (currentScrollY === 0) {
        setHeaderLevel("full");
      } else if (currentScrollY > lastScrollY) {
        // Scroll hacia abajo
        setHeaderLevel((prev) => (prev === "full" ? "middle" : "hidden"));
      } else {
        // Scroll hacia arriba
        setHeaderLevel("middle");
      }

      setLastScrollY(currentScrollY);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [lastScrollY, initialScrollDone]);

  //Obtener tarjeta
  useEffect(() => {
    if (checkTokenExpiration()) {
      const fetchRewardCard = async () => {
        try {
          const res = await fetch("/api/reward-cards/my", {
            headers: { "Authorization": `Bearer ${authToken}` }
          });
          const result = await res.json();
          const cards = result.data;

          // Suponiendo que tomamos la primera tarjeta no canjeada
          const activeCard = cards.find(card => !card.redeemed);
          setActiveRewardCard(activeCard || null);
        } catch (err) {
          console.error("Error fetching reward cards", err);
        }
      };
      fetchRewardCard();
    }
  }, [authToken]);

  const changeLanguage = useCallback((lng) => i18n.changeLanguage(lng), [i18n]);

  const toggleLeftPanel = useCallback(() => setLeftPanelOpen(prev => !prev), []);
  const toggleCartPanel = useCallback(() => setCartPanelOpen(prev => !prev), []);
  const toggleSearchPanel = useCallback(() => setSearchPanelOpen(prev => !prev), []);

  const toggleSearchSection = useCallback((section) => {
    setExpandedSearchSection(prev => prev === section ? null : section);
  }, []);

  const links = useMemo(() => [
    { to: "users/mySpace", label: "MY SPACE" },
    { to: "shop", label: t('all') },
    { to: "shop/camisetas", label: t('t-shirts') },
    { to: "shop/pantalones", label: t('pants') },
    { to: "shop/sudaderas", label: t('hoodies') },
    { to: "shop/accesorios", label: t('accessories') },
    { to: "forFan", label: t('header.forFan') },
    { to: "/#upcoming-events", label: t('upCommingEvents') }
  ], [t]);



  const totalPrice = useMemo(() =>
    shoppingCart.items?.reduce((total, item) => total + item.product.price * item.quantity, 0) || 0,
    [shoppingCart.items]
  );

  const handleSearchByIdentifier = async ({ identifier }) => {
    if (identifier.trim() === '') return alert('Por favor, escribe un valor para buscar.');
    try {
      const response = await fetch(`/api/orders/by-identifier?identifier=${encodeURIComponent(identifier)}&lang=${language}`);

      const result = await response.json();
      const errorMessage = result.message;
      const order = result.data;

      if (!response.ok) {
        return alert(errorMessage);

      }
      setSearchPanelOpen(false);
      navigate('/orders/by-identifier', { state: { order } });
    } catch (error) {
      alert("Ocurrió un error: " + error.message);
    }
  };

  const handleSearchProduct = async ({ reference }) => {
    if (reference.trim() === '') return alert('Por favor, escribe un valor para buscar.');
    try {
      const response = await fetch(`/api/product/by-reference?reference=${encodeURIComponent(reference)}&lang=${language}`);

      const result = await response.json();
      const errorMessage = result.message;

      if (!response.ok) {
        return alert(errorMessage);
      }

      setSearchPanelOpen(false);
      navigate(`/product/details/${result.data}`);
    } catch (error) {
      alert("Ocurrió un error: " + error.message);
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
      alert(error);
    }
  };

  const discountedTotalPrice = useMemo(() => {
    if (!activeRewardCard) return totalPrice;
    const discount = (totalPrice * activeRewardCard.discountPercentage) / 100;
    return totalPrice - discount;
  }, [totalPrice, activeRewardCard]);


  return (
    <>
      {headerLevel === "full" && (
        <div
          className="envio-gratis-bar"
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            backgroundColor: "#002547",
            color: "white",
            height: "35px",
            display: "flex",
            alignItems: "center",
            overflow: "hidden",
            zIndex: 1100,
          }}
        >
          <div className="envio-gratis-text custom-font-footer text-sm">
            <span style={{ marginRight: "1rem" }}>{t("header.customText")}</span>
            <span style={{ marginRight: "1rem" }}>•</span>
            <span style={{ marginRight: "1rem" }}>{t("header.customText")}</span>
            <span style={{ marginRight: "1rem" }}>•</span>
            <span style={{ marginRight: "1rem" }}>{t("header.customText")}</span>
          </div>
        </div>
      )}

      {/* Barra central */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          padding: "10px 20px",
          backgroundColor: "white",
          boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
          justifyContent: "space-around",
          position: "fixed",
          top:
            headerLevel === "hidden"
              ? "-80px"
              : headerLevel === "middle"
                ? "0"
                : "35px",
          left: 0,
          right: 0,
          zIndex: 1000,
        }}
      >
        {/* HeaderBar */}
        <div className="grid grid-cols-3 w-full">
          <div className="col-span-1 flex justify-left items-center">
            <div onClick={toggleLeftPanel} style={{ cursor: "pointer" }}>
              <FontAwesomeIcon icon={faBars} /><span className="inter-400 text-sm"> Menu</span>
            </div>
          </div>
          <div className="col-span-1 flex justify-center items-center">
            <Link to="/"><p className="logo-font">Artists’ Heaven</p></Link>
          </div>
          <div className="col-span-1 flex justify-end gap-2 sm:gap-4 items-center">
            {/* Idiomas */}
            <div className="sm:hidden">
              {language === 'es' ? (
                <button onClick={() => changeLanguage('en')}><span className="fi fi-gb"></span></button>
              ) : (
                <button onClick={() => changeLanguage('es')}><span className="fi fi-es"></span></button>
              )}
            </div>
            <div className="hidden sm:flex gap-4">
              <button onClick={() => changeLanguage('es')}><span className="fi fi-es"></span></button>
              <button onClick={() => changeLanguage('en')}><span className="fi fi-gb"></span></button>
            </div>

            {!authToken && (
              <Link to="/auth/login">
                <FontAwesomeIcon icon={faUser} />
              </Link>
            )}
            {authToken && (
              <Link to="/users/mySpace">
                <FontAwesomeIcon icon={faUser} />
              </Link>
            )}
            <div style={{ position: "relative", cursor: "pointer" }} onClick={toggleCartPanel}>
              <FontAwesomeIcon icon={faBagShopping} />
              {totalProducts > 0 && (
                <span
                  style={{
                    position: "absolute",
                    top: "-8px",
                    right: "-8px",
                    backgroundColor: "red",
                    color: "white",
                    borderRadius: "50%",
                    padding: "2px 6px",
                    fontSize: "12px",
                    fontWeight: "bold",
                    lineHeight: "1",
                  }}
                >
                  {totalProducts}
                </span>
              )}
            </div>
            <div onClick={toggleSearchPanel} style={{ cursor: "pointer" }}>
              <FontAwesomeIcon icon={faSearch} />
            </div>
          </div>
        </div>
      </div>

      {headerLevel === "full" && (
        <div
          className={`
    fixed left-0 right-0 h-[45px] bg-white border-b border-gray-300 z-[900]
    flex justify-center items-center transition-all
  `}
          style={{
            top: isMobile
              ? "100px" // en móviles siempre 100px
              : headerLevel === "hidden"
                ? "-80px" // oculto junto a la barra central
                : headerLevel === "middle"
                  ? "35px" // justo debajo de la barra central
                  : "70px", // cuando está full, debajo de barra superior + central
          }}
        >
          {/* Aquí pondrías tu navegación */}
          <nav className="flex gap-6 text-blue-950 text-sm font-bold overflow-x-auto scrollbar-hide mt-2">
            <Link to="/shop">{t("all")}</Link>
            <Link to="/shop/camisetas">{t("t-shirts")}</Link>
            <Link to="/shop/pantalones">{t("pants")}</Link>
            <Link to="/shop/sudaderas">{t("hoodies")}</Link>
            <Link to="/shop/accesorios">{t("accessories")}</Link>
            <Link to="/forFan">FORFAN</Link>
            <Link to="/artists">ARTISTS</Link>
          </nav>
        </div>
      )}

      {/* Paneles con Suspense */}
      <Suspense fallback={<div>Cargando...</div>}>
        {/* Menú lateral */}
        <SlidingPanel isOpen={isLeftPanelOpen} position="left" onClose={toggleLeftPanel} maxWidth="500px" title="Artists’ Heaven">
          <div className="p-4 custom-font-footer text-3xl inline-block" style={{ color: "black" }}>
            {promotedCollections.length > 0 ? (
              promotedCollections.map((collection, index) => (
                <Link key={index} to={`/shop/${collection.name}`} onClick={toggleLeftPanel}>
                  <p className="transform origin-center hover:scale-110 transition-transform duration-300 ease-in-out">{collection.name}</p>
                </Link>
              ))
            ) : <p>{t('header.noColectionsAvailable')}</p>}

            {links
              .filter(link => authToken || !["MY ORDERS", "MY PROFILE", "MY SPACE"].includes(link.label))
              .filter(link => link.label !== "MY SPACE")
              .map((link, index) => (
                link.to.startsWith("/#") ? (
                  <HashLink smooth to={link.to} key={index} onClick={toggleLeftPanel}>
                    <p className="transform origin-center hover:scale-110 transition-transform duration-300 ease-in-out">
                      {link.label}
                    </p>
                  </HashLink>
                ) : (
                  <Link to={link.to} key={index} onClick={toggleLeftPanel}>
                    <p className="transform origin-center hover:scale-110 transition-transform duration-300 ease-in-out">
                      {link.label}
                    </p>
                  </Link>
                )
              ))}

            {authToken && (
              <>
                <hr className="my-2 border-gray-300" />
                {links.filter(link => link.label === "MY SPACE").map((link, index) => (
                  <Link to={link.to} key={index} onClick={toggleLeftPanel}>
                    <p>{link.label}</p>
                  </Link>
                ))}
              </>
            )}
            {authToken && <Logout />}
          </div>
        </SlidingPanel>

        {/* Carrito */}
        <SlidingPanel isOpen={isCartPanelOpen} onClose={toggleCartPanel} title={t('Cart')}>
          {shoppingCart.items?.length > 0 ? (
            shoppingCart.items.map((item, index) => (
              <div key={index} className="mb-4 border-b p-4">
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <div style={{ display: 'flex' }}>
                    <img
                      src={`/api/product${item.imageUrl}`}
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
                      {item.product.section === "ACCESSORIES" ? (
                        <p className="custom-font-shop-regular custom-font-shop-black">{item.product.section}</p>
                      ) : (
                        <p className="custom-font-shop-regular custom-font-shop-black">{t('header.size')}: {item.size}</p>
                      )}
                    </div>
                  </div>
                  <div
                    style={{
                      border: '1px solid black',
                      padding: '4px 8px',
                      borderRadius: '4px',
                      minWidth: '40px',
                      height: '32px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 'bold',
                      lineHeight: '1',
                    }}
                  >
                    {item.quantity}
                  </div>
                </div>
                <button onClick={() => handleDeleteProduct(index)} style={{ marginTop: '12px' }}>
                  <p className="custom-font-shop-regular custom-font-shop-black">{t('header.deleteProduct')}</p>
                </button>
              </div>
            ))
          ) : (
            <p className="p-4 custom-font-shop-regular custom-font-shop-regular-black">{t('AddAProductToShopping')}</p>
          )}
          {shoppingCart.items?.length > 0 && (
            <>

              <div className="p-4 space-y-2">
                {activeRewardCard && (
                  <><div className="flex justify-between">
                    <p className="custom-font-shop custom-font-shop-black">Subtotal</p>
                    <p className="custom-font-shop custom-font-shop-black">{totalPrice}€</p>
                  </div><div className="flex justify-between text-green-700 font-semibold">
                      <p>{`${t('header.discount')} (${activeRewardCard.discountPercentage}%)`}</p>
                      <p>-{(totalPrice * activeRewardCard.discountPercentage / 100).toFixed(2)}€</p>
                    </div></>
                )}

                <div className="flex justify-between font-bold">
                  <p>Total</p>
                  <p>{discountedTotalPrice.toFixed(2)}€</p>
                </div>

                <div className="p-4 border border-black flex justify-center cursor-pointer" onClick={handleRedirectToPayment}>
                  <p className="custom-font-shop custom-font-shop-black">{t('header.completePurchase')}</p>
                </div>
              </div>
            </>
          )}
        </SlidingPanel>

        {/* Búsqueda */}
        <SlidingPanel isOpen={isSearchPanelOpen} onClose={toggleSearchPanel} title={t('Search panel')}>
          <div onClick={() => toggleSearchSection("pedido")} className="pb-4 mt-4 cursor-pointer flex justify-between items-center border-b border-gray-300 custom-font-shop custom-font-shop-black">
            <h3>{t('header.searchOrder')}</h3>
            <span>{expandedSearchSection === "pedido" ? "▲" : "▼"}</span>
          </div>
          <div className={`transition-all duration-300 ease-in-out overflow-hidden ${expandedSearchSection === "pedido" ? "max-h-[200px] opacity-100" : "max-h-0 opacity-0"}`}>
            <div className="p-4 flex items-center gap-2">
              <input
                type="number"
                placeholder={t('header.referencePlaceholder')}
                className="flex-1 border p-2 rounded"
                value={searchPedidoValue}
                onChange={(e) => setSearchPedidoValue(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearchByIdentifier({ identifier: searchPedidoValue })}
              />
              <button onClick={() => handleSearchByIdentifier({ identifier: searchPedidoValue })} className="border px-4 py-2 rounded bg-gray-300 hover:bg-gray-400">
                {t('header.search')}
              </button>
            </div>
          </div>

          <div onClick={() => toggleSearchSection("producto")} className="pt-4 pb-4 cursor-pointer flex justify-between items-center border-b border-gray-300 custom-font-shop custom-font-shop-black">
            <h3>{t('header.searchProduct')}</h3>
            <span>{expandedSearchSection === "producto" ? "▲" : "▼"}</span>
          </div>
          <div className={`transition-all duration-300 ease-in-out overflow-hidden ${expandedSearchSection === "producto" ? "max-h-[200px] opacity-100" : "max-h-0 opacity-0"}`}>
            <div className="p-4 flex items-center gap-2">
              <input
                type="text"
                placeholder={t('header.productPlaceholder')}
                className="flex-1 border p-2 rounded"
                value={searchProductoValue}
                onChange={(e) => setSearchProductoValue(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearchProduct({ reference: searchProductoValue })}
              />
              <button onClick={() => handleSearchProduct({ reference: searchProductoValue })} className="border px-4 py-2 rounded bg-gray-300 hover:bg-gray-400">
                {t('header.search')}
              </button>
            </div>
          </div>
        </SlidingPanel>
      </Suspense>
    </>
  );
};

export default React.memo(Header);
