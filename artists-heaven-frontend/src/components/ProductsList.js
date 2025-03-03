import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const ProductList = () => {
    const [products, setProducts] = useState([]);
    const navigate = useNavigate();
    const role = localStorage.getItem('role');
    const [authToken] = useState(localStorage.getItem("authToken"));

    useEffect(() => {
        // Obtener los productos del backend usando fetch
        fetch('/api/product/allProducts')
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Hubo un error al obtener los productos');
                }
                return response.json();
            })
            .then((data) => {
                setProducts(data);
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    }, []);

    const handleDelete = (id) => {
        fetch(`/api/product/delete/${id}`, { method: 'DELETE' })
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Error al eliminar el producto');
                }
                // Actualizar la lista de productos eliminando el producto correspondiente
                setProducts(products.filter((product) => product.id !== id));
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    };

    const handleDemoteProduct = async (id) => {
        try {
            const response = await fetch(`/api/product/demote/${id}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${authToken}`
                },
            });

            const message = await response.text(); // 📌 Leer el mensaje del backend

            if (!response.ok) {
                alert(`Error: ${message}`);
            } else {
                alert(message); // 📌 Mostrar mensaje del backend en el alert
                window.location.reload();
            }

        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };


    return (
        <div className="product-list">
            {products.length > 0 ? (
                products.map((product) => (
                    <div key={product.id} className="product-item">
                        <h2>
                            {product.name}{" "}
                            <span style={{ color: product.available ? "green" : "red" }}>
                                {product.available ? "Available" : "Out of Stock"}
                            </span>
                        </h2>
                        <p>{product.description}</p>
                        <p>Precio: ${product.price}</p>
                        <div className="product-images">
                            {product.images.map((image, index) => (
                                <img
                                    key={index}
                                    src={`/api/product${image}`}
                                    alt={image}
                                    className="product-image"
                                    style={{ width: '150px', height: 'auto' }}
                                />
                            ))}
                        </div>
                        {product.available && (
                            <div className="product-sizes">
                                <h3>Tamaños disponibles:</h3>
                                <ul>
                                    {Object.entries(product.size).map(([size, quantity]) => (
                                        <li key={size}>
                                            {size}: {quantity} unidades
                                        </li>
                                    ))}
                                </ul>
                                {/* Botón para ver los detalles del producto */}
                                <button onClick={() => navigate(`/product/details/${product.id}`)} className="btn btn-primary">
                                    Ver Detalles
                                </button>
                            </div>
                        )}




                        {role === 'ADMIN' && (
                            <>
                                <button onClick={() => handleDelete(product.id)} className="delete-button">
                                    Eliminar
                                </button>
                                <button onClick={() => navigate(`/product/edit/${product.id}`)} className="btn btn-primary">
                                    Editar Producto
                                </button>
                                <button onClick={() => navigate(`/product/promote/${product.id}`)} className="btn btn-primary">
                                    Promocionar
                                </button>
                                <button onClick={() => handleDemoteProduct(product.id)} className="btn btn-primary">
                                    Eliminar Promoción
                                </button>
                            </>
                        )}
                    </div>
                ))
            ) : (
                <p>No hay productos disponibles.</p>
            )}
        </div>
    );
};

export default ProductList;