import React, { useEffect, useState, useMemo, useCallback } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faThumbsUp } from "@fortawesome/free-solid-svg-icons";
import { checkTokenExpiration } from "../utils/authUtils";
import SessionExpired from "./SessionExpired";
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import SlidingPanel from "../utils/SlidingPanel";
import userProduct from '../util-image/userproduct1.png';
import Footer from "./Footer";

const ProductCard = ({ product, onVote }) => {
    const [loaded, setLoaded] = useState(false);

    return (
        <div className="w-full group">
            <div className="relative w-full aspect-[700/986] flex items-center justify-center bg-gray-100 overflow-hidden">
                <img
                    src={`http://localhost:8080/api/user-products${product.images[0]}`}
                    alt={product.name}
                    loading="lazy"
                    onLoad={() => setLoaded(true)}
                    className={`h-auto absolute object-contain transition-all duration-700 ease-in-out
                        ${loaded ? "opacity-100 scale-100" : "opacity-0 scale-95"}
                        group-hover:opacity-0 group-hover:scale-110`}
                    style={{ transformOrigin: 'center center' }}
                />
                {product.images[1] && (
                    <img
                        src={`http://localhost:8080/api/user-products${product.images[1]}`}
                        alt={`${product.name} hover`}
                        loading="lazy"
                        className={`h-auto absolute object-cover transition-all duration-700 ease-in-out
                            ${loaded ? "opacity-0 scale-95" : "opacity-0 scale-95"}
                            group-hover:opacity-100 group-hover:scale-110`}
                        style={{ transformOrigin: 'center center' }}
                    />
                )}
            </div>
            <div className="mt-3 text-left ml-3 flex justify-between mr-2">
                <p className="custom-font-shop-regular" style={{ color: 'black' }}>
                    {product.name} - {product.username}
                </p>
                <div className="flex items-center justify-center space-x-2">
                    <p>{product.numVotes}</p>
                    <FontAwesomeIcon
                        icon={faThumbsUp}
                        className={`transition-colors duration-300 ${product.votedByUser
                            ? "text-blue-500 cursor-not-allowed pointer-events-none"
                            : "text-gray-400 hover:text-blue-500 cursor-pointer"
                            }`}
                        onClick={!product.votedByUser ? () => onVote(product.id) : undefined}
                    />
                </div>
            </div>
        </div>
    );
};


export default function UserProductsPage() {
    const [products, setProducts] = useState([]);
    const [filteredProducts, setFilteredProducts] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const [unauthorized, setUnauthorized] = useState(false);
    const [newProduct, setNewProduct] = useState({ name: "", images: [] });
    const [validationErrors, setValidationErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState("");

    const { t, i18n } = useTranslation();
    const language = i18n.language;

    // -------- FILTROS ----------
    const [isFilterOpen, setIsFilterOpen] = useState(false);
    const [orderBy, setOrderBy] = useState("az"); // default: alfabético
    const [voteRange, setVoteRange] = useState({ min: 0, max: 1000 });

    // Temporales (dentro del panel)
    const [tempOrderBy, setTempOrderBy] = useState(orderBy);
    const [tempVoteRange, setTempVoteRange] = useState(voteRange);

    const [maxVotes, setMaxVotes] = useState(1000);

    const [mainImageLoaded, setMainImageLoaded] = useState(false);

    useEffect(() => {
        if (unauthorized) {
            localStorage.removeItem("authToken");
        }
    }, [unauthorized]);

    const fetchProducts = useCallback(async () => {
        try {
            const res = await fetch("http://localhost:8080/api/user-products/all", {
                headers: {
                    'Authorization': authToken ? `Bearer ${authToken}` : undefined,
                },
            });
            if (!res.ok) throw new Error("Error fetching products");
            const data = await res.json();
            setProducts(data.data);

            const maxVote = Math.max(...data.data.map(p => p.numVotes), 0);
            setMaxVotes(maxVote);
            setVoteRange({ min: 0, max: maxVote });
            setTempVoteRange({ min: 0, max: maxVote });
        } catch (err) {
            console.error("Error fetching products:", err);
        }
    }, [authToken]);

    useEffect(() => {
        fetchProducts();
    }, [fetchProducts]);

    // -------- FILTRADO ----------
    const filtered = useMemo(() => {
        let result = [...products];

        // Filtro por votos
        result = result.filter(
            (p) => p.numVotes >= voteRange.min && p.numVotes <= voteRange.max
        );

        // Ordenación
        switch (orderBy) {
            case "az":
                result.sort((a, b) => a.name.localeCompare(b.name));
                break;
            case "za":
                result.sort((a, b) => b.name.localeCompare(a.name));
                break;
            case "votesHighLow":
                result.sort((a, b) => b.numVotes - a.numVotes);
                break;
            case "votesLowHigh":
                result.sort((a, b) => a.numVotes - b.numVotes);
                break;
            case "dateNewOld":
                result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                break;
            case "dateOldNew":
                result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
                break;
            default:
                break;
        }
        return result;
    }, [products, voteRange, orderBy]);

    useEffect(() => {
        setFilteredProducts(filtered);
    }, [filtered]);

    const handleApplyFilters = () => {
        setOrderBy(tempOrderBy);
        setVoteRange(tempVoteRange);
        setIsFilterOpen(false);
    };

    const handleClearFilters = () => {
        setOrderBy("az");
        setVoteRange({ min: 0, max: maxVotes });
        setTempOrderBy("az");
        setTempVoteRange({ min: 0, max: maxVotes });
        setIsFilterOpen(false);
    };

    // -------- VOTACIÓN ----------
    const handleVote = async (productId) => {
        try {
            if (!authToken) {
                alert(t("forfan.noPermission"));
                return;
            }

            if (!checkTokenExpiration()) {
                setUnauthorized(true);
                return;
            }

            const response = await fetch(`http://localhost:8080/api/productVote/${productId}?lang=${language}`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${authToken}` },
            });

            const result = await response.json();
            const errorMessage = result.message;

            if (!response.ok) throw new Error(t(errorMessage));

            alert(errorMessage);
            fetchProducts();
        } catch (error) {
            alert(error.message || t("forfan.errorVote"));
        }
    };

    const handleFileChange = (e) => {
        setNewProduct({ ...newProduct, images: e.target.files });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setValidationErrors({});
        let errors = {};

        if (!newProduct.name) errors.name = t('forFan.error.requiredName');
        if (!newProduct.images || newProduct.images.length !== 2) {
            errors.images = t('forFan.error.requiredTwoImages');
        }
        if (!newProduct.acceptTerms) {
            errors.acceptTerms = t('forFan.error.requiredTerms');
        }

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            return;
        }

        setValidationErrors("");
        try {
            let formData = new FormData();
            formData.append("userProductDTO", new Blob([JSON.stringify({ name: newProduct.name })], { type: "application/json" }));

            for (let i = 0; i < newProduct.images.length; i++) {
                formData.append("images", newProduct.images[i]);
            }

            const res = await fetch(`http://localhost:8080/api/user-products/create?lang=${language}`, {
                method: "POST",
                headers: { 'Authorization': `Bearer ${authToken}` },
                body: formData,
            });

            const result = await res.json();
            const errorMessage = result.message;

            if (!res.ok) throw new Error(errorMessage);

            await fetchProducts();
            alert(t('forFan.createSuccess'));
            setShowForm(false);
            setNewProduct({ name: "", images: [] });
        } catch (err) {
            setErrorMessage(err.message);
        }
    };



    const handleButtonClick = () => {
        if (!authToken) {
            alert("Debes iniciar sesión para registrar un producto");
            return;
        }
        if (!checkTokenExpiration()) {
            setUnauthorized(true);
        }
        else {
            newProduct.name = "";
            newProduct.images = [];
            setErrorMessage("");
            setShowForm(!showForm);
        }
    };



    return (
        <><div className="p-2 mt-5 relative">
            <div className="relative w-full h-[600px] lg:h-[700px] mt-5 overflow-hidden">
                <img
                    src={userProduct}
                    alt="Producto"
                    onLoad={() => setMainImageLoaded(true)}
                    className={`w-full h-full object-cover transition-opacity duration-700 ease-in-out 
        ${mainImageLoaded ? "opacity-100" : "opacity-0"}`}
                />
                {/* Botón de orden */}
                <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2 z-10">
                    <Link to="/forFan">
                        <button className="button-yellow-border" onClick={handleButtonClick}>
                            UPLOAD YOUR ART
                        </button>

                    </Link>
                </div>
            </div>
            {unauthorized ? (
                <SessionExpired />
            ) : (
                <>
                    {/* Header con título y filtros */}
                    <div className="flex justify-end items-center mb-6 w-full">
                        <div className="flex items-center gap-4 mt-2">
                            {/* Botón Filtros */}
                            <p
                                className="custom-font-shop-regular mb-0 cursor-pointer"
                                style={{ color: "black" }}
                                onClick={() => setIsFilterOpen(true)}
                            >
                                {t("productSchema.filterAndSearch")}
                            </p>
                        </div>
                    </div>

                    {/* Grid de productos */}
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 w-ful">
                        {filteredProducts.map((product) => (
                            <div key={product.id} className="flex justify-center">
                                <ProductCard product={product} onVote={handleVote} />
                            </div>
                        ))}
                    </div>

                    {/* Modal para formulario creación de producto */}
                    {showForm && (
                        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-50">
                            <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6 animate-fadeIn">
                                <div className="flex justify-between items-center mb-6">
                                    <h2 className="text-xl font-semibold">{t("forfan.addProduct")}</h2>
                                    <button
                                        onClick={handleButtonClick}
                                        className="text-gray-500 hover:text-gray-700 text-lg"
                                    >
                                        ✕
                                    </button>
                                </div>

                                <form onSubmit={handleSubmit} className="space-y-5">
                                    {/* Nombre */}
                                    <div>
                                        <label className="block text-sm font-medium mb-2">
                                            {t("forfan.name")}
                                        </label>
                                        <input
                                            type="text"
                                            value={newProduct.name}
                                            onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
                                            className="w-full border rounded-xl px-4 py-3 shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none" />
                                        {validationErrors.name && (
                                            <p className="text-red-600 text-sm mt-1">
                                                {validationErrors.name}
                                            </p>
                                        )}
                                    </div>

                                    {/* Imagen recomendación */}
                                    <p className="text-red-600 text-sm">{t("forFan.recommendedImg")}</p>

                                    {/* Imágenes */}
                                    <div>
                                        <label className="block text-sm font-medium mb-2">
                                            {t("forfan.images")}
                                        </label>
                                        <input
                                            type="file"
                                            multiple
                                            accept="image/*"
                                            onChange={handleFileChange}
                                            className="w-full" />
                                        {validationErrors.images && (
                                            <p className="text-red-600 text-sm mt-1">
                                                {validationErrors.images}
                                            </p>
                                        )}
                                    </div>

                                    {/* Checkbox términos */}
                                    <div className="flex items-start gap-2">
                                        <input
                                            type="checkbox"
                                            checked={newProduct.acceptTerms || false}
                                            onChange={(e) => setNewProduct({
                                                ...newProduct,
                                                acceptTerms: e.target.checked,
                                            })}
                                            className="mt-1 h-5 w-5 text-blue-600 rounded focus:ring-blue-500" />
                                        <span className="text-sm">
                                            {t("forFan.acceptTerms") + " "}
                                            <a
                                                href="/terms-and-conditions"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="text-blue-600 underline"
                                            >
                                                {t("forFan.termsLink")}
                                            </a>
                                        </span>
                                    </div>
                                    {validationErrors.acceptTerms && (
                                        <p className="text-red-600 text-sm">
                                            {validationErrors.acceptTerms}
                                        </p>
                                    )}

                                    {/* Botón guardar */}
                                    <button
                                        type="submit"
                                        className="w-full py-3 rounded-full bg-green-600 hover:bg-green-700 text-white font-medium transition-all duration-200 shadow-md"
                                    >
                                        {t("forfan.saveProduct")}
                                    </button>

                                    {/* Mensaje de error */}
                                    {errorMessage && (
                                        <div className="text-red-900 text-sm mt-4 bg-red-200 rounded-md p-3">
                                            {errorMessage}
                                        </div>
                                    )}
                                </form>
                            </div>
                        </div>
                    )}

                    {/* Panel de filtros */}
                    <SlidingPanel
                        isOpen={isFilterOpen}
                        position="right"
                        title={t("Filters")}
                        onClose={() => setIsFilterOpen(false)}
                        maxWidth="500px"
                    >
                        {/* Ordenación */}
                        <div
                            className="p-4 custom-font-shop-regular mb-4"
                            style={{ color: "black" }}
                        >
                            <label htmlFor="orderBy" className="block mb-2 font-semibold">
                                {t("productSchema.sort")}
                            </label>
                            <select
                                id="orderBy"
                                value={tempOrderBy}
                                onChange={(e) => setTempOrderBy(e.target.value)}
                                className="w-full border border-gray-300 rounded p-2"
                            >
                                <option value="az">{t("productSchema.sortedAZ")}</option>
                                <option value="za">{t("productSchema.sortedZA")}</option>
                                <option value="votesHighLow">{t("forFan.sortByMostVotes")}</option>
                                <option value="votesLowHigh">{t("forFan.sortByLessVotes")}</option>
                                <option value="dateNewOld">{t("forFan.SortByNewest")}</option>
                                <option value="dateOldNew">{t("forFan.SortByOldest")}</option>
                            </select>
                        </div>

                        {/* Votos */}
                        <div
                            className="p-4 custom-font-shop-regular mb-4"
                            style={{ color: "black" }}
                        >
                            <p className="font-semibold mb-2">{t("Filtrar por votos")}</p>
                            <input
                                type="range"
                                min={0}
                                max={maxVotes}
                                value={tempVoteRange.max}
                                onChange={(e) => setTempVoteRange({
                                    ...tempVoteRange,
                                    max: Number(e.target.value),
                                })}
                                className="w-full"
                                style={{ accentColor: "black", cursor: "pointer" }} />
                            <p className="mt-2">
                                {t("Votos")}: {tempVoteRange.min} - {tempVoteRange.max}
                            </p>
                        </div>

                        {/* Botones */}
                        <div className="p-4 flex w-full gap-4">
                            <button
                                onClick={handleClearFilters}
                                className="slide-on-hover border border-black cursor-pointer px-8 py-6 flex-1 transition-transform duration-300 ease-in-out hover:translate-x-2"
                            >
                                <span className="custom-font-shop custom-font-shop-black">
                                    {t("productSchema.deleteFilter")}
                                </span>
                            </button>
                            <button
                                onClick={handleApplyFilters}
                                className="slide-on-hover border border-black cursor-pointer px-8 py-6 flex-1 transition-transform duration-300 ease-in-out hover:translate-x-2"
                                style={{ backgroundColor: "black", color: "white" }}
                            >
                                <span className="custom-font-shop">
                                    {t("productSchema.applyFilter")}
                                </span>
                            </button>
                        </div>
                    </SlidingPanel>
                </>
            )}
        </div><Footer /></>
    );


}
