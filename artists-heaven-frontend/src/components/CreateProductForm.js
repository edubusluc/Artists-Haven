import React, { useState, useEffect } from "react";

const CreateProductForm = () => {
  const [product, setProduct] = useState({
    name: "",
    description: "",
    price: "",
    sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
    categories: [], // Array para IDs de categorías seleccionadas
  });

  const [categoriesList, setCategoriesList] = useState([]);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [images, setImages] = useState([]);
  const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));
  const rol = localStorage.getItem("role");


  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch("/api/product/categories");
        if (response.ok) {
          const data = await response.json();
          setCategoriesList(data);
        } else {
          throw new Error("Error al cargar las categorías.");
        }
      } catch (error) {
        setErrorMessage(error.message);
      }
    };

    fetchCategories();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProduct({ ...product, [name]: value });
  };

  const handleSizeChange = (e, size) => { //size es el identificador de la talla
    const { value } = e.target; //Extrae el valor del campo
    setProduct((prev) => ({ //prev es el valor
      ...prev,
      sizes: {
        ...prev.sizes,
        [size]: value ? parseInt(value, 10) : 0, // Convertir a número o 0
      },
      
    }));
  };

  const handleCategoryChange = (e) => {
    const { value, checked } = e.target;
    setProduct((prev) => {
      const updatedCategories = checked
        ? [...prev.categories, value]
        : prev.categories.filter((id) => id !== value);
      return { ...prev, categories: updatedCategories };
    });
  };

  const handleImageChange = (e) => {
    setImages(e.target.files);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const productToSend = { ...product };

    const formData = new FormData();
    formData.append("product", new Blob([JSON.stringify(productToSend)], { type: "application/json" }));

    for (let i = 0; i < images.length; i++) {
      formData.append("images", images[i]);
    }

    try {
      const response = await fetch("/api/product/new", {
        method: "POST",
        body: formData,
        headers: {
          'Authorization': `Bearer ${authToken}`
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
          sizes: {},
          categories: [],
        });
        setImages([]);
        window.location.href = "/";
      } else {
        throw new Error("No se pudo crear el producto.");
      }
    } catch (error) {
      setErrorMessage(error.message);
      setSuccessMessage("");
    }
  };

  if (rol !== "ADMIN") {
    // Si el rol no es ADMIN, no hacemos la petición
    return "No tienes permisos para acceder a esta página";
}

  return (
    <div className="container mt-5">
      <h2>Crear Nuevo Producto</h2>
      <form onSubmit={handleSubmit}>
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
                  checked={product.categories.includes(String(category.id))}
                  onChange={handleCategoryChange}
                />
                <label className="form-check-label" htmlFor={`category-${category.id}`}>
                  {category.name}
                </label>
              </div>
            ))}
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Imágenes:</label>
          <input
            type="file"
            className="form-control"
            multiple
            onChange={handleImageChange}
          />
        </div>

        <button type="submit" className="btn btn-primary">Crear Producto</button>
      </form>

      {successMessage && <div className="alert alert-success mt-3">{successMessage}</div>}
      {errorMessage && <div className="alert alert-danger mt-3">{errorMessage}</div>}
    </div>
  );
};

export default CreateProductForm;
