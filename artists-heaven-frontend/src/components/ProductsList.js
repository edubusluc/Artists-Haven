import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Footer from './Footer';
import cardBg from '../util-image/bgImg.png';
import { Link } from 'react-router-dom';
import bg from '../util-image/bg.png';
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
        <>
            <div
                style={{
                    backgroundImage: `url(${bg})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    backgroundRepeat: 'no-repeat',
                    padding: '20px',
                    color: 'white',
                    minHeight: '90vh',
                }}
            >
                <div className='flex justify-center'>
                    ALL BEST SELLERS NEW ACCESORIES

                </div>
                <div className='grid grid-cols-3 gap-4 p-4 max-w-6xl mx-auto'>
                    {products.map((product) => (
                        <div key={product.id}>
                            <Link to={`/product/details/${product.id}`}>
                            <div
                                className=" bg-cover bg-center shadow-lg w-full max-h-2xl"
                                style={{
                                    backgroundImage: `url(${cardBg})`,
                                    padding: '2rem',
                                    minHeight: '29rem',
                                    boxShadow: '0 4px 15px rgba(0,0,0,1)',
                                }}
                            >
                                {product.images?.[0] && (
                                    <img
                                        src={`/api/product${product.images[0]}`}
                                        alt={product.name}
                                        className="w-full h-full object-contain mt-4 p-4"
                                    />
                                )}
                            </div>
                            </Link>
                            <div className="flex justify-between mt-2">
                                <p className="mt-2 text-left custom-font-shop">{product.name}</p>
                                <p className="mt-2 text-right font-semibold">{product.price}€</p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            <Footer />
        </>
    );
};

export default ProductList;