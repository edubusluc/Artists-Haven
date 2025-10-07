import { useState, useEffect, useMemo } from "react";
import Footer from "../Footer";
import { Link } from "react-router-dom";
import ProductCard from "./ProductCard";
import SlidingPanel from "../../utils/SlidingPanel";
import { useTranslation } from 'react-i18next';

const ProductSchema = ({ endpoint, title, hideCollectionFilter }) => {
    const [products, setProducts] = useState([]);
    const [collections, setCollections] = useState([]);

    const [isFilterOpen, setIsFilterOpen] = useState(false);

    const [orderBy, setOrderBy] = useState("default");
    const [selectedSizes, setSelectedSizes] = useState([]);
    const [priceRange, setPriceRange] = useState({ min: 0, max: 1000 });
    const [selectedCategories, setSelectedCategories] = useState([]);
    const [selectedCollections, setSelectedCollections] = useState([]);
    const [tempSelectedCollections, setTempSelectedCollections] = useState([]);

    const [tempOrderBy, setTempOrderBy] = useState(orderBy);
    const [tempSelectedSizes, setTempSelectedSizes] = useState(selectedSizes);
    const [tempPriceRange, setTempPriceRange] = useState(priceRange);
    const [tempSelectedCategories, setTempSelectedCategories] = useState(selectedCategories);

    const { t } = useTranslation();

    const [maxAbsolutePrice, setMaxAbsolutePrice] = useState(1000);
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        if (!products.length) return;
        const imageUrls = products.map(p => p.images);
        const loadImage = (src) => new Promise((resolve) => {
            const img = new Image();
            img.src = src;
            img.onload = img.onerror = resolve;
        });

        Promise.all(imageUrls.map(loadImage)).then(() => {
            setTimeout(() => {
                setIsVisible(true);
            }, 60);
        });
    }, [products]);

    useEffect(() => {
        fetch(endpoint)
            .then((response) => {
                if (!response.ok) throw new Error("Error al obtener productos");
                return response.json();
            })
            .then((productData) => {
                setProducts(productData.data);

                const prices = productData.data.map((p) => p.price);
                const maxPrice = Math.max(...prices);
                setMaxAbsolutePrice(maxPrice);
                setPriceRange({ min: 0, max: maxPrice });
                setTempPriceRange({ min: 0, max: maxPrice });
            })
            .catch((error) => console.error("Error:", error));
    }, [endpoint]);

    // ✅ Cargar colecciones desde backend
    useEffect(() => {
        fetch("http://localhost:8080/api/product/allCollections")
            .then(res => res.json())
            .then(data => setCollections(data.data))
            .catch(err => console.error("Error cargando colecciones", err));
    }, []);

    const categories = useMemo(() => {
        const catsMap = new Map();
        products.forEach((p) => {
            if (p.categories && Array.isArray(p.categories)) {
                p.categories.forEach((cat) => {
                    catsMap.set(cat.id, cat);
                });
            }
        });
        return Array.from(catsMap.values());
    }, [products]);

    const hasNonAccessoryProducts = useMemo(() => {
        return products.some(product => product.section !== "ACCESSORIES");
    }, [products]);

    const filteredProducts = useMemo(() => {
        let result = [...products];

        if (selectedSizes.length > 0) {
            result = result.filter((product) =>
                product.colors?.some((color) =>
                    color.availableUnits == null &&
                    selectedSizes.some((size) => (color.sizes?.[size] ?? 0) > 0)
                )
            );
        }

        result = result.filter(
            (product) => product.price >= priceRange.min && product.price <= priceRange.max
        );

        if (selectedCategories.length > 0) {
            result = result.filter((product) =>
                product.categories?.some((cat) => selectedCategories.includes(cat))
            );
        }

        // ✅ Filtrado por colecciones
        if (selectedCollections.length > 0) {
            result = result.filter((product) =>
                selectedCollections.includes(product.collectionId)
            );
        }

        switch (orderBy) {
            case "az":
                result.sort((a, b) => a.name.localeCompare(b.name));
                break;
            case "za":
                result.sort((a, b) => b.name.localeCompare(a.name));
                break;
            case "priceHighLow":
                result.sort((a, b) => b.price - a.price);
                break;
            case "priceLowHigh":
                result.sort((a, b) => a.price - b.price);
                break;
            default:
                break;
        }

        return result;
    }, [products, selectedSizes, priceRange, selectedCategories, selectedCollections, orderBy]);

    const toggleTempSize = (size) => {
        setTempSelectedSizes((prev) =>
            prev.includes(size) ? prev.filter((s) => s !== size) : [...prev, size]
        );
    };

    const toggleTempCategory = (category) => {
        setTempSelectedCategories((prev) =>
            prev.includes(category) ? prev.filter((c) => c !== category) : [...prev, category]
        );
    };

    // ✅ Toggle de colecciones (guardar solo IDs)
    const toggleTempCollection = (collectionId) => {
        setTempSelectedCollections((prev) =>
            prev.includes(collectionId)
                ? prev.filter((c) => c !== collectionId)
                : [...prev, collectionId]
        );
    };

    const handleApplyFilters = () => {
        setOrderBy(tempOrderBy);
        setSelectedSizes(tempSelectedSizes);
        setPriceRange(tempPriceRange);
        setSelectedCategories(tempSelectedCategories);
        setSelectedCollections(tempSelectedCollections); // ✅ Colecciones
        setIsFilterOpen(false);
    };

    const handleClearFilters = () => {
        setOrderBy("default");
        setSelectedSizes([]);
        setPriceRange({ min: 0, max: maxAbsolutePrice });
        setSelectedCategories([]);
        setSelectedCollections([]); // ✅ Reset

        setTempOrderBy("default");
        setTempSelectedSizes([]);
        setTempPriceRange({ min: 0, max: maxAbsolutePrice });
        setTempSelectedCategories([]);
        setTempSelectedCollections([]); // ✅ Reset

        setIsFilterOpen(false);
    };

    useEffect(() => {
        if (isFilterOpen) {
            setTempOrderBy(orderBy);
            setTempSelectedSizes(selectedSizes);
            setTempPriceRange(priceRange);
            setTempSelectedCategories(selectedCategories);
            setTempSelectedCollections(selectedCollections); // ✅ Mantener colecciones
        }
    }, [isFilterOpen, orderBy, selectedSizes, priceRange, selectedCategories, selectedCollections]);

    return (
        <>
            <div
                style={{
                    color: "white",
                    minHeight: "100vh",
                    opacity: isVisible ? 1 : 0,
                    transition: "opacity 0.7s ease-in-out",
                }}
                className="p-4 mt-12"
            >
                <div className="flex justify-between mt-5">
                    <p className="custom-font-shop-regular mb-4" style={{ color: "black" }}>
                        {filteredProducts.length} {t('productSchema.products')}
                    </p>
                    <p
                        className="custom-font-shop-regular mb-4 cursor-pointer"
                        style={{ color: "black" }}
                        onClick={() => setIsFilterOpen(true)}
                    >
                        {t('productSchema.filterAndSearch')}
                    </p>
                </div>

                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
                    {filteredProducts.map((product) => (
                        <Link to={`/product/details/${product.id}`} key={product.id}>
                            <div className="flex justify-center">
                                <ProductCard product={product} />
                            </div>
                        </Link>
                    ))}
                </div>
            </div>

            <SlidingPanel
                isOpen={isFilterOpen}
                position="right"
                title={t('Filters')}
                onClose={() => setIsFilterOpen(false)}
                maxWidth="500px"
            >
                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <label htmlFor="orderBy" className="block mb-2 font-semibold">
                        {t('productSchema.sort')}
                    </label>
                    <select
                        id="orderBy"
                        value={tempOrderBy}
                        onChange={(e) => setTempOrderBy(e.target.value)}
                        className="w-full border border-gray-300 rounded p-2"
                    >
                        <option value="default">{t('productSchema.default')}</option>
                        <option value="az">{t('productSchema.sortedAZ')}</option>
                        <option value="za">{t('productSchema.sortedZA')}</option>
                        <option value="priceHighLow">{t('productSchema.sortedPriceHighLow')}</option>
                        <option value="priceLowHigh">{t('productSchema.sortedPriceLowHigh')}</option>
                    </select>
                </div>

                {hasNonAccessoryProducts && (
                    <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                        <p className="font-semibold mb-2">{t('productSchema.sizes')}</p>
                        {["XS", "S", "M", "L", "XL", "XXL"].map((size) => (
                            <label key={size} className="inline-flex items-center mr-4 mb-2 cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="form-checkbox"
                                    checked={tempSelectedSizes.includes(size)}
                                    onChange={() => toggleTempSize(size)}
                                />
                                <span className="ml-2">{size}</span>
                            </label>
                        ))}
                    </div>
                )}

                {/* Categorías */}
                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <p className="font-semibold mb-2">{t('productSchema.categories')}</p>
                    {categories.map((category) => (
                        <label key={category.id} className="inline-flex items-center mr-4 mb-2 cursor-pointer">
                            <input
                                type="checkbox"
                                className="form-checkbox"
                                checked={tempSelectedCategories.some(cat => cat.id === category.id)}
                                onChange={() => toggleTempCategory(category)}
                            />
                            <span className="ml-2">{category.name}</span>
                        </label>
                    ))}
                </div>

                {/* ✅ Colecciones */}
                {!hideCollectionFilter && (
                    <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                        <p className="font-semibold mb-2">{t('productSchema.collections')}</p>
                        {collections.map((col) => (
                            <label key={col.id} className="inline-flex items-center mr-4 mb-2 cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="form-checkbox"
                                    checked={tempSelectedCollections.includes(col.id)}
                                    onChange={() => toggleTempCollection(col.id)}
                                />
                                <span className="ml-2">{col.name}</span>
                            </label>
                        ))}
                    </div>
                )}

                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <p className="font-semibold mb-2">{t('productSchema.sortPrice')}</p>
                    <input
                        type="range"
                        min={0}
                        max={maxAbsolutePrice}
                        value={tempPriceRange.max}
                        onChange={(e) => {
                            const val = Number(e.target.value);
                            if (val >= 0 && val <= maxAbsolutePrice) {
                                setTempPriceRange((prev) => ({ ...prev, min: 0, max: val }));
                            }
                        }}
                        className="w-full"
                        style={{
                            accentColor: "black",
                            cursor: "pointer",
                        }}
                    />
                    <p className="mt-2">
                        {t('productSchema.price')}: 0€ - {tempPriceRange.max.toFixed(2)}€
                    </p>
                </div>

                <div className="p-4 flex w-full gap-4">
                    <button
                        onClick={handleClearFilters}
                        className="slide-on-hover border border-black cursor-pointer px-8 py-6 flex-1 transition-transform duration-300 ease-in-out hover:translate-x-2"
                    >
                        <span className="custom-font-shop custom-font-shop-black">
                            {t('productSchema.deleteFilter')}
                        </span>
                    </button>
                    <button
                        onClick={handleApplyFilters}
                        className="slide-on-hover border border-black cursor-pointer px-8 py-6 flex-1 transition-transform duration-300 ease-in-out hover:translate-x-2"
                        style={{ backgroundColor: "black" }}
                    >
                        <span className="custom-font-shop">
                            {t('productSchema.applyFilter')}
                        </span>
                    </button>
                </div>
            </SlidingPanel>

            <Footer />
        </>
    );
};

export default ProductSchema;
