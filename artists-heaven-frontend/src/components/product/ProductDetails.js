import { useState, useEffect, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { CartContext } from "../../context/CartContext";
import Footer from '../Footer';
import { useTranslation } from 'react-i18next';
import AddReviewModal from "./AddReviewModal";

const ProductDetails = () => {
    const [product, setProduct] = useState({});
    const { id } = useParams();
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { shoppingCart, setShoppingCart } = useContext(CartContext);
    const [selectedSize, setSelectedSize] = useState("");
    const navigate = useNavigate();
    const [reviews, setReviews] = useState([]);
    const { t } = useTranslation();
    const [mainImage, setMainImage] = useState('/default-image.jpg');
    const [isModalOpen, setIsModalOpen] = useState(false);


    const sizeOrder = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [id, product, reviews]);

    useEffect(() => {
        // Fetch producto
        fetch(`/api/product/details/${id}`, {
            method: "GET",
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Error al obtener el producto: " + response.statusText);
                }
                return response.json();
            })
            .then((data) => {
                data.sizes = data.size;
                data.categories = data.categories.map(category => category.id);
                setProduct(data);

                if (data.images && data.images.length > 0) {
                    setMainImage(data.images[0]);
                } else {
                    setMainImage('/default-image.jpg');
                }
            })
            .catch((error) => {
                console.error(error);
            });

        // Fetch reseñas
        fetch(`/api/rating/productReview/${id}`, {
            method: "GET",
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Error al obtener las reseñas: " + response.statusText);
                }
                return response.json();
            })
            .then((additionalData) => {
                setReviews(additionalData);
            })
            .catch((error) => {
                console.error(error);
            });
    }, [id]);

    const handleAddProduct = async () => {
        if (!selectedSize) {
            alert("Por favor, selecciona un tamaño antes de añadir al carrito.");
            return;
        }

        const endpoint = authToken
            ? `/api/myShoppingCart/addProducts`
            : `/api/myShoppingCart/addProductsNonAuthenticate`;

        const payload = authToken
            ? {
                productId: product.id,
                size: selectedSize,
            }
            : {
                shoppingCart: shoppingCart,
                productId: product.id,
                size: selectedSize,
            };

        try {
            const response = await fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(authToken && { Authorization: `Bearer ${authToken}` }),
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error(`Error al añadir el producto al carrito: ${response.status}`);
            }

            const data = await response.json();
            setShoppingCart(data);
        } catch (error) {
            console.error("Error al añadir el producto al carrito:", error);
        }
    };

    const handleAddReview = () => {
        navigate(`/product/newReview/${id}`);
    };

    const renderStars = (score) => {
        const fullStars = Math.floor(score);
        const halfStars = score % 1 !== 0 ? 1 : 0;
        const emptyStars = 5 - fullStars - halfStars;

        return (
            <>
                {"★".repeat(fullStars)}{"☆".repeat(halfStars)}{"☆".repeat(emptyStars)}
            </>
        );
    };

    const handleOpenModal = () => {
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        
    };

    return (
        <>
            <div className="grid grid-cols-1 md:grid-cols-2 p-4 mt-10">
                {/* Columna 1 */}
                <div>
                    {/*VERSION MÓVIL*/}
                    <div className="md:hidden flex overflow-x-auto space-x-4 p-4 justify-around">
                        {product.images?.map((image, index) => (
                            <div
                                key={index}
                                style={{ flexShrink: 0 }}
                                onClick={() => setMainImage(image)}
                            >
                                <img
                                    src={`/api/product${product.images[index]}`}
                                    alt={product.name}
                                    className="max-w-full max-h-[300px] object-contain rounded-md"
                                    loading="lazy"
                                />
                            </div>
                        ))}
                    </div>
                    {/*VERSION MD/LG */}
                    <div className="hidden md:grid grid-cols-2 gap-4 p-10">
                        {product.images?.map((image, index) => (
                            <div
                                key={index}
                                style={{ flexShrink: 0 }}
                                onClick={() => setMainImage(image)}
                            >
                                <img
                                    src={`/api/product${product.images[index]}`}
                                    alt={product.name}
                                    className="max-w-full max-h-[500px] object-contain rounded-md"
                                    loading="lazy"
                                />
                            </div>
                        ))}
                    </div>
                </div>

                {/* Columna 2 */}
                <div className="flex flex-col items-center justify-center p-4 sm:p-6">
                    {/* Nombre + PRECIO + REF */}
                    <div className="w-full">
                        <div className="text-black mx-auto">
                            <p className="custom-font-footer-black text-lg">{product.name}</p>
                            {product.on_Promotion && product.discount > 0 ? (
                                <>
                                    <snap className="inline-block p-1 bg-red-500 inter-400 text-xs text-white">{`DESCUENTO ${product.discount}%`}</snap>
                                    <div className="flex items-center gap-2">
                                        <span className="inter-400 text-sm line-through" style={{ color: '#909497' }}>
                                            {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
                                        </span>
                                        <span className="inter-400 text-sm text-red-600">
                                            {product.price ? product.price.toFixed(2) : "Precio no disponible"}€
                                        </span>
                                    </div>
                                </>
                            ) : (
                                <span className="inter-400 text-sm" style={{color: '#909497'}}>
                                    {product.price ? product.price.toFixed(2) : "Precio no disponible"}€
                                </span>
                            )}
                            <p className="inter-400 text-sm" >DESARROLLAR REF </p>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* DESCRIPCIÓN */}
                        <div className="text-black w-full mx-auto">
                            <p className="inter-400 text-sm">{product.description}</p>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* TALLAS DISPONIBLES */}
                        <div className="text-black w-full mx-auto">
                            <div className="flex flex-wrap gap-2">
                                {product.sizes &&
                                    Object.entries(product.sizes)
                                        .filter(([size, quantity]) => quantity > 0 && sizeOrder.includes(size))
                                        .sort(([sizeA], [sizeB]) => sizeOrder.indexOf(sizeA) - sizeOrder.indexOf(sizeB))
                                        .map(([size]) => (
                                            <label
                                                key={size}
                                                className={`border border-gray-200 cursor-pointer w-14 h-10 text-sm text-center font-medium flex items-center justify-center ${selectedSize === size
                                                    ? 'bg-yellow-400 text-white hover:bg-yellow-500 transition'
                                                    : 'bg-white bold-arial hover:bg-gray-200 transition'
                                                    }`}
                                            >
                                                <input
                                                    type="radio"
                                                    name="size"
                                                    value={size}
                                                    className="hidden"
                                                    onChange={(e) => setSelectedSize(e.target.value)}
                                                />
                                                {size}
                                            </label>
                                        ))}
                            </div>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* Añadir a la cesta + guía de tallas */}
                        <div className="text-black w-full mx-auto">
                            {!product.available && (
                                <p className="text-red-600 font-semibold mb-2">{t('NotAvailable')}</p>
                            )}

                            <button
                                onClick={handleAddProduct}
                                disabled={!product.available}
                                className={`w-full py-2 px-4 ${product.available
                                    ? 'button-custom cursor-pointer'
                                    : 'bg-gray-300 text-gray-600 cursor-not-allowed'
                                    }`}
                            >
                                <p className="inter-400 text-sm">{t('AddToShoppingCard')}</p>
                            </button>

                            {/* MODAL PARA AÑADIR RESEÑA */}
                            <div className="w-full mx-auto">
                                {/* Botón para abrir el modal */}
                                <button
                                    onClick={handleOpenModal}
                                    className="w-full mt-2 bg-black py-2 px-4 border-none cursor-pointer"
                                >
                                    <p className="text-white inter-400 text-sm">{t('AddReview')}</p>
                                </button>

                                {/* Modal para añadir reseña */}
                                <AddReviewModal
                                    isOpen={isModalOpen}
                                    onClose={handleCloseModal}
                                    productId={id}
                                    authToken={authToken}
                                />
                            </div>

                            <hr className="border-t border-black my-4 w-full" />

                            <button className="w-full border border-black text-black py-2 px-4 cursor-pointer bg-white">
                                <p className="inter-400 text-sm">{t('sizeGuide')}</p>
                            </button>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* DETALLES DEL PRODUCTO */}
                        <div className="text-black w-full mx-auto">
                            <p className="inter-400 text-sm">{t('Product details')}</p>
                            <p className="inter-400 text-sm">{t('Composition')}</p>
                            <hr className="border-t border-black my-2 w-full" />
                            <p className="inter-400 text-sm">{t('Care')}</p>
                            <hr className="border-t border-black my-2 w-full" />
                            <p className="inter-400 text-sm">{t('Origin')}</p>
                            <hr className="border-t border-black my-2 w-full" />
                        </div>

                        {/* VALORACIÓN */}
                        <div className="text-black w-full mx-auto">
                            <p className="inter-400 text-sm">Valoración General</p>
                            {reviews.length > 0 ? (
                                reviews.map((review, index) => (
                                    <div key={index} className="review">
                                        <div className="review-rating">
                                            {renderStars(review.score)} {/* Muestra las estrellas */}
                                        </div>
                                        <div className="review-comment inter-400 text-sm">
                                            <p>{review.comment}</p> {/* Muestra el comentario */}
                                            <p>{review.email}</p>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p className="inter-400 text-sm">{t('No reviews')}</p>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex flex-col items-center mt-4">
                <hr className="border-2 border-yellow-500 mb-2 w-2/3 sm:w-1/3" />
                <hr className="border-2 border-yellow-500 w-1/3" />
            </div>

            <Footer />
        </>
    );

};

export default ProductDetails;
