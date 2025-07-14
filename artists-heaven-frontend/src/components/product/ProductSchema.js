import { useState, useEffect, useMemo } from "react";
import Footer from "../Footer";
import { Link } from "react-router-dom";
import ProductCard from "./ProductCard";
import SlidingPanel from "./SlidingPanel";

const ProductSchema = ({ endpoint, title }) => {
    const [products, setProducts] = useState([]);
    const [isFilterOpen, setIsFilterOpen] = useState(false);

    const [orderBy, setOrderBy] = useState("default");
    const [selectedSizes, setSelectedSizes] = useState([]);
    const [priceRange, setPriceRange] = useState({ min: 0, max: 1000 });
    const [selectedCategories, setSelectedCategories] = useState([]);

    const [tempOrderBy, setTempOrderBy] = useState(orderBy);
    const [tempSelectedSizes, setTempSelectedSizes] = useState(selectedSizes);
    const [tempPriceRange, setTempPriceRange] = useState(priceRange);
    const [tempSelectedCategories, setTempSelectedCategories] = useState(selectedCategories);

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
            }, 60); // Delay suave
        });

    }, [products]);

    useEffect(() => {
        fetch(endpoint)
            .then((response) => {
                if (!response.ok) throw new Error("Error al obtener productos");
                return response.json();
            })
            .then((data) => {
                setProducts(data);
                const prices = data.map((p) => p.price);
                const maxPrice = Math.max(...prices);
                setMaxAbsolutePrice(maxPrice);
                setPriceRange({ min: 0, max: maxPrice });
                setTempPriceRange({ min: 0, max: maxPrice });
            })
            .catch((error) => console.error("Error:", error));
    }, [endpoint]);

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
            result = result.filter((product) => {
                if (product.section === "ACCESSORIES") return true;
                return selectedSizes.some((size) => product.sizes?.[size] > 0);
            });
        }

        result = result.filter(
            (product) => product.price >= priceRange.min && product.price <= priceRange.max
        );

        if (selectedCategories.length > 0) {
            result = result.filter((product) =>
                product.categories?.some((cat) => selectedCategories.includes(cat))
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
    }, [products, selectedSizes, priceRange, selectedCategories, orderBy]);

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
    };

    useEffect(() => {
        if (isFilterOpen) {
            setTempOrderBy(orderBy);
            setTempSelectedSizes(selectedSizes);
            setTempPriceRange(priceRange);
            setTempSelectedCategories(selectedCategories);
        }
    }, [isFilterOpen, orderBy, selectedSizes, priceRange, selectedCategories]);

    return (
        <>
            <div
                style={{
                    padding: "20px",
                    color: "white",
                    minHeight: "100vh",
                    marginTop: '40px',
                    opacity: isVisible ? 1 : 0,
                    transition: "opacity 0.7s ease-in-out",
                }}
            >
                <div className="flex justify-between">
                    <p className="custom-font-shop-regular mb-4" style={{ color: "black" }}>
                        {filteredProducts.length} productos
                    </p>
                    <p
                        className="custom-font-shop-regular mb-4 cursor-pointer"
                        style={{ color: "black" }}
                        onClick={() => setIsFilterOpen(true)}
                    >
                        Filtrar y buscar
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
                onClose={() => setIsFilterOpen(false)}
                maxWidth="500px"
            >
                <h3
                    className="p-4 custom-font-shop custom-font-shop-black"
                    style={{ borderBottom: "1px solid #e5e7eb" }}
                >
                    Filtros
                </h3>

                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <label htmlFor="orderBy" className="block mb-2 font-semibold">
                        Ordenar
                    </label>
                    <select
                        id="orderBy"
                        value={tempOrderBy}
                        onChange={(e) => setTempOrderBy(e.target.value)}
                        className="w-full border border-gray-300 rounded p-2"
                    >
                        <option value="default">Por defecto</option>
                        <option value="az">Ordenado de la A a la Z</option>
                        <option value="za">Ordenado de la Z a la A</option>
                        <option value="priceHighLow">Ordenar precio: más alto a más bajo</option>
                        <option value="priceLowHigh">Ordenar precio: más bajo a más alto</option>
                    </select>
                </div>

                {hasNonAccessoryProducts && (
                    <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                        <p className="font-semibold mb-2">Tallas</p>
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
                    <p className="font-semibold mb-2">Categorías</p>
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
                    <p className="font-semibold mb-2">Rango de precio</p>
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
                        Precio: 0€ - {tempPriceRange.max.toFixed(2)}€
                    </p>
                </div>

                <div className="p-4 flex w-full gap-4">
                    <button
                        onClick={handleClearFilters}
                        className="border border-black cursor-pointer px-8 py-6 flex-1"                    >
                        <span className="custom-font-shop custom-font-shop-black transition-transform duration-300 ease-in-out hover:translate-x-2">
                            Eliminar filtros
                        </span>
                    </button>
                    <button
                        onClick={handleApplyFilters}
                        className="border border-black cursor-pointer px-8 py-6 flex-1"
                        style={{ backgroundColor: "black" }}
                    >
                        <span className="custom-font-shop transition-transform duration-300 ease-in-out hover:translate-x-2">
                            Aplicar filtros
                        </span>
                    </button>
                </div>

            </SlidingPanel>

            <Footer />
        </>
    );
};

export default ProductSchema;
