import React, { useState, useEffect, useCallback, useMemo, useRef } from "react";
import { checkTokenExpiration } from "../../utils/authUtils";
import { useParams, useNavigate } from "react-router-dom";
import SimpleMDE from "react-simplemde-editor";
import "easymde/dist/easymde.min.css";
import { useTranslation } from 'react-i18next';

const EditProduct = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [product, setProduct] = useState({
        name: "",
        description: "",
        price: "",
        categories: [],
        collectionId: null,
        section: "",
        composition: "",
        shippingDetails: "",
    });

    const [colors, setColors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [categoriesList, setCategoriesList] = useState([]);
    const [collectionsList, setCollectionsList] = useState([]);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const rol = localStorage.getItem("role");

    const { t, i18n } = useTranslation();
    const language = i18n.language;

    useEffect(() => {
        setValidationErrors({});
    }, [language]);

    const [validationErrors, setValidationErrors] = useState({});

    const markdownOptions = useMemo(() => ({
        spellChecker: false,
        status: false,
        autofocus: false,
        toolbar: [
            "bold", "italic", "heading", "|",
            "quote", "unordered-list", "ordered-list", "|",
            "link", "|", "guide",
        ],
        toolbarTips: true,
        maxHeight: "300px",
    }), []);

    const handleCompositionChange = useCallback((value) => {
        setProduct(prev => ({ ...prev, composition: value }));
    }, []);

    const handleCareChange = useCallback((value) => {
        setProduct(prev => ({ ...prev, shippingDetails: value }));
    }, []);

    // 🔹 Cargar producto + colores desde backend
    useEffect(() => {
        if (rol !== "ADMIN") {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            setLoading(false);
            return;
        }
        fetch(`http://localhost:8080/api/product/details/${id}`, { method: "GET" })
            .then((response) => {
                if (!response.ok) throw new Error("Error al obtener el producto");
                return response.json();
            })
            .then((productData) => {
                const p = productData.data;
                p.categories = p.categories.map(c => c.id);
                setProduct(p);
                setColors(
                    p.colors.map(c => ({
                        ...c,
                        colorId: c.colorId,
                        images: c.images || [],
                        sizes: c.sizes || { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
                        availableUnits: c.availableUnits || 0,
                        modelFile: null,
                    }))
                );
                setLoading(false);
            })
            .catch(() => {
                setErrorMessage("Error cargando los datos del producto.");
                setLoading(false);
            });
    }, [id, rol]);

    // 🔹 Cargar categorías y colecciones
    useEffect(() => {
        if (!checkTokenExpiration || rol !== 'ADMIN') {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            return;
        }
        const fetchCategories = async () => {
            const response = await fetch("http://localhost:8080/api/product/categories");
            const data = await response.json();
            setCategoriesList(data.data);
        };
        const fetchCollections = async () => {
            const response = await fetch("http://localhost:8080/api/product/allCollections", {
                headers: { 'Authorization': `Bearer ${authToken}` },
            });
            const data = await response.json();
            setCollectionsList(data.data);
        };
        fetchCategories();
        fetchCollections();
    }, [checkTokenExpiration, rol]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({ ...product, [name]: value });
    };

    const formRef = useRef(null);

    // 🔹 Enviar actualización
    const handleSubmit = async (e) => {
        e.preventDefault();
        setValidationErrors({});
        setErrorMessage("");

        let errors = {};
        if (!product.name) errors.name = t('productForm.error.requiredName');
        if (!product.description) errors.description = t('productForm.error.requiredDescription');
        if (product.price < 0) errors.price = t('productForm.error.invalidPrice');
        if (!product.price) errors.price = t('productForm.error.requiredPrice');
        if (product.categories.length === 0) errors.categories = t('productForm.error.requiredCategories');
        if (!product.section) errors.section = t('productForm.error.requiredSection');
        if (!product.composition) errors.composition = t('productForm.error.requiredComposition');
        if (!product.shippingDetails) errors.shippingDetails = t('productForm.error.requiredShippingDetails');

        colors.forEach((c, idx) => {
            if (!c.colorName || c.colorName.trim() === "") {
                errors[`color_${idx}_name`] = t('productForm.error.requiredColorName');
            }

            if (!c.images || c.images.length === 0) {
                errors[`color_${idx}_images`] = t('productForm.error.requiredImages');
            }

            if (product.section === "ACCESSORIES") {
                if (!c.availableUnits || c.availableUnits < 1) {
                    errors[`color_${idx}_availableUnits`] = t('productForm.error.requiredAvailableUnits');
                }
            } else {
                if (!c.sizes || Object.values(c.sizes).every(v => v === 0)) {
                    errors[`color_${idx}_sizes`] = t('productForm.error.requiredSizes');
                }
            }
        });

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            const firstError = Object.keys(errors)[0];
            const el = formRef.current.querySelector(`[name="${firstError}"]`);
            if (el) el.scrollIntoView({ behavior: "smooth", block: "center" });
            return;
        }

        const productToSend = {
            ...product, colors: colors.map(c => ({
                colorId: c.colorId || null,
                colorName: c.colorName,
                hexCode: c.hexCode,
                images: c.images.filter(img => typeof img === "string"), // solo rutas
                sizes: product.section === "ACCESSORIES" ? null : c.sizes,
                availableUnits: product.section === "ACCESSORIES" ? c.availableUnits : null,
                modelReference: c.modelReference || null,
            }))
        };

        const formData = new FormData();
        formData.append("product", new Blob([JSON.stringify(productToSend)], { type: "application/json" }));
        colors.forEach((c, i) => {
            c.images.forEach(img => {
                if (img instanceof File) {
                    formData.append(`colorImages_${i}`, img);
                }
            });

            if (c.modelFile) {
                formData.append(`colorModel_${i}`, c.modelFile);
            }
        });

        try {
            const response = await fetch(`http://localhost:8080/api/product/edit/${id}?lang=${language}`, {
                method: "PUT",
                body: formData,
                headers: { 'Authorization': `Bearer ${authToken}` },
            });
            const result = await response.json();
            if (response.ok) {
                setSuccessMessage("Producto actualizado correctamente.");
                navigate("/admin/products/store");
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setErrorMessage(err.message);
        }
    };

    if (loading) return <div>Cargando...</div>;
    if (rol !== "ADMIN") return "No tienes permisos para acceder";

    return (
        <div className="min-h-screen bg-gray-50 py-10 px-4 sm:px-10">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">{t('editProductForm.title')}</h2>
                <form ref={formRef} onSubmit={handleSubmit} className="space-y-6">
                    {/* Campos básicos */}
                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('editProductForm.name')}</label>
                        <input
                            type="text"
                            name="name"
                            value={product.name}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"

                        />
                        {validationErrors.name && (
                            <p className="text-red-600 text-sm">{validationErrors.name}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('editProductForm.description')}</label>
                        <textarea
                            name="description"
                            rows="3"
                            value={product.description}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        ></textarea>
                        {validationErrors.description && (
                            <p className="text-red-600 text-sm">{validationErrors.description}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('editProductForm.price')} (€)</label>
                        <input
                            type="number"
                            step="0.01"
                            name="price"
                            value={product.price}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"

                        />
                        {validationErrors.price && (
                            <p className="text-red-600 text-sm">{validationErrors.price}</p>
                        )}
                    </div>

                    {/* Sección */}
                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.section')}</label>
                        <select
                            name="section"
                            value={product.section}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"

                        >
                            <option value="">{t('editProductForm.chooseOneOption')}</option>
                            <option value="TSHIRT">{t('editProductForm.tshirt')}</option>
                            <option value="PANTS">{t('editProductForm.pants')}</option>
                            <option value="ACCESSORIES">{t('editProductForm.accessories')}</option>
                            <option value="HOODIES">{t('editProductForm.hoodies')}</option>
                        </select>
                        {validationErrors.section && (
                            <p className="text-red-600 text-sm">{validationErrors.section}</p>
                        )}
                    </div>
                    {/* Categorías */}
                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.categories')}</label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                            {categoriesList.map((cat) => (
                                <label key={cat.id} className="flex items-center text-sm text-gray-600">
                                    <input
                                        type="checkbox"
                                        name="categories"
                                        checked={product.categories.includes(cat.id)}
                                        onChange={(e) => {
                                            const checked = e.target.checked;
                                            setProduct((prev) => ({
                                                ...prev,
                                                categories: checked
                                                    ? [...prev.categories, cat.id]
                                                    : prev.categories.filter((id) => id !== cat.id),
                                            }));
                                        }}
                                        className="mr-2"
                                    />
                                    {cat.name}
                                </label>
                            ))}
                        </div>
                        {validationErrors.categories && (
                            <p className="text-red-600 text-sm">{validationErrors.categories}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">
                            {t('editProductForm.collections')}
                        </label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                            {collectionsList.map((coll) => (
                                <label key={coll.id} className="flex items-center text-sm text-gray-600">
                                    <input
                                        type="checkbox"
                                        name="collections"
                                        checked={product.collectionId === coll.id}
                                        onChange={() => {
                                            setProduct((prev) => ({
                                                ...prev,
                                                collectionId: prev.collectionId === coll.id ? null : coll.id,
                                            }));
                                        }}
                                        className="mr-2"
                                    />
                                    {coll.name}
                                </label>
                            ))}
                        </div>
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">
                            {t('editProductForm.composition')}
                        </label>
                        <SimpleMDE
                            id="composition-editor"
                            value={product.composition}
                            onChange={handleCompositionChange}
                            style={{ border: "1px solid #ccc", borderRadius: "4px" }}
                            options={markdownOptions}
                        />
                        {validationErrors.composition && (
                            <p className="text-red-600 text-sm">{validationErrors.composition}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">
                            {t('editProductForm.shippingDetails')}
                        </label>
                        <SimpleMDE
                            id="care-editor"
                            value={product.shippingDetails}
                            onChange={handleCareChange}
                            style={{ border: "1px solid #ccc", borderRadius: "4px" }}
                            options={markdownOptions}
                        />
                        {validationErrors.shippingDetails && (
                            <p className="text-red-600 text-sm">{validationErrors.shippingDetails}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.colors')}</label>
                        {colors.map((color, index) => (
                            <div key={index} className="mb-4 border p-2 rounded">
                                <div className="flex justify-between items-center">
                                    <span className="font-semibold">Color #{index + 1}</span>
                                    <button
                                        type="button"
                                        onClick={() => setColors(colors.filter((_, i) => i !== index))}
                                        className="text-red-600 text-sm font-bold hover:underline"
                                    >
                                        {t('editProductForm.removeColor')}
                                    </button>
                                </div>
                                <input
                                    type="text"
                                    placeholder="Nombre del color"
                                    value={color.colorName}
                                    onChange={(e) => {
                                        const updated = [...colors];
                                        updated[index].colorName = e.target.value;
                                        setColors(updated);
                                    }}
                                    className="border rounded px-2 py-1 mr-2"
                                />
                                <input
                                    type="color"
                                    value={color.hexCode || "#000000"}
                                    onChange={(e) => {
                                        const updated = [...colors];
                                        updated[index].hexCode = e.target.value;
                                        setColors(updated);
                                    }}
                                    className="mr-2"
                                />
                                <input
                                    type="file"
                                    multiple
                                    onChange={(e) => {
                                        const updated = [...colors];
                                        updated[index].images = [...updated[index].images, ...Array.from(e.target.files)];
                                        setColors(updated);
                                    }}
                                />

                                <div className="mt-2">
                                    <label className="block text-sm font-semibold">{t('editProductForm.3dModel')}</label>

                                    {color.modelReference && !color.modelFile && (
                                        <p className="text-sm text-gray-600">
                                            Modelo actual: {color.modelReference.replace("/product_media/", "")}
                                        </p>
                                    )}

                                    <input
                                        type="file"
                                        accept=".glb,.gltf"
                                        onChange={(e) => {
                                            const file = e.target.files[0];
                                            if (!file) return;

                                            const allowed = ["glb", "gltf"];
                                            const ext = file.name.split(".").pop().toLowerCase();
                                            if (!allowed.includes(ext)) {
                                                setValidationErrors(prev => ({
                                                    ...prev,
                                                    [`color_${index}_model`]: "Solo .glb o .gltf"
                                                }));
                                                return;
                                            }

                                            const updated = [...colors];
                                            updated[index].modelFile = file;
                                            updated[index].modelReference = file.name;
                                            setColors(updated);
                                        }}
                                        className="mt-1 block text-sm text-gray-500"
                                    />

                                    {validationErrors[`color_${index}_name`] && (
                                        <p className="text-red-600 text-sm">{validationErrors[`color_${index}_name`]}</p>
                                    )}
                                    {validationErrors[`color_${index}_images`] && (
                                        <p className="text-red-600 text-sm">{validationErrors[`color_${index}_images`]}</p>
                                    )}
                                </div>

                                {/* Vista previa */}
                                {color.images.length > 0 && (
                                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4">
                                        {color.images.map((img, imgIndex) => (
                                            <div key={imgIndex} className="text-center relative">
                                                <img
                                                    src={typeof img === "string" ? `http://localhost:8080/api/product${img}` : URL.createObjectURL(img)}
                                                    alt={`preview-${index}-${imgIndex}`}
                                                    className="w-full h-24 object-cover rounded shadow mb-2"
                                                />

                                                {/* Botón eliminar */}
                                                <button
                                                    type="button"
                                                    onClick={() => {
                                                        const updated = [...colors];
                                                        updated[index].images = updated[index].images.filter((_, i) => i !== imgIndex);
                                                        setColors(updated);
                                                    }}
                                                    className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs shadow hover:bg-red-600"
                                                >
                                                    ✕
                                                </button>

                                                {/* Botones de orden */}
                                                <div className="flex justify-center gap-1">
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            const updated = [...colors];
                                                            const imgs = [...updated[index].images];
                                                            const [moved] = imgs.splice(imgIndex, 1);
                                                            imgs.splice(imgIndex - 1, 0, moved);
                                                            updated[index].images = imgs;
                                                            setColors(updated);
                                                        }}
                                                        disabled={imgIndex === 0}
                                                        className="px-2 py-1 bg-gray-300 rounded text-xs disabled:opacity-50"
                                                    >
                                                        ↑
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            const updated = [...colors];
                                                            const imgs = [...updated[index].images];
                                                            const [moved] = imgs.splice(imgIndex, 1);
                                                            imgs.splice(imgIndex + 1, 0, moved);
                                                            updated[index].images = imgs;
                                                            setColors(updated);
                                                        }}
                                                        disabled={imgIndex === color.images.length - 1}
                                                        className="px-2 py-1 bg-gray-300 rounded text-xs disabled:opacity-50"
                                                    >
                                                        ↓
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {/* Stock */}
                                {product.section === "ACCESSORIES" ? (
                                    <div className="mt-2">
                                        <label className="block text-sm">{t('editProductForm.availableUnits')}</label>
                                        <input
                                            type="number"
                                            min="0"
                                            value={color.availableUnits}
                                            onChange={(e) => {
                                                const updated = [...colors];
                                                updated[index].availableUnits = parseInt(e.target.value) || 0;
                                                setColors(updated);
                                            }}
                                            className="border rounded px-2 py-1"
                                        />
                                    </div>
                                ) : (
                                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 mt-2">
                                        {["XS", "S", "M", "L", "XL", "XXL"].map((size) => (
                                            <div key={size}>
                                                <label className="block text-sm text-gray-500">{size}</label>
                                                <input
                                                    type="number"
                                                    min="0"
                                                    value={color.sizes[size] || 0}
                                                    onChange={(e) => {
                                                        const updated = [...colors];
                                                        updated[index].sizes[size] = parseInt(e.target.value) || 0;
                                                        setColors(updated);
                                                    }}
                                                    className="w-full border rounded px-3 py-1 text-sm"
                                                />
                                            </div>
                                        ))}
                                    </div>
                                )}
                                {validationErrors[`color_${index}_sizes`] && (
                                    <p className="text-red-600 text-sm">{validationErrors[`color_${index}_sizes`]}</p>
                                )}
                                {validationErrors[`color_${index}_availableUnits`] && (
                                    <p className="text-red-600 text-sm">{validationErrors[`color_${index}_availableUnits`]}</p>
                                )}
                            </div>

                        ))}
                        <button
                            type="button"
                            onClick={() =>
                                setColors([...colors, {
                                    colorName: "",
                                    hexCode: "",
                                    images: [],
                                    sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
                                    availableUnits: 0
                                }])
                            }
                            className="bg-gray-300 px-2 py-1 rounded"
                        >
                            + {t('editProductForm.addColor')}
                        </button>
                    </div>

                    {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
                    {errorMessage && <div className="text-red-600 text-sm mt-3">{errorMessage}</div>}

                    <button type="submit" className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300">
                        {t('editProductForm.saveChanges')}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default EditProduct;