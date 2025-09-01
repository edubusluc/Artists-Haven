import React, { useState, useEffect, useCallback } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
    faShirt,
    faCheck,
    faXmark,
    faRectangleAd,
} from "@fortawesome/free-solid-svg-icons";
import { Link } from "react-router-dom";
import {
    getAllCategories,
    getProductManagement,
    getAllProducts,
    toggleProductAvailability,
    demoteProduct,
    createCategory,
    editCategory,
    createCollection,
    editCollection,
    getAllCollections,
} from "../../services/adminServices";
import { checkTokenExpiration } from "../../utils/authUtils";
import Footer from "../Footer";
import { LazyLoadImage } from 'react-lazy-load-image-component';
import NonAuthorise from "../NonAuthorise";
import SessionExpired from "../SessionExpired";
import { useTranslation } from 'react-i18next';

// ------------------- COMPONENTES REUTILIZABLES -------------------
const ModalForm = React.memo(({ title, placeholder, value, setValue, onSubmit, onCancel, isEdit }) => {
    const { t } = useTranslation();

    return (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-500 bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                <h2 className="text-xl font-bold mb-4">
                    {isEdit ? `${t('adminProductList.edit')} ${title}` : `${t('adminProductList.createNew')} ${title}`}
                </h2>
                <form onSubmit={onSubmit}>
                    <input
                        type="text"
                        placeholder={placeholder}
                        value={value}
                        onChange={(e) => setValue(e.target.value)}
                        className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                    />
                    <div className="flex justify-between">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 bg-gray-300 text-black rounded-lg"
                        >
                            {t('adminProductList.cancel')}
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 bg-blue-500 text-white rounded-lg"
                        >
                            {isEdit ? t('adminProductList.saveChanges') : `${t('adminProductList.create')} ${title}`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
});


const ModalCollectionForm = React.memo(({ title, placeholder, value, setValue, onSubmit, onCancel, isEdit, isPromoted, setIsPromoted }) => {
    const { t } = useTranslation();
    return (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-500 bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                <h2 className="text-xl font-bold mb-4">
                    {isEdit ? `${t('adminProductList.edit')} ${title}` : `${t('adminProductList.createNew')} ${title}`}
                </h2>
                <form onSubmit={onSubmit}>
                    <input
                        type="text"
                        value={value}
                        onChange={(e) => setValue(e.target.value)}
                        placeholder={placeholder}
                        className="w-full border border-gray-300 rounded p-2 mb-4"
                        required
                    />
                    {isEdit && (
                        <label className="inline-flex items-center mb-4">
                            <input
                                type="checkbox"
                                checked={isPromoted}
                                onChange={(e) => setIsPromoted(e.target.checked)}
                                className="form-checkbox"
                            />
                            <span className="ml-2">{t('adminProductList.promoteCollection')}</span>
                        </label>
                    )}
                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 rounded bg-gray-200"
                        >
                            {t('adminProductList.cancel')}
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 rounded bg-yellow-400 text-black font-semibold hover:bg-yellow-500"
                        >
                            {isEdit ? t('adminProductList.saveChanges') : `${t('adminProductList.create')} ${title}`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
});

const MetricCard = React.memo(
    ({ icon, value, title, iconColor, bgColor }) => (
        <div className="flex-1 bg-white shadow-lg rounded-lg p-4 m-2 flex items-center">
            <div
                className={`mr-4 w-12 h-12 rounded-full flex items-center justify-center ${bgColor}`}
            >
                <FontAwesomeIcon icon={icon} className={`${iconColor} text-xl`} />
            </div>
            <div>
                <p className="text-3xl font-bold text-indigo-600 truncate">{value}</p>
                <p className="text-sm font-semibold text-gray-400 truncate">{title}</p>
            </div>
        </div>
    )
);

const Pagination = React.memo(({ page, totalPages, onPrev, onNext }) => {
    const prevDisabled = page === 0;
    const nextDisabled = page >= totalPages - 1;

    const baseBtn = "px-4 py-2 rounded w-full sm:w-auto font-medium transition-colors";
    const disabledClass = "bg-gray-300 text-gray-600 cursor-not-allowed opacity-50";
    const activeClass = "bg-gray-300 hover:bg-gray-400 text-gray-800";
    const { t } = useTranslation();

    return (
        <div className="flex flex-col sm:flex-row justify-center items-center mt-4 gap-2 sm:gap-4">
            <button
                onClick={onPrev}
                disabled={prevDisabled}
                aria-label="Página anterior"
                className={`${baseBtn} ${prevDisabled ? disabledClass : activeClass}`}
            >
                {t('adminProductList.previous')}
            </button>

            <span className="font-semibold text-gray-700 whitespace-nowrap">
                {t('adminProductList.page')} {page + 1} {t('adminProductList.of')} {totalPages}
            </span>

            <button
                onClick={onNext}
                disabled={nextDisabled}
                aria-label="Página siguiente"
                className={`${baseBtn} ${nextDisabled ? disabledClass : activeClass}`}
            >
                {t('adminProductList.next')}
            </button>
        </div>
    );
});

// ------------------- COMPONENTE PRINCIPAL -------------------

const AdminProductList = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [collections, setCollections] = useState([]);
    const [productManagement, setProductManagement] = useState({
        totalProducts: 0,
        notAvailableProducts: 0,
        availableProducts: 0,
        promotedProducts: 0,
    });

    const role = localStorage.getItem("role");
    const authToken = localStorage.getItem("authToken");

    const [categoryToEdit, setCategoryToEdit] = useState(null);
    const [collectionToEdit, setCollectionToEdit] = useState(null);
    const [categoryName, setCategoryName] = useState("");
    const [collectionName, setCollectionName] = useState("");
    const [showModal, setShowModal] = useState(false);
    const [showCollectionModal, setShowCollectionModal] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [isPromoted, setIsPromoted] = useState(false);

    const { t, i18n } = useTranslation();
    const language = i18n.language;

    const pageSize = 6;

    // ------------------- FETCH FUNCTIONS -------------------

    const fetchCategories = useCallback(async () => {
        try {
            const response = await getAllCategories(authToken);
            console.log("CATEGORIES")
            console.log(response)
            setCategories(response.data);
        } catch (error) {
            console.error(error.message);
        }
    }, [authToken]);

    const fetchCollections = useCallback(async () => {
        try {
            const response = await getAllCollections(authToken);
            setCollections(response.data);
        } catch (error) {
            console.error(error.message);
        }
    }, [authToken]);

    const fetchProductManagement = useCallback(async () => {
        try {
            const response = await getProductManagement(authToken);
            setProductManagement(response.data);
        } catch (error) {
            console.error(error.message);
        }
    }, [authToken]);

    const fetchProducts = useCallback(async () => {
        try {
            const response = await getAllProducts(authToken, page, pageSize, searchTerm);
            setProducts(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error(error.message);
        }
    }, [authToken, page, searchTerm]);

    // ------------------- EFFECTS -------------------

    useEffect(() => {
        if (!checkTokenExpiration || role !== "ADMIN") return;
        fetchCategories();
        fetchCollections();
        fetchProductManagement();
    }, [fetchCategories, fetchCollections, fetchProductManagement, role]);

    useEffect(() => {
        if (!checkTokenExpiration || role !== "ADMIN") return;
        const debounce = setTimeout(fetchProducts, 400);
        return () => clearTimeout(debounce);
    }, [fetchProducts, role]);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [page]);

    // ------------------- HANDLERS -------------------

    const handleToggleAvailability = async (id, shouldEnable) => {
        if (!checkTokenExpiration()) return;
        try {
            await toggleProductAvailability(authToken, id, shouldEnable);
            fetchProducts();
        } catch (err) {
            console.error("Error:", err);
        }
    };

    const handleDemoteProduct = async (e, id) => {
        e.preventDefault();
        try {
            const message = await demoteProduct(authToken, id, language);
            console.log(message);
            alert(message.message);
            fetchProducts();
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };

    const handleCreateCategory = async (e) => {
        e.preventDefault();
        try {
            await createCategory(authToken, categoryName);
            alert("Categoría creada con éxito!");
            fetchCategories();
            setShowModal(false);
            setCategoryName("");
        } catch (error) {
            alert(`Error creando categoría: ${error.message}`);
        }
    };

    const handleEditCategory = async (e, categoryId) => {
        e.preventDefault();
        try {
            await editCategory(authToken, categoryId, categoryName);
            alert("Categoría editada con éxito!");
            fetchCategories();
            setShowModal(false);
            setCategoryToEdit(null);
        } catch (error) {
            alert(`Error al editar la categoría: ${error.message}`);
        }
    };

    const handleCreateCollection = async (e) => {
        e.preventDefault();
        try {
            await createCollection(authToken, collectionName);
            alert("Colección creada con éxito!");
            fetchCollections();
            setShowCollectionModal(false);
            setCollectionName("");
        } catch (error) {
            alert(`Error creando colección: ${error.message}`);
        }
    };

    const handleEditCollection = async (e, collectionId) => {
        e.preventDefault();
        try {
            await editCollection(authToken, collectionId, collectionName, isPromoted);
            alert("Colección editada con éxito!");
            fetchCollections();
            setShowCollectionModal(false);
            setCollectionToEdit(null);
        } catch (error) {
            alert(`Error al editar la colección: ${error.message}`);
        }
    };

    const handleSearchChange = useCallback((e) => {
        setSearchTerm(e.target.value);
        setPage(0);
    }, []);

    const nextPage = useCallback(() => {
        setPage((prev) => Math.min(prev + 1, totalPages - 1));
    }, [totalPages]);

    const prevPage = useCallback(() => {
        setPage((prev) => Math.max(prev - 1, 0));
    }, []);

    if (!role || role !== 'ADMIN') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    console.log(products);

    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-4">
                    {/* COLUMNA 1 */}
                    <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
                        <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                            <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                {t('adminProductList.productManagemente')}
                            </p>
                            <Link to="/product/new" className="w-full md:w-auto">
                                <button
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    {t('adminProductList.createNewProduct')}
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
                                            <LazyLoadImage
                                                src={`/api/product${product.images[0]}`}
                                                alt={product.name}
                                                className="w-full h-40 object-contain rounded-md mb-3"
                                            />
                                            <div className="flex justify-between items-center mb-2">
                                                <h2 className="font-semibold text-base md:text-lg text-gray-800 truncate">
                                                    {product.name}
                                                </h2>
                                                {!product.available ? (
                                                    <span className="text-xs bg-red-100 text-red-600 px-2 py-1 rounded">{t('adminProductList.notAvailable')}</span>
                                                ) : (
                                                    <span className="text-xs bg-green-100 text-green-600 px-2 py-1 rounded">{t('adminProductList.active')}</span>
                                                )}
                                            </div>
                                            <div className="mb-2">
                                                {product.onPromotion && product.discount > 0 ? (
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
                                            {Object.keys(product.sizes ?? {}).length > 0 || product.section !== 'ACCESSORIES' ? (
                                                <div className="text-sm text-gray-600 mb-2">
                                                    {t('adminProductList.sizes')}:{" "}
                                                    {Object.entries(product.sizes ?? {})
                                                        .filter(([, stock]) => stock > 0)
                                                        .map(([size]) => size)
                                                        .join(", ") || "Sin stock"}
                                                </div>
                                            ) : (
                                                <div className="text-sm text-gray-600 mb-2">
                                                    <p>{t('editProductForm.availableUnits')}: {product.availableUnits}</p>
                                                </div>
                                            )}
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
                                        <div className="mt-2 flex flex-col gap-2 text-center">
                                            {product.onPromotion ? (
                                                <button
                                                    onClick={(e) => handleDemoteProduct(e, product.id)}
                                                    className="w-full bg-red-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-red-600"
                                                >
                                                    {t('adminProductList.demote')}
                                                </button>
                                            ) : (
                                                product.available && (
                                                    <Link
                                                        to={`/product/promote/${product.id}`}
                                                        className="w-full bg-green-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-green-600"
                                                    >
                                                        {t('adminProductList.promote')}
                                                    </Link>
                                                )
                                            )}

                                            {/* Botón de habilitar/deshabilitar */}
                                            {product.available ? (
                                                <button
                                                    onClick={() => handleToggleAvailability(product.id, false)}
                                                    className="w-full bg-red-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-gray-600"
                                                >
                                                    {t('adminProductList.disable')}
                                                </button>
                                            ) : (
                                                <button
                                                    onClick={() => handleToggleAvailability(product.id, true)}
                                                    className="w-full bg-indigo-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-indigo-600"
                                                >
                                                    {t('adminProductList.enable')}
                                                </button>
                                            )}
                                        </div>

                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* Paginación */}
                        <Pagination
                            page={page}
                            totalPages={totalPages}
                            onPrev={prevPage}
                            onNext={nextPage}
                        />
                    </div>

                    {/* COLUMNA 2 */}
                    <div className="w-full">
                        <div className="bg-gray-50 p-4 rounded-lg mb-4 flex justify-around">
                            <div className="flex flex-col sm:flex-row flex-wrap ">
                                <MetricCard icon={faShirt} value={productManagement.totalProducts} title={t('adminProductList.totalProducts')} iconColor="text-blue-600" bgColor="bg-blue-300" />
                                <MetricCard icon={faCheck} value={productManagement.availableProducts} title={t('adminProductList.availableProducts')} iconColor="text-green-600" bgColor="bg-green-300" />
                                <MetricCard icon={faXmark} value={productManagement.notAvailableProducts} title={t('adminProductList.notAvailableProducts')} iconColor="text-red-600" bgColor="bg-red-300" />
                                <MetricCard icon={faRectangleAd} value={productManagement.promotedProducts} title={t('adminProductList.promoteProducts')} iconColor="text-yellow-600" bgColor="bg-yellow-300" />
                            </div>
                        </div>

                        <div className="bg-gray-50 p-4 rounded-lg">
                            <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                                <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                    {t('adminProductList.labelsManagement')}
                                </p>
                                <button
                                    onClick={() => setShowModal(true)} 
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    {t('adminProductList.createLabel')}
                                </button>
                            </div>

                            <table className="min-w-full divide-y divide-gray-200 text-sm">
                                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                                    <tr>
                                        <th className="px-4 py-3 text-left">ID</th>
                                        <th className="px-4 py-3 text-left">{t('adminProductList.name')}</th>
                                        <th className="px-4 py-3 text-center">{t('adminProductList.actions')}</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                    {categories.length > 0 ? 
                                    categories
                                    .slice()
                                    .sort((a, b) => a.id - b.id)
                                    .map((category) => (
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
                                                    {t('adminProductList.edit')}
                                                </Link>
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="3" className="text-center px-4 py-3 text-gray-500">
                                                {t('adminProductList.notCategoryAvailable')}
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>

                        <div className="bg-gray-50 p-4 rounded-lg mt-4">
                            <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                                <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                                    {t('adminProductList.collectionManagement')}
                                </p>
                                <button
                                    onClick={() => setShowCollectionModal(true)} 
                                    className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                >
                                    {t('adminProductList.newCollection')}
                                </button>
                            </div>

                            <table className="min-w-full divide-y divide-gray-200 text-sm">
                                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                                    <tr>
                                        <th className="px-4 py-3 text-left">ID</th>
                                        <th className="px-4 py-3 text-left">{t('adminProductList.name')}</th>
                                        <th className="px-4 py-3 text-center">{t('adminProductList.promoted')}</th>
                                        <th className="px-4 py-3 text-center">{t('adminProductList.actions')}</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                    {collections.length > 0 ? collections.map((collection) => (
                                        <tr key={collection.id}>
                                            <td className="px-4 py-3">{collection.id}</td>
                                            <td className="px-4 py-3">{collection.name}</td>
                                            <td className="px-4 py-3 text-center">
                                                <span
                                                    className={`px-2 py-1 rounded text-white ${collection.isPromoted ? 'bg-green-500' : 'bg-red-500'
                                                        }`}
                                                >
                                                    {collection.isPromoted ? t('adminProductList.promotedCollection') : t('adminProductList.notPromotedCollecion')}
                                                </span>
                                            </td>
                                            <td className="px-4 py-3 text-center">
                                                <Link
                                                    to="#"
                                                    onClick={() => {
                                                        setCollectionToEdit(collection);
                                                        setCollectionName(collection.name);
                                                        setIsPromoted(Boolean(collection.isPromoted));
                                                        setShowCollectionModal(true);
                                                    }}
                                                    className="text-blue-500 hover:text-blue-700 mr-3">
                                                    {t('adminProductList.edit')}
                                                </Link>
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="3" className="text-center px-4 py-3 text-gray-500">
                                                {t('adminProductList.notCollectionsAvailable')}
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            {/* Modal para crear categoría y colleciones */}
            {showModal && (
                <ModalForm
                    title={t('adminProductList.category')}
                    placeholder={t('adminProductList.categoryPlaceholder')}
                    value={categoryName}
                    setValue={setCategoryName}
                    onSubmit={(e) =>
                        categoryToEdit
                            ? handleEditCategory(e, categoryToEdit.id)
                            : handleCreateCategory(e)
                    }
                    onCancel={() => {
                        setShowModal(false);
                        setCategoryToEdit(null);
                        setCategoryName("");
                    }}
                    isEdit={!!categoryToEdit}
                />
            )}

            {showCollectionModal && (
                <ModalCollectionForm
                    title={t('adminProductList.collection')}
                    placeholder={t('adminProductList.collectionPlaceholder')}
                    value={collectionName}
                    setValue={setCollectionName}
                    onSubmit={(e) =>
                        collectionToEdit
                            ? handleEditCollection(e, collectionToEdit.id)
                            : handleCreateCollection(e)
                    }
                    onCancel={() => {
                        setShowCollectionModal(false);
                        setCollectionToEdit(null);
                        setCollectionName("");
                        setIsPromoted(false);
                    }}
                    isEdit={!!collectionToEdit}
                    isPromoted={isPromoted}
                    setIsPromoted={setIsPromoted}
                />
            )}
            <Footer />
        </>
    );
};

export default AdminProductList;
