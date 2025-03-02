import React, { useState, useEffect, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";

const ProductDetails = () => {
    const [product, setProduct] = useState({});
    const { id } = useParams();
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { shoppingCart, setShoppingCart } = useContext(CartContext);
    const [selectedSize, setSelectedSize] = useState("");
    const navigate = useNavigate();
    const [reviews, setReviews] = useState([]);

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
            <h1>Detalles del Producto: {product.name}</h1>
            <h3>Precio: {product.price}</h3>
            <div className="product-sizes">
                <h3>Tamaños disponibles:</h3>
                <ul>
                    {product.sizes &&
                        Object.entries(product.sizes).map(([size]) => (
                            <li key={size}>
                                <label>
                                    <input
                                        type="radio"
                                        name="size"
                                        value={size}
                                        onChange={(e) => setSelectedSize(e.target.value)}
                                    />
                                    {size}
                                </label>
                            </li>
                        ))}
                </ul>
            </div>

            <div>
                <h3>Valoraciones del producto:</h3>
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
                    <p>No hay reseñas para este producto aún.</p>
                )}
            </div>

            <button onClick={handleAddProduct} className="btn btn-primary">
                Añadir al carrito
            </button>

            <button onClick={handleAddReview} className="btn btn-primary">
                Añadir Review
            </button>
        </>
    );
};

export default ProductDetails;
