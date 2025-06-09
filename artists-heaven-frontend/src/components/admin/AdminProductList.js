import bg from '../../util-image/bg.png';
import Footer from '../Footer';
import React, { useState, useEffect } from 'react';

const AdminProductList = () => {
    const [products, setProducts] = useState([]);

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

    return (
        <>
            <div
                className="min-h-screen flex items-center justify-center p-6"
                style={{
                    backgroundImage: `url(${bg})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    backgroundRepeat: 'no-repeat',
                    padding: '20px',
                    color: 'white',
                    minHeight: '100vh',
                }}
            >
                <div className="w-full max-w-4xl h-full rounded-lg shadow-lg bg-white backdrop-blur-md p-6 md:p-8">
                    <p className="custom-font-footer-black mb-6 text-center ">Gestión de Productos</p>

                    {/* Tabla de productos */}
                    <table className="w-full table-auto border-collapse mb-6">
                        <thead>
                            <tr className="bg-gray-800 text-white">
                                <th className="px-6 py-3 text-left">Nombre</th>
                                <th className="px-6 py-3 text-left">Nombre</th>
                                <th className="px-6 py-3 text-left">Precio</th>
                                <th className="px-6 py-3 text-left">Categoría</th>
                                <th className="px-6 py-3 text-left">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            {products.map((product) => (
                                <tr
                                    key={product.id}
                                    className="transition duration-300 ease-in-out transform hover:bg-gray-200 hover:scale-105"
                                >   <td className="px-6 py-4 border-b border-gray-200">
                                        <div className="w-24 h-24 mx-auto">
                                            <img
                                                src={`/api/product${product.images[0]}`}
                                                alt={product.name}
                                                className="w-full h-full object-contain"
                                            />
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 border-b border-gray-200 text-black">{product.name}</td>
                                    <td className="px-6 py-4 border-b border-gray-200 text-black">{product.price} $</td>
                                    <td className="px-6 py-4 border-b border-gray-200 text-black">
                                        {product.available ? 'Disponible' : 'No disponible'}
                                    </td>
                                    <td className="px-6 py-4 border-b border-gray-200">
                                        <button className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition ease-in-out duration-200 transform hover:scale-105 mr-2">
                                            Editar
                                        </button>
                                        <button className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition ease-in-out duration-200 transform hover:scale-105">
                                            Eliminar
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
            <Footer />
        </>
    );
}

export default AdminProductList;
