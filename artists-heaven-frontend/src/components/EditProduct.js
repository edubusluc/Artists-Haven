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
                navigate("/product/all"); // Redirigir al listado de productos
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
        <div className="container mt-5">
            <h2>Editar Producto</h2>
            <form onSubmit={handleSubmit}>
                {/* Información del producto */}
                <div className="mb-3">
                    <label htmlFor="name" className="form-label">Nombre:</label>
                    <input
                        type="text"
                        className="form-control"
                        id="name"
                        name="name"
                        value={product.name}
                        onChange={handleChange}
                        required
                    />
                </div>

                {/* Descripción */}
                <div className="mb-3">
                    <label htmlFor="description" className="form-label">Descripción:</label>
                    <textarea
                        className="form-control"
                        id="description"
                        name="description"
                        value={product.description}
                        onChange={handleChange}
                        rows="3"
                    ></textarea>
                </div>

                {/* Precio */}
                <div className="mb-3">
                    <label htmlFor="price" className="form-label">Precio:</label>
                    <input
                        type="number"
                        step="0.01"
                        className="form-control"
                        id="price"
                        name="price"
                        value={product.price}
                        onChange={handleChange}
                        required
                    />
                </div>

                {/* Tallas y cantidades */}
                <div className="mb-3">
                    <label className="form-label">Tallas y cantidades:</label>
                    <div>
                        {["XS", "S", "M", "L", "XL", "XXL"].map((size) => (
                            <div key={size} className="mb-2">
                                <label htmlFor={`size-${size}`} className="form-label me-2">
                                    {size}:
                                </label>
                                <input
                                    type="number"
                                    className="form-control d-inline-block w-25"
                                    id={`size-${size}`}
                                    value={product.sizes[size] || ""}
                                    onChange={(e) => handleSizeChange(e, size)}
                                    min="0"
                                />
                            </div>
                        ))}
                    </div>
                </div>

                {/* Categorías */}
                <div className="mb-3">
                    <label className="form-label">Categorías:</label>
                    <div>
                        {categoriesList.map((category) => (
                            <div key={category.id} className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id={`category-${category.id}`}
                                    value={category.id}
                                    checked={product.categories.includes(category.id)}
                                    onChange={(e) => {
                                        const isChecked = e.target.checked;
                                        setProduct((prev) => ({
                                            ...prev,
                                            categories: isChecked
                                                ? [...prev.categories, category.id]
                                                : prev.categories.filter((id) => id !== category.id),
                                        }));
                                    }}
                                />
                                <label className="form-check-label" htmlFor={`category-${category.id}`}>
                                    {category.name}
                                </label>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Imágenes */}
                <div className="mb-3">
                    <label className="form-label">Imágenes:</label>
                    <div>
                        {product.images.map((image, index) => (
                            <div key={index} className="mb-2">
                                <img src={`/api/product${image}`} alt={`Product Image ${index}`} className="img-thumbnail" />
                                <button type="button" className="btn btn-danger" onClick={() => handleRemoveImage(image)}>
                                    Eliminar
                                </button>
                            </div>
                        ))}
                    </div>
                    <div className="mb-3">
                        <label htmlFor="newImages" className="form-label">Añadir Nuevas Imágenes:</label>
                        <input
                            type="file"
                            className="form-control"
                            id="newImages"
                            multiple
                            onChange={handleImageChange}
                        />
                    </div>
                </div>

                {/* Botón de submit */}
                <button type="submit" className="btn btn-primary">Actualizar Producto</button>
            </form>

            {/* Mensajes */}
            {successMessage && <div className="alert alert-success mt-3">{successMessage}</div>}
            {errorMessage && <div className="alert alert-danger mt-3">{errorMessage}</div>}
        </div>
    );
};

export default EditProduct;