import React, { useState, useEffect, useCallback, useMemo, useRef } from "react";
import { useNavigate } from "react-router-dom";
import SimpleMDE from "react-simplemde-editor";
import "easymde/dist/easymde.min.css";
import NonAuthorise from "../NonAuthorise";
import SessionExpired from "../SessionExpired";
import { checkTokenExpiration } from "../../utils/authUtils";
import { useTranslation } from 'react-i18next';

const CreateProductForm = () => {
  const defaultComposition = `# COMPOSICIÓN

Incluye una descripción general sobre los materiales utilizados, su origen o beneficios ambientales si aplica.

# MATERIALES

- XX% Material 1
- XX% Material 2

(Puedes agregar más líneas según los materiales necesarios)

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

### Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde el hilo (o la fibra, según corresponda) hasta la prenda final para cada pedido.
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
`;

  const defaultShippingDetails = `# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

### Proceso de Devolución

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a 'MY SPACE' y luego a 'MY ORDER'. Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.

`;

  const [product, setProduct] = useState({
    name: "",
    description: "",
    price: "",
    sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
    categories: [],
    collectionId: null,
    section: "",
    availableUnits: 0,
    composition: defaultComposition,
    shippingDetails: defaultShippingDetails,
    modelReference: "",
  });

  const [categoriesList, setCategoriesList] = useState([]);
  const [collectionsList, setCollectionsList] = useState([]);
  const [images, setImages] = useState([]);
  const [modelFile, setModelFile] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [authToken] = useState(localStorage.getItem("authToken"));
  const [validationError, setValidationError] = useState("");
  const rol = localStorage.getItem("role");
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const language = i18n.language;

  useEffect(() => {
    setValidationErrors({})
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

  const handleOriginChange = useCallback((value) => {
    setProduct(prev => ({ ...prev, origin: value }));
  }, []);

  const handleCareChange = useCallback((value) => {
    setProduct(prev => ({ ...prev, care: value }));
  }, []);

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

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProduct({ ...product, [name]: value });
  };

  const handleSectionChange = (e) => {
    const section = e.target.value;
    setProduct({
      ...product,
      section: section,
      sizes: section === "ACCESSORIES" ? {} : product.sizes,
      availableUnits: section === "ACCESSORIES" ? 1 : product.availableUnits
    });
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
    setModelFile(file); // ✅ ahora solo guardamos el File aparte
  };

  const handleImageChange = (e) => {
    setImages(Array.from(e.target.files));
  };

  const moveImage = (fromIndex, toIndex) => {
    setImages((prevImages) => {
      const updated = [...prevImages];
      const [moved] = updated.splice(fromIndex, 1);
      updated.splice(toIndex, 0, moved);
      return updated;
    });
  };

  const formRef = useRef(null);

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
    if (images.length === 0) errors.images = t('productForm.error.requiredImages');


    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      const firstErrorField = Object.keys(errors)[0];
      const element = formRef.current.querySelector(`[name="${firstErrorField}"]`);
      if (element) {
        element.scrollIntoView({ behavior: "smooth", block: "center" });
        element.focus();
      }

      return;
    }

    if (product.categories.length === 0) {
      setValidationError("Debes seleccionar al menos una categoría.");
      alert("Debes seleccionar al menos una categoría.");
      return
    }
    if (!product.section) {
      setValidationError("Debes seleccionar una sección.");
      return;
    }

    if (product.section === "ACCESSORIES" && (!product.availableUnits || product.availableUnits < 1)) {
      setValidationError("Los accesorios deben tener al menos 1 unidad disponible.");
      return;
    }
    setValidationError("");

    const formData = new FormData();
    formData.append("product", new Blob([JSON.stringify(product)], { type: "application/json" }));
    images.forEach((file) => formData.append("images", file));
    if (modelFile) {
      formData.append("model", modelFile); // ✅ el backend lo recibe en @RequestPart("model")
    }

    try {
      const response = await fetch(`/api/product/new?lang=${language}`, {
        method: "POST",
        body: formData,
        headers: {
          'Authorization': `Bearer ${authToken}`,
        },
      });
      const result = await response.json();
      const errorMessage = result.message;

      if (response.ok) {

        setSuccessMessage(`¡Producto "${result.data.name}" creado con éxito!`);
        setErrorMessage("");
        setProduct({
          name: "",
          description: "",
          price: "",
          sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
          categories: [],
        });
        setImages([]);
        navigate("/admin/products/store");
      } else {
        throw new Error(errorMessage);
      }
    } catch (error) {
      setErrorMessage(error.message);
      setSuccessMessage("");
    }
  };

  if (!rol || rol !== 'ADMIN') {
    return <NonAuthorise />;
  } else if (!checkTokenExpiration()) {
    return <SessionExpired />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white py-10 px-4 sm:px-10">
      <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
        <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">{t('createProductForm.title')}</h2>

        {validationError && (
          <div className="text-red-900 text-sm mt-2 bg-red-300 rounded-md p-4 mb-4">{validationError}</div>
        )}

        <form ref={formRef} onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block font-semibold mb-1 text-sm text-gray-600">{t('createProductForm.name')}</label>
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
            <label className="block font-semibold mb-1 text-sm text-gray-600">{t('createProductForm.description')}</label>
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
            <label className="block font-semibold mb-1 text-sm text-gray-600">{t('createProductForm.price')} (€)</label>
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

          <div>
            <label className="block font-semibold mb-2 text-sm text-gray-600">{t('createProductForm.section')}</label>
            <select
              name="section"
              value={product.section}
              onChange={handleSectionChange}
              className="w-full border rounded px-4 py-2 text-sm"

            >
              <option value="">{t('createProductForm.chooseOneOption')}</option>
              <option value="TSHIRT">{t('createProductForm.tshirt')}</option>
              <option value="PANTS">{t('createProductForm.pants')}</option>
              <option value="ACCESSORIES">{t('createProductForm.accessories')}</option>
              <option value="HOODIES">{t('createProductForm.hoodies')}</option>
            </select>
            {validationErrors.section && (
              <p className="text-red-600 text-sm">{validationErrors.section}</p>
            )}
          </div>

          {/* Mostrar el campo de tallas y stock solo si el producto no es un accesorio */}
          {product.section !== "ACCESSORIES" && (
            <div>
              <label className="block font-semibold mb-2 text-sm text-gray-600">{t('createProductForm.size&stock')}</label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                {["XS", "S", "M", "L", "XL", "XXL"].map((size) => (
                  <div key={size}>
                    <label className="block text-sm text-gray-500">{size}</label>
                    <input
                      type="number"
                      name="sizes"
                      min="0"
                      value={product.sizes[size] || 0}
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

          {/* Mostrar el campo de unidades disponibles solo si el producto es un accesorio */}
          {product.section === "ACCESSORIES" && (
            <div>
              <label className="block font-semibold mb-2 text-sm text-gray-600">{t('createProductForm.availableUnits')}</label>
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

          <div>
            <label className="block font-semibold mb-2 text-sm text-gray-600">{t('createProductForm.categories')}</label>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {categoriesList.map((cat) => (
                <label key={cat.id} className="flex items-center text-sm text-gray-600">
                  <input
                    type="checkbox"
                    name="categories"
                    value={cat.id}
                    checked={product.categories.includes(cat.id)}
                    onChange={handleCategoryChange}
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
              {t('createProductForm.colections')}
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
              {t('createProductForm.composition')}
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
              {t('createProductForm.shippingDetails')}
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
            <label className="block font-semibold mb-2 text-sm text-gray-600">{t('createProductForm.uploadImages')}</label>
            <input
              type="file"
              multiple
              onChange={handleImageChange}
              className="block w-full text-sm text-gray-500"

            />
            {validationErrors.images && (
              <p className="text-red-600 text-sm">{validationErrors.images}</p>
            )}
          </div>

          <div>
            <label className="block font-semibold mb-2 text-sm text-gray-600">{t('createProductForm.upload3dModel')}</label>
            <input
              type="file"
              onChange={handleModelReferenceChange}
              className="block w-full text-sm text-gray-500"
            />
            {validationErrors.modelReference && (
              <p className="text-red-600 text-sm">{validationErrors.modelReference}</p>
            )}
          </div>

          {images.length > 0 && (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4">
              {images.map((img, index) => (
                <div key={index} className="text-center">
                  <span className="block text-xs text-gray-500 mb-1">#{index + 1}</span>
                  <img
                    src={URL.createObjectURL(img)}
                    alt={`preview-${index}`}
                    className="w-full h-24 object-cover rounded shadow mb-2"
                  />
                  <div className="flex justify-center gap-1">
                    <button
                      type="button"
                      onClick={() => moveImage(index, index - 1)}
                      disabled={index === 0}
                      className="px-2 py-1 bg-gray-300 rounded text-xs disabled:opacity-50"
                    >
                      ↑
                    </button>
                    <button
                      type="button"
                      onClick={() => moveImage(index, index + 1)}
                      disabled={index === images.length - 1}
                      className="px-2 py-1 bg-gray-300 rounded text-xs disabled:opacity-50"
                    >
                      ↓
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
          {errorMessage && <div className="text-red-600 text-sm mt-3">{errorMessage}</div>}

          <button
            type="submit"
            className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
          >
            {t('createProductForm.createProduct')}
          </button>


        </form>
      </div>
    </div>
  );

};

export default CreateProductForm;
