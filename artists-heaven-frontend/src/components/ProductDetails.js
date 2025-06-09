import React, { useState, useEffect, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import bg from '../util-image/bg.png';
import shopbg from '../util-image/shopBg.png';
import Footer from './Footer';
import { useTranslation } from 'react-i18next';

const ProductDetails = () => {
    const [product, setProduct] = useState({});
    const { id } = useParams();
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { shoppingCart, setShoppingCart } = useContext(CartContext);
    const [selectedSize, setSelectedSize] = useState("");
    const navigate = useNavigate();
    const [reviews, setReviews] = useState([]);
    const { t, i18n } = useTranslation();

    useEffect(() => {
        // Primer fetch
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
                data.sizes = data.size; // En el product viene como size
                data.categories = data.categories.map(category => category.id); // Obtener solo los IDs de las categorías
                setProduct(data); // Establecer los valores iniciales del producto
            })
            .catch((error) => {
                console.error(error);
            });

        // Segundo fetch
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
                console.log(additionalData);
                setReviews(additionalData); // Establecer las reseñas
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
        const fullStars = Math.floor(score); // Estrellas llenas
        const halfStars = score % 1 !== 0 ? 1 : 0; // Estrella a medio llenar
        const emptyStars = 5 - fullStars - halfStars; // Estrellas vacías

        return (
            <>
                {"★".repeat(fullStars)}{"☆".repeat(halfStars)}{"☆".repeat(emptyStars)}
            </>
        );
    };

    return (
    <>
    
        <div
            className="flex flex-col items-center justify-center"
            style={{
                backgroundImage: `url(${bg})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                backgroundRepeat: 'no-repeat',
                padding: '20px',
                color: 'white',
            }}
        >
            <div
                className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 max-w-6xl mx-auto rounded-3xl bg-white md:max-h-auto"
                style={{
                    backgroundImage: `url(${shopbg})`,
                    backgroundRepeat: 'no-repeat',
                    backgroundSize: 'cover',
                }}
            >
                {/* IMÁGENES SECUNDARIAS DEL PRODUCTO */}
                <div className="flex gap-4 col-span-1 p-4 h-full items-center md:flex-col">
                    <div className="gap-4 flex-wrap md:flex-row">
                        {product.images?.map((image, index) => (
                            <img
                                key={index}
                                src={`/api/product${image}`}
                                alt={product.name}
                                className="w-10 h-10 bg-white mt-2"
                                style={{ boxShadow: '0 2px 4px rgba(0,0,0,0.5)' }}
                                loading="lazy"
                            />
                        ))}
                    </div>

                    {/* IMAGEN PRINCIPAL */}
                    <div className="bg-white shadow-lg rounded-lg p-4 flex justify-center items-center">
                        <img
                            src={`/api/product${product.images}`}
                            alt={product.name}
                            className="w-auto h-auto object-contain mt-4 p-4"
                            style={{ maxWidth: '100%', height: 'auto' }}
                            loading="lazy"
                        />
                    </div>
                </div>

                {/* INFO DEL PRODUCTO */}
                <div className="col-span-1 ">
                    <div className="grid grid-rows-">
                        {/* Nombre + PRECIO + REF */}
                        <div className="text-black w-full mx-auto">
                            <p className="custom-font-footer-black">{product.name}</p>
                            <p className="bold-arial">{product.price}€</p>
                            <p className="arial">DESARROLLAR REF </p>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* DESCRIPCIÓN */}
                        <div className="text-black w-full mx-auto">
                            <p className="arial">{product.description}</p>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* TALLAS DISPONIBLES */}
                        <div className="text-black w-full mx-auto">
                            <p className="custom-font-footer-black">{t('AvailableSizes')}</p>
                            <div className="flex flex-wrap gap-2">
                                {product.sizes &&
                                    Object.entries(product.sizes).map(([size]) => (
                                        <label
                                            key={size}
                                            className={`cursor-pointer px-4 py-2 text-sm font-medium ${
                                                selectedSize === size
                                                    ? 'bg-yellow-400 text-white hover:bg-yellow-500 transition'
                                                    : 'bg-white bold-arial hover:bg-gray-200 transition'
                                            } `}
                                            style={{ boxShadow: '0 2px 4px rgba(0,0,0,0.5)' }}
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
                                className={`w-full py-2 px-4 ${
                                    product.available
                                        ? 'button-custom cursor-pointer'
                                        : 'bg-gray-300 text-gray-600 cursor-not-allowed'
                                }`}
                            >
                                <p className="arial">{t('AddToShoppingCard')}</p>
                            </button>

                            <hr className="border-t border-black my-4 w-full" />

                            <button
                                onClick={handleAddReview}
                                className="button-custom w-full bg-yellow-400 text-black py-2 px-4 border-none cursor-pointer "
                            >
                                <p className="arial">{t('AddReview')}</p>
                            </button>

                            <hr className="border-t border-black my-4 w-full" />

                            <button className="w-full border border-black text-black py-2 px-4 cursor-pointer bg-white">
                                <p className="arial">{t('sizeGuide')}</p>
                            </button>
                            <hr className="border-t border-black my-4 w-full" />
                        </div>

                        {/* DETALLES DEL PRODUCTO */}
                        <div className="text-black w-full mx-auto">
                            <p className="arial"> {t('Product details')}</p>
                            <p className="arial"> {t('Composition')}</p>
                            <hr className="border-t border-black my-2 w-full" />
                            <p className="arial"> {t('Care')}</p>
                            <hr className="border-t border-black my-2 w-full" />
                            <p className="arial">{t('Origin')}</p>
                            <hr className="border-t border-black my-2 w-full" />
                        </div>

                        {/* VALORACIÓN */}
                        <div className="text-black w-full mx-auto">
                            <p className="arial">Valoración General</p>
                            {reviews.length > 0 ? (
                                reviews.map((review, index) => (
                                    <div key={index} className="review">
                                        <div className="review-rating">
                                            {renderStars(review.score)} {/* Muestra las estrellas */}
                                        </div>
                                        <div className="review-comment">
                                            <p>{review.comment}</p> {/* Muestra el comentario */}
                                            <p>{review.email}</p>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p>{t('No reviews')}</p>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <hr className="border-2 border-yellow-500 my-4 w-2/3" />
            <hr className="border-2 border-yellow-500 my-1 w-1/3" />
        </div>

        <Footer />
    </>
);


};

export default ProductDetails;
