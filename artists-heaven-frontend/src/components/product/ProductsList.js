import { useState, useEffect } from 'react';
import Footer from '../Footer';
import cardBg from '../../util-image/bgImg.png';
import { Link } from 'react-router-dom';
import bg from '../../util-image/bg.png';

const ProductList = () => {
    const [products, setProducts] = useState([]);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 6;

    useEffect(() => {
        // Obtener los productos del backend usando fetch
        fetch(`/api/product/allProducts?page=${page}&size=${pageSize}`)
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Hubo un error al obtener los productos');
                }
                return response.json();
            })
            .then((data) => {
                setProducts(data.content);
                setTotalPages(data.totalPages);
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    }, [page]);

    const nextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    const prevPage = () => {
        if (page > 0) setPage(page - 1);
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
                {/* Paginación */}
                <div className="flex flex-col sm:flex-row justify-center items-center mt-4 gap-2 sm:gap-4">
                    <button onClick={prevPage} disabled={page === 0} className="w-full sm:w-auto px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                        Anterior
                    </button>
                    <span className="font-semibold text-gray-700">Página {page + 1} de {totalPages}</span>
                    <button onClick={nextPage} disabled={page >= totalPages - 1} className="w-full sm:w-auto px-4 py-2 bg-gray-300 rounded disabled:opacity-50">
                        Siguiente
                    </button>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default ProductList;