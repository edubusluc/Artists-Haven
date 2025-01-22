import React, { useState, useEffect, useContext } from "react";
import { useParams } from "react-router-dom";
import { CartContext } from "../context/CartContext";

const ProductDetails = () => {
    const [product, setProduct] = useState({});
    const { id } = useParams();
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { shoppingCart, setShoppingCart } = useContext(CartContext);
    const [selectedSize, setSelectedSize] = useState(""); // Tamaño seleccionado

    useEffect(() => {
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
            <button onClick={handleAddProduct} className="btn btn-primary">
                Añadir al carrito
            </button>
        </>
    );
};

export default ProductDetails;