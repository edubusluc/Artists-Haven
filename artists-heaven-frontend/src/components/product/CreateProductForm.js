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
    categories: [],
    collectionId: null,
    section: "",
    composition: defaultComposition,
    shippingDetails: defaultShippingDetails,
    modelReference: "",
  });

  const [colors, setColors] = useState([
    {
      colorName: "",
      hexCode: "#000000",
      images: [],
      sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
      availableUnits: 0,
      modelReference: null
    }
  ]);

  const [categoriesList, setCategoriesList] = useState([]);
  const [collectionsList, setCollectionsList] = useState([]);
  const [images, setImages] = useState([]);
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


  const handleCareChange = useCallback((value) => {
    setProduct(prev => ({ ...prev, care: value }));
  }, []);

  useEffect(() => {
    if (!checkTokenExpiration || rol !== 'ADMIN') {
      setErrorMessage("No tienes permisos para acceder a esta página.");
      return;
    }

    const fetchCategories = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/product/categories");
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
        const response = await fetch("http://localhost:8080/api/product/allCollections", {
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

  const handleCategoryChange = (e) => {
    const { value, checked } = e.target;
    setProduct((prev) => {
      const updatedCategories = checked
        ? [...prev.categories, parseInt(value)]
        : prev.categories.filter((id) => id !== parseInt(value));
      return { ...prev, categories: updatedCategories };
    });
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

    // Validaciones generales (como ya tienes)...
    if (!product.name) errors.name = t('productForm.error.requiredName');
    if (!product.description) errors.description = t('productForm.error.requiredDescription');
    if (!product.price) errors.price = t('productForm.error.requiredPrice');
    if (product.price < 0) errors.price = t('productForm.error.invalidPrice');
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

    const formData = new FormData();
    const productToSend = {
      ...product,
      colors: colors.map(c => ({
        colorName: c.colorName,
        hexCode: c.hexCode,
        images: [],
        sizes: product.section === "ACCESSORIES" ? null : c.sizes,
        availableUnits: product.section === "ACCESSORIES" ? c.availableUnits : null,
        modelReference: null,
      })),
    };

    formData.append("product", new Blob([JSON.stringify(productToSend)], { type: "application/json" }));

    // Imágenes y modelos por color
    colors.forEach((c, i) => {
      c.images.forEach((file) => {
        formData.append(`colorImages_${i}`, file);
      });

      if (c.modelReference && c.modelReference instanceof File) {
        formData.append(`colorModels_${i}`, c.modelReference);
      }
    });

    try {
      const response = await fetch(`http://localhost:8080/api/product/new?lang=${language}`, {
        method: "POST",
        body: formData,
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      const result = await response.json();

      if (response.ok) {
        setSuccessMessage(`¡Producto "${result.data.name}" creado con éxito!`);
        setErrorMessage("");

        // resetear formulario
        setProduct({
          name: "",
          description: "",
          price: "",
          categories: [],
          collectionId: null,
          section: "",
          composition: defaultComposition,
          shippingDetails: defaultShippingDetails,
          modelReference: "",
        });
        setColors([
          {
            colorName: "",
            hexCode: "#000000",
            images: [],
            sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
            availableUnits: 0,
            modelReference: null,
          },
        ]);

        navigate("/admin/products/store");
      } else {
        throw new Error(result.message || "Error al crear el producto.");
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
            <label className="block font-semibold mb-2 text-sm text-gray-600">Colores</label>
            {colors.map((color, index) => (
              <div key={index} className="mb-4 border p-2 rounded">
                <div className="flex justify-between items-center">
                  <span className="font-semibold">Color #{index + 1}</span>
                  <button
                    type="button"
                    onClick={() => {
                      const updated = colors.filter((_, i) => i !== index);
                      setColors(updated);
                    }}
                    className="text-red-600 text-sm font-bold hover:underline"
                  >
                    {t('createProductForm.removeColor')}
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
                  accept="image/*"
                  onChange={(e) => {
                    const updated = [...colors];
                    updated[index].images = Array.from(e.target.files);
                    setColors(updated);
                  }}
                />

                <div className="mt-2">
                  <label className="block text-sm">{t('createProductForm.3DModel')}</label>
                  <input
                    type="file"
                    accept=".glb,.gltf"
                    onChange={(e) => {
                      const file = e.target.files[0];
                      const updated = [...colors];
                      if (file) {
                        updated[index].modelReference = file;
                      } else {
                        updated[index].modelReference = null;
                      }
                      setColors(updated);
                    }}
                    className="block w-full text-sm text-gray-500"
                  />
                </div>
                {validationErrors[`color_${index}_name`] && (
                  <p className="text-red-600 text-sm">{validationErrors[`color_${index}_name`]}</p>
                )}
                {validationErrors[`color_${index}_images`] && (
                  <p className="text-red-600 text-sm">{validationErrors[`color_${index}_images`]}</p>
                )}
                {/* Vista previa y mover imágenes por color */}
                {color.images.length > 0 && (
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4">
                    {color.images.map((img, imgIndex, colorindx) => (
                      <div key={imgIndex} className="text-center relative">
                        <span className="block text-xs text-gray-500 mb-1">#{imgIndex + 1}</span>
                        <img
                          src={URL.createObjectURL(img)}
                          alt={`preview-${index}-${imgIndex}`}
                          className="w-full h-24 object-cover rounded shadow mb-2"
                        />

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

                {/* Stock por color */}
                {product.section === "ACCESSORIES" ? (
                  <div className="mt-2">
                    <label className="block text-sm">{t('createProductForm.availableUnits')}</label>
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
                setColors([
                  ...colors,
                  {
                    colorName: "",
                    hexCode: "",
                    images: [],
                    sizes: { XS: 0, S: 0, M: 0, L: 0, XL: 0, XXL: 0 },
                    availableUnits: 0
                  }
                ])
              }
              className="bg-gray-300 px-2 py-1 rounded"
            >
              + {t('createProductForm.addColor')}
            </button>
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
