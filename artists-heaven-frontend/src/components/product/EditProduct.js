import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

const EditProduct = () => {
    const { id } = useParams(); // Obtener el ID del producto desde la URL
    const navigate = useNavigate();

    const [product, setProduct] = useState({
        name: "",
        description: "",
        price: "",
        sizes: {},
        categories: [],
        images: [],
    });

    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [categoriesList, setCategoriesList] = useState([]);
    const [newImages, setNewImages] = useState([]);
    const [removedImages, setRemovedImages] = useState([]);
    const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));
    const rol = localStorage.getItem("role");


    // Cargar datos del producto al montar el componente
    useEffect(() => {
        if (rol !== "ADMIN") {
            // Si el rol no es ADMIN, no realizar las peticiones
            setErrorMessage("No tienes permisos para acceder a esta página.");
            return; // No hacer nada más si no es ADMIN
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
            .then((data) => {
                data.sizes = data.size; // En el product viene como size
                data.categories = data.categories.map(category => category.id); // Obtener solo los IDs de las categorías
                setProduct(data); // Establecer los valores iniciales del producto
                setLoading(false);
            })
            .catch((error) => {
                setErrorMessage("Error cargando los datos del producto.");
                console.error(error);
                setLoading(false);
            });
    }, [id]);

    // Cargar lista de categorías
    useEffect(() => {
        if (rol !== "ADMIN") {
            // Si el rol no es ADMIN, no realizar las peticiones
            setErrorMessage("No tienes permisos para acceder a esta página.");
            return; // No hacer nada más si no es ADMIN
        }
        fetch("/api/product/categories")
            .then((response) => response.json())
            .then((data) => setCategoriesList(data))
            .catch((error) => console.error("Error cargando las categorías:", error));
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct({ ...product, [name]: value });
    };

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);
        setNewImages([...newImages, ...files]);
    };

    const handleRemoveImage = async (image) => {
        setProduct((prev) => ({
            ...prev,
            images: prev.images.filter((img) => img !== image),
        }));
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

    const handleSubmit = async (e) => {
        e.preventDefault();

        const formData = new FormData();
        formData.append("product", new Blob([JSON.stringify(product)], { type: "application/json" }));

        newImages.forEach((file) => {
            formData.append("newImages", file);
        });

        removedImages.forEach((file) => {
            formData.append("removedImages", file);
        });

        try {
            const response = await fetch(`/api/product/edit/${id}`, {
                method: "PUT",
                body: formData,
                headers: {
                    'Authorization': `Bearer ${authToken}`
                },
            });

            if (response.ok) {
                setSuccessMessage("Producto actualizado correctamente.");
                navigate("/admin/products/store");
            } else {
                throw new Error("Error al actualizar el producto: " + response.statusText);
            }
        } catch (error) {
            setErrorMessage("Error actualizando el producto.");
        }
    };

    if (loading) {
        return <div>Cargando...</div>;
    }

    if (rol !== "ADMIN") {
        // Si el rol no es ADMIN, no hacemos la petición
        return "No tienes permisos para acceder a esta página";
    }

    return (
        <div className="min-h-screen bg-gray-50 py-10 px-4 sm:px-10">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">Editar Producto</h2>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Nombre del producto</label>
                        <input
                            type="text"
                            name="name"
                            value={product.name}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                            required
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Descripción</label>
                        <textarea
                            name="description"
                            rows="3"
                            value={product.description}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        ></textarea>
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Precio (€)</label>
                        <input
                            type="number"
                            step="0.01"
                            name="price"
                            value={product.price}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                            required
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">Tallas y Stock</label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                            {["XS", "S", "M", "L", "XL", "XXL"].map((size) => (
                                <div key={size}>
                                    <label className="block text-sm text-gray-500">{size}</label>
                                    <input
                                        type="number"
                                        min="0"
                                        value={product.sizes[size] || ""}
                                        onChange={(e) => handleSizeChange(e, size)}
                                        className="w-full border rounded px-3 py-1 text-sm"
                                    />
                                </div>
                            ))}
                        </div>
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">Categorías</label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                            {categoriesList.map((cat) => (
                                <label key={cat.id} className="flex items-center text-sm text-gray-600">
                                    <input
                                        type="checkbox"
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
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">Imágenes actuales</label>
                        <div className="flex flex-wrap gap-4">
                            {product.images.map((image, idx) => (
                                <div key={idx} className="relative w-24 h-24">
                                    <img src={`/api/product${image}`} alt={`Imagen ${idx}`} className="w-full h-full object-content rounded" />
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveImage(image)}
                                        className="absolute top-0 right-0 bg-red-500 text-white text-xs rounded-full p-1"
                                    >
                                        ✕
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">Subir nuevas imágenes</label>
                        <input
                            type="file"
                            multiple
                            onChange={handleImageChange}
                            className="block w-full text-sm text-gray-500"
                        />
                    </div>
                    <button type="submit" className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300">
                        Guardar Cambios
                    </button>

                    {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
                    {errorMessage && <div className="text-red-600 text-sm mt-3">{errorMessage}</div>}
                </form>
            </div>

            {/* Mensajes */}
            {successMessage && <div className="alert alert-success mt-3">{successMessage}</div>}
            {errorMessage && <div className="alert alert-danger mt-3">{errorMessage}</div>}
        </div>
    );
};

export default EditProduct;