import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const CreateProductForm = () => {
  const [product, setProduct] = useState({
    name: "",
    description: "",
    price: "",
    sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
    categories: [],
  });

  const [categoriesList, setCategoriesList] = useState([]);
  const [images, setImages] = useState([]);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [authToken] = useState(localStorage.getItem("authToken"));
  const [validationError, setValidationError] = useState("");
  const rol = localStorage.getItem("role");
  const navigate = useNavigate();

  useEffect(() => {
    if (rol !== "ADMIN") {
      setErrorMessage("No tienes permisos para acceder a esta página.");
      return;
    }

    fetch("/api/product/categories")
      .then((response) => response.json())
      .then((data) => setCategoriesList(data))
      .catch((error) => {
        console.error("Error cargando las categorías:", error);
        setErrorMessage("Error al cargar las categorías.");
      });
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProduct({ ...product, [name]: value });
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

  const handleCategoryChange = (e) => {
    const { value, checked } = e.target;
    setProduct((prev) => {
      const updatedCategories = checked
        ? [...prev.categories, parseInt(value)]
        : prev.categories.filter((id) => id !== parseInt(value));
      return { ...prev, categories: updatedCategories };
    });
  };



  const handleImageChange = (e) => {
    setImages(Array.from(e.target.files));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (product.categories.length === 0) {
      setValidationError("Debes seleccionar al menos una categoría.");
      return
    }
    setValidationError("");

    const formData = new FormData();
    formData.append("product", new Blob([JSON.stringify(product)], { type: "application/json" }));
    images.forEach((file) => formData.append("images", file));

    try {
      const response = await fetch("/api/product/new", {
        method: "POST",
        body: formData,
        headers: {
          'Authorization': `Bearer ${authToken}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setSuccessMessage(`¡Producto "${data.name}" creado con éxito!`);
        setErrorMessage("");
        setProduct({
          name: "",
          description: "",
          price: "",
          sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
          categories: [],
        });
        setImages([]);
        navigate("/product/all");
      } else {
        throw new Error("No se pudo crear el producto.");
      }
    } catch (error) {
      setErrorMessage(error.message);
      setSuccessMessage("");
    }
  };

  if (rol !== "ADMIN") {
    return "No tienes permisos para acceder a esta página";
  }

  return (
    <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white  py-10 px-4 sm:px-10">
      <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">

        <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">Crear Nuevo Producto</h2>

        {validationError && (
          <div className="text-red-900 text-sm mt-2 bg-red-300 rounded-md p-4 mb-4">{validationError}</div>
        )}

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
              required
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
                    value={product.sizes[size] || 0}
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
                    value={cat.id}
                    checked={product.categories.includes(cat.id)}
                    onChange={handleCategoryChange}
                    className="mr-2"
                  />
                  {cat.name}
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block font-semibold mb-2 text-sm text-gray-600">Subir imágenes</label>
            <input
              type="file"
              multiple
              onChange={handleImageChange}
              className="block w-full text-sm text-gray-500"
              required
            />
          </div>

          <button
            type="submit"
            className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
          >
            Crear Producto
          </button>

          {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
          {errorMessage && <div className="text-red-600 text-sm mt-3">{errorMessage}</div>}
        </form>
      </div>
    </div>
  );
};

export default CreateProductForm;
