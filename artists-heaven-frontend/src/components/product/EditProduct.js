import React, { useState, useEffect, useCallback, useMemo, useRef } from "react";
import { checkTokenExpiration } from "../../utils/authUtils";
import { useParams, useNavigate } from "react-router-dom";
import SimpleMDE from "react-simplemde-editor";
import "easymde/dist/easymde.min.css";
import { useTranslation } from 'react-i18next';

const EditProduct = () => {
    const { id } = useParams(); // Obtener el ID del producto desde la URL
    const navigate = useNavigate();

    const [product, setProduct] = useState({
        name: "",
        description: "",
        price: "",
        sizes: {},
        availableUnits: 1, // Unidades disponibles solo para accesorios
        categories: [],
        collectionId: null,
        images: [],
        section: "",
        composition: "",
        shippingDetails: "",
        modelReference: ""
    });

    const [loading, setLoading] = useState(true);
    const [modelFile, setModelFile] = useState(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [categoriesList, setCategoriesList] = useState([]);
    const [collectionsList, setCollectionsList] = useState([]);
    const [newImages, setNewImages] = useState([]);
    const [removedImages, setRemovedImages] = useState([]);
    const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));
    const rol = localStorage.getItem("role");

    const { t, i18n } = useTranslation();
    const language = i18n.language;

    useEffect(() => {
        setValidationErrors({})
    }, [language]);

    const [validationErrors, setValidationErrors] = useState({});

    const markdownOptions = useMemo(() => ({
        spellChecker: false,
        placeholder: "Ejemplo: - Lavar a mano\n- No usar lejía",
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

    const handleOriginChange = useCallback((value) => {
        setProduct(prev => ({ ...prev, origin: value }));
    }, []);

    const handleCareChange = useCallback((value) => {
        const cleaned = value.replace(/[#*_\-\n]/g, "").trim();
        setProduct(prev => ({
            ...prev,
            shippingDetails: cleaned === "" ? "" : value
        }));
    }, []);

    // Estado para controlar el orden local de las imágenes
    const [images, setImages] = useState([]);

    // Función para mover imágenes dentro del array images
    const moveImage = (fromIndex, toIndex) => {
        if (toIndex < 0 || toIndex >= images.length) return; // evitar índices fuera de rango
        setImages((prevImages) => {
            const updated = [...prevImages];
            const [moved] = updated.splice(fromIndex, 1);
            updated.splice(toIndex, 0, moved);
            return updated;
        });
    };

    // Cargar datos del producto al montar el componente
    useEffect(() => {
        if (rol !== "ADMIN") {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            setLoading(false);
            return;
        }
        fetch(`/api/product/details/${id}`, {
            method: "GET",
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Error al obtener el producto: " + response.statusText);
                }
                return response.json();
            })

            .then((productData) => {
                productData.data.categories = productData.data.categories.map(category => category.id); // Obtener solo los IDs de las categorías
                setProduct(productData.data); // Establecer los valores iniciales del producto
                setImages(productData.data.images || []); // Inicializar estado local de imágenes con las actuales
                setLoading(false);
            })
            .catch((error) => {
                setErrorMessage("Error cargando los datos del producto.");
                console.error(error);
                setLoading(false);
            });
    }, [id, rol]);

    console.log(product);

    // Cargar lista de categorías
    useEffect(() => {
        if (!checkTokenExpiration || rol !== 'ADMIN') {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            return;  // Sale de la función si no tiene permisos
        }

        const fetchCategories = async () => {
            try {
                const response = await fetch("/api/product/categories");
                if (!response.ok) {
                    throw new Error('Error al cargar las categorías');
                }
                const categoriesData = await response.json();
                setCategoriesList(categoriesData.data);
            } catch (error) {
                console.error("Error cargando las categorías:", error);
                setErrorMessage("Error al cargar las categorías.");
            }
        };

        const fetchCollections = async () => {
            try {
                const response = await fetch("/api/admin/allCollections", {
                    headers: {
                        'Authorization': `Bearer ${authToken}`,
                    }
                });
                if (!response.ok) {
                    throw new Error('Error al cargar las colecciones');
                }
                const collectionData = await response.json();
                setCollectionsList(collectionData.data);
            } catch (error) {
                console.error("Error cargando las colecciones:", error);
                setErrorMessage("Error al cargar las colecciones.");
            }
        };

        fetchCategories();
        fetchCollections();
    }, [checkTokenExpiration, rol]);

    // Sin cambios en handleChange y demás handlers
    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({ ...product, [name]: value });
    };

    const handleModelReferenceChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const allowedExtensions = ["glb", "gltf"];
        const fileExtension = file.name.split(".").pop().toLowerCase();

        if (!allowedExtensions.includes(fileExtension)) {
            setValidationErrors((prev) => ({
                ...prev,
                modelReference: "Solo se permiten archivos .glb o .gltf",
            }));
            return;
        }

        setValidationErrors((prev) => ({ ...prev, modelReference: "" }));
        setModelFile(file);
        setProduct((prev) => ({
            ...prev,
            modelReference: file.name
        }));
    };

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);
        setNewImages([...newImages, ...files]);
    };

    const handleRemoveImage = async (image) => {
        // Actualizamos las imágenes locales
        setImages((prev) => prev.filter((img) => img !== image));
        // También actualizamos el producto
        setProduct((prev) => ({
            ...prev,
            images: prev.images.filter((img) => img !== image),
        }));
        // Añadimos a removedImages para el backend
        const response = await fetch(`/api/product${image}`);
        const blob = await response.blob();
        const file = new File([blob], image.split('/').pop(), { type: blob.type });
        setRemovedImages((prev) => [...prev, file]);
    };

    const handleSizeChange = (e, size) => {
        const { value } = e.target;
        setProduct((prev) => ({
            ...prev,
            sizes: {
                ...prev.sizes,
                [size]: value ? parseInt(value, 10) : 0,
            },
        }));
    };

    const formRef = useRef(null);

    console.log("EO")

    const handleSubmit = async (e) => {
        e.preventDefault();

        setValidationErrors({});
        setErrorMessage("");

        let errors = {};

        if (!product.name) errors.name = t('productForm.error.requiredName');
        if (!product.description) errors.description = t('productForm.error.requiredDescription');
        if (product.price < 0) errors.price = t('productForm.error.invalidPrice');
        if (!product.price) errors.price = t('productForm.error.requiredPrice');
        if (
            product.section !== "ACCESSORIES" &&
            (!product.sizes || Object.values(product.sizes).every(value => value === 0))
        ) {
            errors.sizes = t('productForm.error.requiredSizes');
        }
        if (product.categories.length === 0) errors.categories = t('productForm.error.requiredCategories');
        if (!product.section) errors.section = t('productForm.error.requiredSection');
        if (product.section === "ACCESSORIES" && (!product.availableUnits || product.availableUnits < 1)) {
            errors.availableUnits = t('productForm.error.requiredAvailableUnits');
        }
        if (!product.composition) errors.composition = t('productForm.error.requiredComposition');
        if (!product.shippingDetails) errors.shippingDetails = t('productForm.error.requiredShippingDetails');
        if (product.images.length === 0 && newImages.length === 0) errors.images = t('productForm.error.requiredImages');


        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            const firstErrorField = Object.keys(errors)[0];
            console.log(firstErrorField)
            const element = formRef.current.querySelector(`[name="${firstErrorField}"]`);
            console.log(element);
            if (element) {
                element.scrollIntoView({ behavior: "smooth", block: "center" });
                element.focus();
            }

            return;
        }

        // Actualizamos product.images con el orden actual de imágenes locales antes de enviar
        const updatedProduct = { ...product, images };

        const formData = new FormData();
        formData.append("product", new Blob([JSON.stringify(updatedProduct)], { type: "application/json" }));

        newImages.forEach((file) => {
            formData.append("newImages", file);
        });

        removedImages.forEach((file) => {
            formData.append("removedImages", file);
        });

        if (modelFile) {
            formData.append("model", modelFile);
        }

        formData.append(
            "reorderedImages",
            new Blob([JSON.stringify(images)], { type: "application/json" })
        );

        try {
            const response = await fetch(`/api/product/edit/${id}?lang=${language}`, {
                method: "PUT",
                body: formData,
                headers: {
                    'Authorization': `Bearer ${authToken}`
                },
            });

            const result = await response.json();
            const errorMessage = result.message;
            console.log(result);

            if (response.ok) {
                setSuccessMessage("Producto actualizado correctamente.");
                navigate("/admin/products/store");
            } else {
                throw new Error(errorMessage);
            }
        } catch (error) {
            setErrorMessage(error.message);;
        }
    };

    if (loading) {
        return <div>Cargando...</div>;
    }

    if (rol !== "ADMIN") {
        return "No tienes permisos para acceder a esta página";
    }

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

                    {/* Tallas o unidades */}
                    {product.section !== "ACCESSORIES" && (
                        <div>
                            <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.size&stock')}</label>
                            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                                {["XS", "S", "M", "L", "XL", "XXL"].map((size) => (
                                    <div key={size}>
                                        <label className="block text-sm text-gray-500">{size}</label>
                                        <input
                                            type="number"
                                            name="sizes"
                                            min="0"
                                            value={product.sizes[size] || ""}
                                            onChange={(e) => handleSizeChange(e, size)}
                                            className="w-full border rounded px-3 py-1 text-sm"
                                        />
                                    </div>
                                ))}
                            </div>
                            {validationErrors.sizes && (
                                <p className="text-red-600 text-sm">{validationErrors.sizes}</p>
                            )}
                        </div>
                    )}

                    {product.section === "ACCESSORIES" && (
                        <div>
                            <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.availableUnits')}</label>
                            <input
                                type="number"
                                min="1"
                                name="availableUnits"
                                value={product.availableUnits}
                                onChange={(e) => setProduct({ ...product, availableUnits: e.target.value })}
                                className="w-full border rounded px-4 py-2 text-sm"

                            />
                            {validationErrors.availableUnits && (
                                <p className="text-red-600 text-sm">{validationErrors.availableUnits}</p>
                            )}
                        </div>
                    )}

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

                    {/* Imágenes actuales con botones ↑ ↓ para mover */}
                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.actualImages')}</label>
                        <div className="flex flex-wrap gap-4">
                            {images.map((image, idx) => (
                                <div key={idx} className="relative w-24 h-24 flex flex-col items-center">
                                    <img
                                        src={`/api/product${image}`}
                                        alt={`Imagen ${idx}`}
                                        className="w-full h-full object-contain rounded"
                                    />
                                    <div className="flex gap-1 mt-1">
                                        <button
                                            type="button"
                                            onClick={() => moveImage(idx, idx - 1)}
                                            disabled={idx === 0}
                                            className="bg-gray-300 hover:bg-gray-400 rounded px-1 text-sm"
                                            title="Mover arriba"
                                        >
                                            ↑
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => moveImage(idx, idx + 1)}
                                            disabled={idx === images.length - 1}
                                            className="bg-gray-300 hover:bg-gray-400 rounded px-1 text-sm"
                                            title="Mover abajo"
                                        >
                                            ↓
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => handleRemoveImage(image)}
                                            className="bg-red-500 hover:bg-red-600 text-white rounded px-1 text-sm"
                                            title="Eliminar imagen"
                                        >
                                            ✕
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Subir nuevas imágenes */}
                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.uploadNewImages')}</label>
                        <input
                            type="file"
                            name="images"
                            multiple
                            onChange={handleImageChange}
                            className="block w-full text-sm text-gray-500"
                        />
                        {validationErrors.images && (
                            <p className="text-red-600 text-sm">{validationErrors.images}</p>
                        )}
                    </div>

                    {product.modelReference && !modelFile && (
                        <p className="text-sm text-gray-600">
                            Modelo actual:
                            {product.modelReference.replace("/product_media/", "")}
                        </p>
                    )}

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('editProductForm.upload3dModel')}</label>
                        <input
                            type="file"
                            onChange={handleModelReferenceChange}
                            className="block w-full text-sm text-gray-500"
                        />
                        {validationErrors.modelReference && (
                            <p className="text-red-600 text-sm">{validationErrors.modelReference}</p>
                        )}
                    </div>


                    {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
                    {errorMessage && <div className="text-red-600 text-sm mt-3">{errorMessage}</div>}

                    <button
                        type="submit"
                        className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                    >
                        Guardar Cambios
                    </button>
                </form>

            </div>
        </div>
    );
};

export default EditProduct;
