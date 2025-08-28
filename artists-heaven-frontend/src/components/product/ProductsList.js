import { useState, useEffect, useMemo } from "react";
import Footer from "../Footer";
import { Link } from "react-router-dom";
import ProductCard from "./ProductCard";
import SlidingPanel from "../../utils/SlidingPanel";
import { useTranslation } from 'react-i18next';

const ProductList = () => {
    const [products, setProducts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 6;
    const [isFilterOpen, setIsFilterOpen] = useState(false);

    const [orderBy, setOrderBy] = useState("default");
    const [selectedSizes, setSelectedSizes] = useState([]);
    const [priceRange, setPriceRange] = useState({ min: 0, max: 1000 });
    const [selectedCategories, setSelectedCategories] = useState([]);

    const [tempOrderBy, setTempOrderBy] = useState(orderBy);
    const [tempSelectedSizes, setTempSelectedSizes] = useState(selectedSizes);
    const [tempPriceRange, setTempPriceRange] = useState(priceRange);
    const [tempSelectedCategories, setTempSelectedCategories] = useState(selectedCategories);

    const { t, i18n } = useTranslation();


    const [maxAbsolutePrice, setMaxAbsolutePrice] = useState(1000);

    const [imagesLoaded, setImagesLoaded] = useState(false);

    // Nuevo estado para controlar visibilidad y animación fadeIn
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
            setImagesLoaded(true);
            setTimeout(() => {
                setIsVisible(true); // Activamos el fadeIn
            }, 30); // Delay suave
        });

    }, [products]);

    useEffect(() => {
        fetch(`/api/product/allProducts?size=-1`)
            .then((response) => {
                if (!response.ok) throw new Error("Hubo un error al obtener los productos");
                return response.json();
            })
            .then((dataProduct) => {
                setProducts(dataProduct.data.content);
                setTotalPages(Math.ceil(dataProduct.data.content.length / pageSize));

                const prices = dataProduct.data.content.map((p) => p.price);
                const maxPrice = Math.max(...prices);
                setMaxAbsolutePrice(maxPrice);
                setPriceRange({ min: 0, max: maxPrice });
                setTempPriceRange({ min: 0, max: maxPrice });
            })
            .catch((error) => console.error("Error:", error));
    }, []);

    const categories = useMemo(() => {
        const catsMap = new Map();  // Usar Map para evitar duplicados por id
        products.forEach((p) => {
            if (p.categories && Array.isArray(p.categories)) {
                p.categories.forEach((cat) => {
                    catsMap.set(cat.id, cat); // Usamos id como clave para evitar duplicados
                });
            }
        });
        return Array.from(catsMap.values()); // Array de objetos categoría únicos
    }, [products]);

    const filteredProducts = useMemo(() => {
        let result = [...products];

        // Filtro por tallas
        if (selectedSizes.length > 0) {
            result = result.filter((product) =>
                selectedSizes.some((size) => product.size?.[size] > 0)
            );
        }

        // Filtro por precio
        result = result.filter(
            (product) => product.price >= priceRange.min && product.price <= priceRange.max
        );

        // Filtro por categorías
        if (selectedCategories.length > 0) {
            result = result.filter((product) =>
                product.categories?.some((cat) => selectedCategories.includes(cat))
            );
        }

        // Ordenar
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

        setTotalPages(Math.ceil(result.length / pageSize));
        if (page >= Math.ceil(result.length / pageSize)) setPage(0);

        return result;
    }, [products, selectedSizes, priceRange, selectedCategories, orderBy, page, pageSize]);

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

    const handleApplyFilters = () => {
        setOrderBy(tempOrderBy);
        setSelectedSizes(tempSelectedSizes);
        setPriceRange(tempPriceRange);
        setSelectedCategories(tempSelectedCategories);

        setIsFilterOpen(false);
        setPage(0);
    };

    const handleClearFilters = () => {
        setOrderBy("default");
        setSelectedSizes([]);
        setPriceRange({ min: 0, max: maxAbsolutePrice });
        setSelectedCategories([]);

        setTempOrderBy("default");
        setTempSelectedSizes([]);
        setTempPriceRange({ min: 0, max: maxAbsolutePrice });
        setTempSelectedCategories([]);

        setIsFilterOpen(false);
        setPage(0);
    };

    useEffect(() => {
        if (isFilterOpen) {
            setTempOrderBy(orderBy);
            setTempSelectedSizes(selectedSizes);
            setTempPriceRange(priceRange);
            setTempSelectedCategories(selectedCategories);
        }
    }, [isFilterOpen, orderBy, selectedSizes, priceRange, selectedCategories]);

    console.log("Filtered Products:", filteredProducts);


    return (
        <>
            <div
                style={{

                    color: "white",
                    minHeight: "100vh",
                    opacity: isVisible ? 1 : 0,
                    transition: "opacity 0.7s ease-in-out",
                }}
                className="p-4 mt-4"
            >
                <div className="flex justify-between mt-5">
                    <p className="custom-font-shop-regular mb-4" style={{ color: "black" }}>
                        {filteredProducts.length} {t('productsList.products')}
                    </p>
                    <p
                        className="custom-font-shop-regular mb-4 cursor-pointer"
                        style={{ color: "black" }}
                        onClick={() => setIsFilterOpen(true)}
                    >
                        {t('productsList.filterAndSearch')}
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

            {/* Panel separado por Portal */}
            <SlidingPanel
                isOpen={isFilterOpen}
                position="right"
                title={t('Filters')}
                onClose={() => setIsFilterOpen(false)}
                maxWidth="500px"
            >
                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <label htmlFor="orderBy" className="block mb-2 font-semibold">
                        {t('productsList.sort')}
                    </label>
                    <select
                        id="orderBy"
                        value={tempOrderBy}
                        onChange={(e) => setTempOrderBy(e.target.value)}
                        className="w-full border border-gray-300 rounded p-2"
                    >
                        <option value="default">{t('productsList.default')}</option>
                        <option value="az">{t('productsList.sortedAZ')}</option>
                        <option value="za">{t('productsList.sortedZA')}</option>
                        <option value="priceHighLow">{t('productsList.sortedPriceHighLow')}</option>
                        <option value="priceLowHigh">{t('productsList.sortedPriceLowHigh')}</option>
                    </select>
                </div>

                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <p className="font-semibold mb-2">{t('productsList.sizes')}</p>
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

                {/* Categorías */}
                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <p className="font-semibold mb-2">{t('productsList.categories')}</p>
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

                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <p className="font-semibold mb-2">{t('productsList.sortPrice')}</p>
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
                        {t('productsList.price')}: 0€ - {tempPriceRange.max.toFixed(2)}€
                    </p>
                </div>

                <div className="p-4 flex w-full gap-4">
                    <button
                        onClick={handleClearFilters}
                        className="slide-on-hover border border-black cursor-pointer px-8 py-6 flex-1 transition-transform duration-300 ease-in-out hover:translate-x-2"
                    >
                        <span className="custom-font-shop custom-font-shop-black">
                            {t('productsList.deleteFilter')}
                        </span>
                    </button>
                    <button
                        onClick={handleApplyFilters}
                        className="slide-on-hover border border-black cursor-pointer px-8 py-6 flex-1 transition-transform duration-300 ease-in-out hover:translate-x-2"

                        style={{ backgroundColor: "black" }}
                    >
                        <span className="custom-font-shop">
                            {t('productsList.applyFilter')}
                        </span>
                    </button>
                </div>

            </SlidingPanel>

            <Footer />
        </>
    );
};

export default ProductList;
