import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShirt, faCheck, faXmark, faRectangleAd } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';
import { getAllCategories, getProductManagement, getAllProducts, toggleProductAvailability, demoteProduct, createCategory, editCategory } from '../../services/adminServices';

import Footer from '../Footer';
import NonAuthorise from '../NonAuthorise';

const AdminProductList = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [categoryToEdit, setCategoryToEdit] = useState(null);
    const [productManagement, setProductManagement] = useState({
        totalProducts: 0,
        notAvailableProducts: 0,
        availableProducts: 0,
        promotedProducts: 0
    });

    const role = localStorage.getItem("role");
    const authToken = localStorage.getItem("authToken");
    const [categoryName, setCategoryName] = useState('');
    const [showModal, setShowModal] = useState(false);

    const [searchTerm, setSearchTerm] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 6;

    // ---------------- EFECTOS ----------------

    useEffect(() => {
        setPage(0);
    }, [searchTerm]);

    // Obtener categorías
    useEffect(() => {
        if (!authToken || role !== 'ADMIN') return;

        const fetchCategories = async () => {
            try {
                const data = await getAllCategories(authToken);
                setCategories(data);
            } catch (error) {
                console.error(error.message);
            }
        };

        fetchCategories();
    }, [authToken, role]);

    // Obtener métricas
    useEffect(() => {
        if (!authToken || role !== 'ADMIN') return;

        const fetchProductManagement = async () => {
            try {
                const data = await getProductManagement(authToken);
                setProductManagement(data);
            } catch (error) {
                console.error(error.message);
            }
        };

        fetchProductManagement();
    }, [authToken, role]);

    // Obtener productos
    useEffect(() => {
        if (!authToken || role !== 'ADMIN') return;

        const debounce = setTimeout(() => {
            const fetchProducts = async () => {
                try {
                    const data = await getAllProducts(authToken, page, pageSize, searchTerm);
                    setProducts(data.content);
                    setTotalPages(data.totalPages);
                } catch (error) {
                    console.error(error.message);
                }
            };

            fetchProducts();
        }, 400);

        return () => {
            clearTimeout(debounce);
        };
    }, [searchTerm, page, authToken, role]);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [page]);

    const handleSearchChange = (e) => setSearchTerm(e.target.value);

    const nextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    const prevPage = () => {
        if (page > 0) setPage(page - 1);
    };

    const handleToggleAvailability = (id, shouldEnable) => {
        if (role !== "ADMIN") return;

        toggleProductAvailability(authToken, id, shouldEnable)
            .then(() => window.location.reload())
            .catch((err) => console.error('Error:', err));
    };

    const handleDemoteProduct = async (e, id) => {
        e.preventDefault();
        try {
            const message = await demoteProduct(authToken, id);
            alert(message);
            window.location.reload();
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };

    const handleCreateCategory = async (e) => {
        e.preventDefault();
        try {
            await createCategory(authToken, categoryName);
            alert('Category created successfully!');
            window.location.reload();
        } catch (error) {
            alert(`Error creating category: ${error.message}`);
        }
    };

    const handleEditCategory = async (e, categoryId) => {
        e.preventDefault();
        try {
            await editCategory(authToken, categoryId, categoryName);
            alert('Categoría editada con éxito!');
            window.location.reload();
        } catch (error) {
            alert(`Error al editar la categoría: ${error.message}`);
        }
    };


    const MetricCard = React.memo(({ icon, value, title, iconColor, bgColor }) => (
        <div className="flex-1 bg-white shadow-lg rounded-lg p-4 m-2 flex items-center">
            <div className={`mr-4 w-12 h-12 rounded-full flex items-center justify-center ${bgColor}`}>
                <FontAwesomeIcon icon={icon} className={`${iconColor} text-xl`} />
            </div>
            <div>
                <p className="text-3xl font-bold text-indigo-600 truncate">{value}</p>
                <p className="text-sm font-semibold text-gray-400 truncate">{title}</p>
            </div>
        </div>
    ));

    if (role !== 'ADMIN') return <NonAuthorise />;

    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-4">
                    {/* COLUMNA 1 */}
                    <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
                        <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                            <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                Gestión de Productos
                            </p>
                            <Link to="/product/new" className="w-full md:w-auto">
                                <button
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    Crear nuevo producto
                                </button>
                            </Link>
                        </div>
                        <input
                            type="text"
                            placeholder="Buscar producto..."
                            value={searchTerm}
                            onChange={handleSearchChange}
                            className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                        />

                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-3 gap-6">
                            {products.map((product) => (
                                <div key={product.id} className="h-full">
                                    <div className="h-full bg-white shadow-md rounded-lg p-4 border border-gray-200 hover:shadow-lg transition-shadow relative flex flex-col justify-between">
                                        <Link to={`/product/edit/${product.id}`} className="w-full">
                                            <img
                                                src={`/api/product${product.images[0]}`}
                                                alt={product.name}
                                                className="w-full h-40 object-contain rounded-md mb-3"
                                                loading="lazy"
                                            />
                                            <div className="flex justify-between items-center mb-2">
                                                <h2 className="font-semibold text-base md:text-lg text-gray-800 truncate">
                                                    {product.name}
                                                </h2>
                                                {!product.available ? (
                                                    <span className="text-xs bg-red-100 text-red-600 px-2 py-1 rounded">No disponible</span>
                                                ) : (
                                                    <span className="text-xs bg-green-100 text-green-600 px-2 py-1 rounded">Activo</span>
                                                )}
                                            </div>
                                            <div className="mb-2">
                                                {product.on_Promotion && product.discount > 0 ? (
                                                    <div className="flex items-center gap-2">
                                                        <span className="text-sm text-gray-500 line-through">{(product.price / ((100 - product.discount) / 100)).toFixed(2)}€</span>
                                                        <span className="text-base font-bold text-rose-600">
                                                            {product.price.toFixed(2)}€
                                                        </span>
                                                        <span className="text-xs bg-yellow-100 text-yellow-600 px-2 py-0.5 rounded">
                                                            -{product.discount}%
                                                        </span>
                                                    </div>
                                                ) : (
                                                    <span className="text-base font-semibold text-gray-900">{product.price.toFixed(2)}€</span>
                                                )}
                                            </div>
                                            <div className="text-sm text-gray-600 mb-2">
                                                Tallas:{" "}
                                                {Object.entries(product.size)
                                                    .filter(([, stock]) => stock > 0)
                                                    .map(([size]) => size)
                                                    .join(", ") || "Sin stock"}
                                            </div>
                                            <div className="text-xs text-gray-500 italic">
                                                {Array.from(product.categories || [])
                                                    .sort((a, b) => a.name.localeCompare(b.name))
                                                    .map((cat) => cat.name)
                                                    .join(", ") || "Sin Categoría"}
                                            </div>
                                            {product.ratings?.length > 0 && (
                                                <div className="absolute top-2 right-2 bg-yellow-400 text-white text-xs font-bold px-2 py-0.5 rounded">
                                                    ⭐{" "}
                                                    {(
                                                        product.ratings.reduce((acc, r) => acc + r.score, 0) /
                                                        product.ratings.length
                                                    ).toFixed(1)}
                                                </div>
                                            )}
                                        </Link>
                                        {/* Botón de promoción/despromoción */}
                                        <div className='mt-2 flex flex-col gap-2 text-center'>
                                            {product.on_Promotion ? (
                                                <button
                                                    onClick={(e) => handleDemoteProduct(e, product.id)}
                                                    className="w-full bg-red-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-red-600"
                                                >
                                                    Despromocionar
                                                </button>
                                            ) : (
                                                <Link
                                                    to={`/product/promote/${product.id}?price=${product.price}`}
                                                    className="w-full bg-green-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-green-600"
                                                >
                                                    Promocionar
                                                </Link>
                                            )}

                                            {/* Botón de habilitar/deshabilitar */}
                                            {product.available ? (
                                                <button
                                                    onClick={() => handleToggleAvailability(product.id, false)}
                                                    className="w-full bg-red-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-gray-600"
                                                >
                                                    Deshabilitar
                                                </button>
                                            ) : (
                                                <button
                                                    onClick={() => handleToggleAvailability(product.id, true)}
                                                    className="w-full bg-indigo-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-indigo-600"
                                                >
                                                    Habilitar
                                                </button>
                                            )}
                                        </div>
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

                    {/* COLUMNA 2 */}
                    <div className="w-full">
                        <div className="bg-gray-50 p-4 rounded-lg mb-4 flex justify-around">
                            <div className="flex flex-col sm:flex-row flex-wrap ">
                                <MetricCard icon={faShirt} value={productManagement.totalProducts} title="Productos Totales" iconColor="text-blue-600" bgColor="bg-blue-300" />
                                <MetricCard icon={faCheck} value={productManagement.availableProducts} title="Productos Disponibles" iconColor="text-green-600" bgColor="bg-green-300" />
                                <MetricCard icon={faXmark} value={productManagement.notAvailableProducts} title="Productos No Disponibles" iconColor="text-red-600" bgColor="bg-red-300" />
                                <MetricCard icon={faRectangleAd} value={productManagement.promotedProducts} title="Productos Promocionados" iconColor="text-yellow-600" bgColor="bg-yellow-300" />
                            </div>
                        </div>

                        <div className="bg-gray-50 p-4 rounded-lg">
                            <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                                <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                    Gestión de Etiquetas
                                </p>
                                <button
                                    onClick={() => setShowModal(true)}  // Abre el modal
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    Crear nueva etiqueta
                                </button>
                            </div>
                            <table className="min-w-full divide-y divide-gray-200 text-sm">
                                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                                    <tr>
                                        <th className="px-4 py-3 text-left">ID</th>
                                        <th className="px-4 py-3 text-left">Nombre</th>
                                        <th className="px-4 py-3 text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                    {categories.length > 0 ? categories.map((category) => (
                                        <tr key={category.id}>
                                            <td className="px-4 py-3">{category.id}</td>
                                            <td className="px-4 py-3">{category.name}</td>
                                            <td className="px-4 py-3 text-center">
                                                <Link
                                                    to="#"
                                                    onClick={() => {
                                                        setCategoryToEdit(category);
                                                        setCategoryName(category.name);
                                                        setShowModal(true);
                                                    }}
                                                    className="text-blue-500 hover:text-blue-700 mr-3">
                                                    Editar
                                                </Link>
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="3" className="text-center px-4 py-3 text-gray-500">
                                                No hay categorías disponibles.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            {/* Modal para crear categoría */}
            {showModal && (
                <div className="fixed inset-0 flex justify-center items-center bg-gray-500 bg-opacity-50 z-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                        <h2 className="text-xl font-bold mb-4">
                            {categoryToEdit ? 'Editar categoría' : 'Crear nueva categoría'}
                        </h2>
                        <form
                            onSubmit={(e) =>
                                categoryToEdit
                                    ? handleEditCategory(e, categoryToEdit.id)
                                    : handleCreateCategory(e)
                            }
                        >
                            <input
                                type="text"
                                placeholder="Nombre de la categoría"
                                value={categoryName}
                                onChange={(e) => setCategoryName(e.target.value)}
                                className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                            />
                            <div className="flex justify-between">
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowModal(false);
                                        setCategoryToEdit(null);
                                        setCategoryName('');
                                    }}
                                    className="px-4 py-2 bg-gray-300 text-black rounded-lg"
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-blue-500 text-white rounded-lg"
                                >
                                    {categoryToEdit ? 'Guardar cambios' : 'Crear categoría'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
            <Footer />
        </>
    );
};

export default AdminProductList;
