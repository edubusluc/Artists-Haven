import { useState, useEffect, useContext, useMemo, useCallback, useRef } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { CartContext } from "../../context/CartContext";
import Footer from "../Footer";
import { useTranslation } from "react-i18next";
import AddReviewModal from "./AddReviewModal";
import ProductCard from "./ProductCard";
import guiaCamiseta from "../../util-image/guiaCamiseta.png";
import guiaPantalon from "../../util-image/guiaPantalon.png";
import guiaSudadera from "../../util-image/guiaSudadera.png";
import ReviewsModal from "./ReviewsModal";
import MoreInfoSlide from "./MoreInfoSlide";
import { checkTokenExpiration } from "../../utils/authUtils";
import SessionExpired from "../SessionExpired";
import ProductAR from "./ProductAR";


// ✅ Componente para mostrar imágenes
const ProductImages = ({ images, name }) => (
    <>
        {/* Mobile */}
        <div className="lg:hidden flex overflow-x-auto space-x-4 px-6 py-4 scroll-snap-x snap-mandatory">
            {images?.map((image, index) => (
                <div key={index} style={{ flexShrink: 0 }}>
                    <img
                        src={`/api/product${image}`}
                        alt={name}
                        className="max-w-full max-h-[300px] object-contain"
                        loading="lazy"
                    />
                </div>
            ))}
        </div>
        {/* Desktop */}
        <div className="hidden lg:grid grid-cols-2 mt-10 auto-rows-auto gap-4">
            {images?.map((image, index) => (
                <ZoomableImage
                    key={index}
                    src={`/api/product${image}`}
                    alt={name}
                />
            ))}
        </div>
    </>
);

// ✅ Componente para seleccionar tallas
const SizeSelector = ({ sizes, selectedSize, onSelect, sizeOrder, t }) => {
    const availableSizes = useMemo(
        () =>
            Object.entries(sizes || {})
                .filter(([size, quantity]) => quantity > 0 && sizeOrder.includes(size))
                .sort(([a], [b]) => sizeOrder.indexOf(a) - sizeOrder.indexOf(b)),
        [sizes, sizeOrder]
    );

    if (availableSizes.length === 0) {
        return (
            <p className="inter-400 text-sm flex items-center gap-2 text-red-500">
                🚫 {t("productDetails.ProductOutOfStock")}
            </p>
        );
    }

    return (
        <div className="flex flex-wrap gap-2">
            {availableSizes.map(([size]) => (
                <label
                    key={size}
                    className={`border border-gray-200 cursor-pointer w-14 h-10 text-sm text-center font-medium flex items-center justify-center ${selectedSize === size
                        ? "bg-yellow-400 text-white hover:bg-yellow-500 transition"
                        : "bg-white bold-arial hover:bg-gray-200 transition"
                        }`}
                >
                    <input
                        type="radio"
                        name="size"
                        value={size}
                        className="hidden"
                        onChange={(e) => onSelect(e.target.value)}
                    />
                    {size}
                </label>
            ))}
        </div>
    );
};

const ZoomableImage = ({ src, alt }) => {
    const [zoom, setZoom] = useState(false);
    const [pos, setPos] = useState({ x: 50, y: 50 });
    const containerRef = useRef(null);

    const handleMouseMove = (e) => {
        const { left, top, width, height } = containerRef.current.getBoundingClientRect();
        const x = ((e.pageX - left) / width) * 100;
        const y = ((e.pageY - top) / height) * 100;
        setPos({ x, y });
    };

    return (
        <div
            ref={containerRef}
            className="relative w-full aspect-[4/5]"
            onMouseEnter={() => setZoom(true)}
            onMouseLeave={() => setZoom(false)}
            onMouseMove={handleMouseMove}
            style={{
                backgroundImage: `url(${src})`,
                backgroundSize: zoom ? "200%" : "contain",
                backgroundPosition: `${pos.x}% ${pos.y}%`,
                backgroundRepeat: "no-repeat",
            }}
            aria-label={alt}
        />
    );
};

// ✅ Función para renderizar estrellas
const renderStars = (averageRating) => {
    const rating = parseFloat(averageRating);
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating - fullStars >= 0.25 && rating - fullStars < 0.75;

    return Array.from({ length: 5 }, (_, index) => {
        if (index < fullStars) {
            return (
                <svg
                    key={index}
                    xmlns="http://www.w3.org/2000/svg"
                    fill="#facc15"
                    viewBox="0 0 24 24"
                    width="15"
                    height="15"
                >
                    <path d="M12 .587l3.668 7.571L24 9.748l-6 5.849L19.335 24 12 19.897 4.665 24 6 15.597 0 9.748l8.332-1.59z" />
                </svg>
            );
        } else if (index === fullStars && hasHalfStar) {
            return (
                <svg
                    key={index}
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    width="15"
                    height="15"
                >
                    <defs>
                        <linearGradient id={`half-grad-${index}`}>
                            <stop offset="50%" stopColor="#facc15" />
                            <stop offset="50%" stopColor="#e5e7eb" />
                        </linearGradient>
                    </defs>
                    <path
                        fill={`url(#half-grad-${index})`}
                        d="M12 .587l3.668 7.571L24 9.748l-6 5.849L19.335 24 12 19.897 4.665 24 6 15.597 0 9.748l8.332-1.59z"
                    />
                </svg>
            );
        } else {
            return (
                <svg
                    key={index}
                    xmlns="http://www.w3.org/2000/svg"
                    fill="#e5e7eb"
                    viewBox="0 0 24 24"
                    width="15"
                    height="15"
                >
                    <path d="M12 .587l3.668 7.571L24 9.748l-6 5.849L19.335 24 12 19.897 4.665 24 6 15.597 0 9.748l8.332-1.59z" />
                </svg>
            );
        }
    });
};



const ProductDetails = () => {
    const [product, setProduct] = useState({});
    const { id } = useParams();
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { shoppingCart, setShoppingCart } = useContext(CartContext);
    const [selectedSize, setSelectedSize] = useState("");
    const navigate = useNavigate();
    const [reviews, setReviews] = useState([]);
    const { t } = useTranslation();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [relatedProducts, setRelatedProducts] = useState([]);
    const [showSizeGuide, setShowSizeGuide] = useState(false);
    const [isReviewsModalOpen, setIsReviewsModalOpen] = useState(false);
    const [isCompositionSlideOpen, setIsCompositionSlideOpen] = useState(false);
    const [isShippingSlideOpen, setIsShippingSlideOpen] = useState(false);
    const [added, setAdded] = useState(false);
    const [isClicked, setIsClicked] = useState(false);
    const [sessionExpired, setSessionExpired] = useState(false);
    const sizeOrder = ["XS", "S", "M", "L", "XL", "XXL"];
    const [isAROpen, setIsAROpen] = useState(false);

    const averageRating = useMemo(() => {
        if (reviews.length === 0) return null;
        return (
            reviews.reduce((sum, review) => sum + review.score, 0) / reviews.length
        ).toFixed(1);
    }, [reviews]);

    // ✅ Fetch datos del producto y relacionados
    useEffect(() => {
        const fetchProductDetails = async () => {
            try {
                setLoading(true);
                const [productRes, reviewsRes] = await Promise.all([
                    fetch(`/api/product/details/${id}`),
                    fetch(`/api/rating/productReview/${id}`)
                ]);

                if (!productRes.ok) throw new Error("Error al obtener el producto");
                const productData = await productRes.json();
                productData.data.categories = productData.data.categories.map((c) => c.id);
                setProduct(productData.data);

                const relatedRes = await fetch(
                    `/api/product/related?section=${encodeURIComponent(
                        productData.data.section
                    )}&id=${encodeURIComponent(productData.data.id)}`
                );
                if (relatedRes.ok) {
                    const relatedData = await relatedRes.json();
                    setRelatedProducts(relatedData.data);
                }

                if (reviewsRes.ok) {
                    const reviewsData = await reviewsRes.json();
                    setReviews(reviewsData.data);
                }
            } catch (error) {
                console.error(error);
            } finally {
                setLoading(false);
            }
        };

        fetchProductDetails();
        window.scrollTo(0, 0);
    }, [id]);

    const handleAddProduct = useCallback(async () => {
        // 🔹 Si NO es accesorio, seguir pidiendo talla
        if (product.section !== "ACCESSORIES" && !selectedSize) {
            alert(t('shoppinCart.selectSizeFirst'));
            return;
        }

        let availableStock;
        let alreadyInCart;

        if (product.section === "ACCESSORIES") {
            // Para accesorios, usamos availableUnits
            availableStock = product.availableUnits;
            alreadyInCart =
                shoppingCart?.items
                    ?.filter(item => item.product.id === product.id)
                    .reduce((sum, item) => sum + item.quantity, 0) || 0;
        } else {
            // Para ropa, usamos tallas
            availableStock = product.sizes[selectedSize];
            alreadyInCart =
                shoppingCart?.items
                    ?.filter(item => item.product.id === product.id && item.size === selectedSize)
                    .reduce((sum, item) => sum + item.quantity, 0) || 0;
        }

        if (alreadyInCart >= availableStock) {
            alert("No puedes añadir más unidades. Stock insuficiente.");
            return;
        }

        setAdded(true);
        setTimeout(() => setAdded(false), 500);

        const endpoint = authToken
            ? `/api/myShoppingCart/addProducts`
            : `/api/myShoppingCart/addProductsNonAuthenticate`;

        const payload = authToken
            ? { productId: product.id, ...(product.section !== "ACCESSORIES" && { size: selectedSize }) }
            : { shoppingCart, productId: product.id, ...(product.section !== "ACCESSORIES" && { size: selectedSize }) };


        try {
            const response = await fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(authToken && { Authorization: `Bearer ${authToken}` })
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error(`Error al añadir el producto al carrito: ${response.status}`);
            }

            const data = await response.json();
            setShoppingCart(data);

            const event = new CustomEvent("openCartPanel");
            window.dispatchEvent(event);
        } catch (error) {
            console.error("Error al añadir el producto al carrito:", error);
        }
    }, [selectedSize, product, shoppingCart, authToken, setShoppingCart]);

    const handleAddProductThrottled = () => {
        if (!checkTokenExpiration()) {
            setSessionExpired(true);
            return;
        }
        if (isClicked) return;

        setIsClicked(true);
        handleAddProduct();

        setTimeout(() => setIsClicked(false), 500);
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 border-solid mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">{t('productDetails.loadInfo')}</p>
                </div>
            </div>
        );
    }

    if (sessionExpired) {
        return <SessionExpired />;
    }

    console.log(product);

    return (
        <>
            <div className="p-6 bg-white">
                <div className="grid grid-cols-1 lg:grid-cols-2">
                    {/* Columna 1 */}
                    <div className="mt-10">
                        <ProductImages images={product.images} name={product.name} />
                    </div>

                    {/* Columna 2 */}
                    <div className="flex flex-col items-center justify-start sm:p-6 md:p-0 w-full mt-10">
                        <div className="w-full lg:w-1/2 sticky top-20 mt-10">
                            {/* Nombre + Precio */}
                            <div className="text-black space-y-2">
                                <p className="custom-font-footer-black text-lg mb-2">{product.name}</p>
                                {product.on_Promotion && product.discount > 0 ? (
                                    <>
                                        <span className="inline-block p-1 bg-red-500 inter-400 text-xs text-white">
                                            {`DESCUENTO ${product.discount}%`}
                                        </span>
                                        <div className="flex items-center gap-2">
                                            <span
                                                className="inter-400 text-sm line-through"
                                                style={{ color: "#909497" }}
                                            >
                                                {(
                                                    product.price /
                                                    ((100 - product.discount) / 100)
                                                ).toFixed(2)}
                                                €
                                            </span>
                                            <span className="inter-400 text-sm text-red-600">
                                                {product.price ? product.price.toFixed(2) : "Precio no disponible"}€
                                            </span>
                                        </div>
                                    </>
                                ) : (
                                    <span className="inter-400 text-base">
                                        {product.price ? product.price.toFixed(2) : "Precio no disponible"}€
                                    </span>
                                )}
                                <p className="inter-400 text-sm">
                                    {t("Reference")}: {product.reference}
                                </p>
                                <hr className="border-t border-black my-4 w-full" />
                            </div>

                            {/* Descripción */}
                            <div className="text-black w-full mx-auto mt-2">
                                <p className="inter-400 text-sm">{product.description}</p>
                                <hr className="border-t border-black my-4 w-full" />
                            </div>

                            {/* Selector de tallas */}
                            {product.section !== "ACCESSORIES" && (
                                <div className="text-black w-full mx-auto">
                                    {selectedSize && <p className="inter-400 text-sm mb-2">{t('productDetails.size')}: {selectedSize}</p>}
                                    <SizeSelector
                                        sizes={product.sizes}
                                        selectedSize={selectedSize}
                                        onSelect={setSelectedSize}
                                        sizeOrder={sizeOrder}
                                        t={t}
                                    />
                                    <hr className="border-t border-black my-4 w-full" />
                                </div>
                            )}

                            {/* Añadir a carrito */}
                            <div className="text-black w-full mx-auto">
                                {!product.available && (
                                    <p className="text-red-600 font-semibold mb-2">{t("NotAvailable")}</p>
                                )}
                                <button
                                    onClick={handleAddProductThrottled}
                                    disabled={
                                        !product.available ||
                                        (
                                            product.section === "ACCESSORIES"
                                                ? product.availableUnits <= 0
                                                : !product.sizes || Object.entries(product.sizes).every(([_, qty]) => qty === 0)
                                        )
                                    }
                                    className={`w-full py-4 px-4 ${added ? "added" : ""}
        ${product.available &&
                                            (
                                                product.section === "ACCESSORIES"
                                                    ? product.availableUnits > 0
                                                    : product.sizes && Object.values(product.sizes).some(qty => qty > 0)
                                            )
                                            ? "button-custom cursor-pointer"
                                            : "bg-gray-300 text-gray-600 cursor-not-allowed"
                                        }`}
                                >
                                    <p className="inter-400 text-base">{t("AddToShoppingCard")}</p>
                                </button>

                                {/* Botón reseña */}
                                <div className="w-full mx-auto">
                                    <button
                                        onClick={() => setIsModalOpen(true)}
                                        className="w-full mt-2 bg-black py-2 px-4 border-none cursor-pointer"
                                    >
                                        <p className="text-white inter-400 text-sm">{t("AddReview")}</p>
                                    </button>
                                </div>

                                {/* Botón Ver en AR */}
                                {product.modelReference  &&
                                    <div className="w-full mx-auto">
                                        <button
                                            onClick={() => setIsAROpen(true)}
                                            className="w-full mt-2 bg-yellow-400 py-2 px-4 border-none cursor-pointer"
                                        >
                                            <p className="text-black inter-400 text-sm">👓 Ver en AR</p>
                                        </button>
                                    </div>
                                }

                                <hr className="border-t border-black my-4 w-full" />

                                {/* Guía de tallas */}
                                {product.section !== "ACCESSORIES" && (
                                    <>
                                        <button
                                            onClick={() => setShowSizeGuide((prev) => !prev)}
                                            className="w-full border border-black text-black py-2 px-4 cursor-pointer bg-white flex items-center justify-between"
                                        >
                                            <p className="inter-400 text-sm">{t("sizeGuide")}</p>
                                            <span className="text-sm">{showSizeGuide ? "▲" : "▼"}</span>
                                        </button>
                                        <div
                                            className={`transition-all duration-300 ease-in-out overflow-hidden ${showSizeGuide ? "max-h-[500px] opacity-100 mt-4" : "max-h-0 opacity-0"
                                                }`}
                                        >
                                            {product.section === "TSHIRT" && (
                                                <img
                                                    src={guiaCamiseta}
                                                    alt="Guía de tallas - Camiseta"
                                                    className="w-full max-w-md mx-auto"
                                                />
                                            )}
                                            {product.section === "PANTS" && (
                                                <img
                                                    src={guiaPantalon}
                                                    alt="Guía de tallas - Pantalón"
                                                    className="w-full max-w-md mx-auto"
                                                />
                                            )}
                                            {product.section === "HOODIES" && (
                                                <img
                                                    src={guiaSudadera}
                                                    alt="Guía de tallas - Pantalón"
                                                    className="w-full max-w-md mx-auto"
                                                />
                                            )}
                                        </div>
                                        <hr className="border-t border-black my-4 w-full" />
                                    </>
                                )}
                            </div>

                            {/* Enlaces adicionales */}
                            <div className="text-black w-full mx-auto">
                                <p
                                    onClick={() => setIsCompositionSlideOpen(true)}
                                    className="inter-400 text-xs mt-2 underline text-gray-400 cursor-pointer"
                                >
                                    {t("Composition")}
                                </p>
                                <p
                                    onClick={() => setIsShippingSlideOpen(true)}
                                    className="inter-400 text-xs mt-2 underline text-gray-400 cursor-pointer"
                                >
                                    {t('shipping_details')}
                                </p>
                                <hr className="border-t border-black my-2 w-full" />
                            </div>

                            {/* Valoración */}
                            <div className="text-black w-full mx-auto">
                                <p className="inter-400 text-sm mb-2">{t("GeneralRating")}</p>
                                {reviews.length > 0 ? (
                                    <div className="flex items-center gap-3">
                                        <div className="flex gap-1" aria-label={`Valoración media ${averageRating} de 5`}>
                                            {renderStars(averageRating)}
                                        </div>
                                        <div className="flex text-sm text-gray-800 gap-2">
                                            <span className="font-semibold">{averageRating} / 5</span>
                                            <span className="text-gray-500">{reviews.length} {t("reviews")}</span>
                                        </div>
                                    </div>
                                ) : (
                                    <p className="inter-400 text-sm text-gray-500">{t("No reviews")}</p>
                                )}
                                {reviews.length > 0 && (
                                    <p
                                        onClick={() => setIsReviewsModalOpen(true)}
                                        className="inter-400 text-xs mt-2 underline text-gray-400 cursor-pointer"
                                    >
                                        {t("ViewReview")}
                                    </p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Productos relacionados */}
                <div className="flex items-center gap-2 mb-5 mt-5">
                    <p className="">■</p>
                    <p className="inter-400 text-sm">{t('related_products')}</p>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
                    {relatedProducts.map((product, index) => (
                        <Link to={`/product/details/${product.id}`} key={index}>
                            <div className="flex justify-center">
                                <ProductCard product={product} />
                            </div>
                        </Link>
                    ))}
                </div>
            </div>

            <Footer />

            {/* Modales */}
            <ReviewsModal
                isOpen={isReviewsModalOpen}
                onClose={() => setIsReviewsModalOpen(false)}
                reviews={reviews}
            />
            <AddReviewModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                productId={id}
                authToken={authToken}
            />
            <MoreInfoSlide
                isOpen={isCompositionSlideOpen}
                titleSlide={t("Composition")}
                onClose={() => setIsCompositionSlideOpen(false)}
                productDetails={product.composition}
            />
            <MoreInfoSlide
                isOpen={isShippingSlideOpen}
                titleSlide="Envio"
                onClose={() => setIsShippingSlideOpen(false)}
                productDetails={product.shippingDetails}
            />
            {isAROpen && (
                <ProductAR
                    onClose={() => setIsAROpen(false)}
                    modelReference={product.modelReference}
                    section={product.section}
                />
            )}
        </>
    );
};

export default ProductDetails;
