import { useState, useEffect, useCallback } from "react";
import {
  faShirt,
  faCheck,
  faXmark,
  faRectangleAd,
} from "@fortawesome/free-solid-svg-icons";
import { Link } from "react-router-dom";
import {
  toggleProductAvailability,
  demoteProduct,
  createCategory,
  editCategory,
  createCollection,
  editCollection,
} from "../../../services/adminServices";
import { checkTokenExpiration } from "../../../utils/authUtils";
import Footer from "../../Footer";
import { LazyLoadImage } from 'react-lazy-load-image-component';
import NonAuthorise from "../../NonAuthorise";
import SessionExpired from "../../SessionExpired";
import { useTranslation } from 'react-i18next';
import { ModalForm, ModalCollectionForm } from "./ModalForm";
import { MetricCard, Pagination } from "./MetricCard";
import { useAdminData } from "./AdminProductListData";

const AdminProductList = () => {
  const role = localStorage.getItem("role");
  const authToken = localStorage.getItem("authToken");

  const [categoryToEdit, setCategoryToEdit] = useState(null);
  const [collectionToEdit, setCollectionToEdit] = useState(null);
  const [categoryName, setCategoryName] = useState("");
  const [collectionName, setCollectionName] = useState("");
  const [collectionSearch, setCollectionSearch] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [showCollectionModal, setShowCollectionModal] = useState(false);
  const [totalPages, setTotalPages] = useState(1);
  const [isPromoted, setIsPromoted] = useState(false);



  const [filters, setFilters] = useState({
    searchTerm: "",
    page: 0,
    pageSize: 6,
    available: null,
    promoted: null,
  });


  // --- NUEVOS ESTADOS ---
  const [filterAvailable, setFilterAvailable] = useState(false);
  const [filterNotAvailable, setFilterNotAvailable] = useState(false);
  const [filterPromoted, setFilterPromoted] = useState(false);
  const [filterPromotedCollections, setFilterPromotedCollections] = useState(false);
  const [filterNotPromotedCollections, setFilterNotPromotedCollections] = useState(false);
  const [categorySearch, setCategorySearch] = useState("");
  const [categoryPage, setCategoryPage] = useState(0);
  const [collectionPage, setCollectionPage] = useState(0);

  const pageSize = 6;
  const { t, i18n } = useTranslation();
  const language = i18n.language;

  const { categories, collections, products, productManagement, fetchCategories, fetchCollections, fetchProducts, fetchProductManagement } = useAdminData(authToken, setTotalPages);

  // ------------------- EFFECTS -------------------

  useEffect(() => {
    fetchCategories();
    fetchCollections();
    fetchProductManagement();
  }, [fetchCategories, fetchCollections, fetchProductManagement, role]);

  useEffect(() => {
    if (!checkTokenExpiration || role !== "ADMIN") return;
    const delayDebounce = setTimeout(() => {
      fetchProducts(filters);
    }, 500);

    return () => clearTimeout(delayDebounce);
  }, [filters, fetchProducts, role]);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [filters.page]);

  // ------------------- HANDLERS -------------------
  const handleFilterChange = (type) => {
    setFilters((prev) => {
      let available = prev.available;
      if (type === "available") {
        available = prev.available === true ? null : true;
      } else if (type === "notAvailable") {
        available = prev.available === false ? null : false;
      }
      if (type === "promoted") {
        return { ...prev, promoted: prev.promoted ? null : true, page: 0 };
      }
      return { ...prev, available, page: 0 };
    });
  };

  const handleToggleAvailability = async (id, shouldEnable) => {
    if (!checkTokenExpiration()) return;
    try {
      await toggleProductAvailability(authToken, id, shouldEnable);
      alert(t('adminProductList.productUpdated'));
      fetchProducts(filters);
    } catch (err) {
      console.error("Error:", err);
    }
  };

  const handleDemoteProduct = async (e, id) => {
    e.preventDefault();
    try {
      const message = await demoteProduct(authToken, id, language);
      alert(message.message);
      fetchProducts(filters);
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();

    try {
      const result = await createCategory(authToken, categoryName);
      if (result.status = 200) {
        alert(t('adminProductList.categoryCreated'));
      }

      fetchCategories();
      setCategoryName("");
      setShowModal(false);

    } catch (error) {
      let userMessage = error.message;

      if (error.message.includes("already exists")) {
        userMessage = t('adminProductList.categoryExists');
      }

      alert(userMessage);
    }
  };

  const handleEditCategory = async (e, categoryId) => {
    e.preventDefault();

    try {
      const result = await editCategory(authToken, categoryId, categoryName);
      if (result.status = 200) {
        alert(t('adminProductList.categoryEdited'));
      }
      fetchCategories();
      setShowModal(false);
      setCategoryToEdit(null);

    } catch (error) {
      let userMessage = error.message;
      if (error.message.includes("already exists")) {
        userMessage = t('adminProductList.categoryExists');
      }

      alert(userMessage);
    }
  };

  const handleCreateCollection = async (e) => {
    e.preventDefault();
    try {
      const result = await createCollection(authToken, collectionName);
      if (result.status = 200) {
        alert(t('adminProductList.collectionCreated'));
      }
      fetchCollections();
      setShowCollectionModal(false);
      setCollectionName("");
    } catch (error) {
      let userMessage = error.message;
     if (error.message.includes("already exists")) {
        userMessage = t('adminProductList.collectionExists');
      }

      alert(userMessage);
    }
  };

  const handleEditCollection = async (e, collectionId) => {
    e.preventDefault();
    try {
      const result = await editCollection(authToken, collectionId, collectionName, isPromoted);
      if (result.status = 200) {
        alert(t('adminProductList.collectionEdited'));
      }
      fetchCollections();
      setShowCollectionModal(false);
      setCollectionName("");
    } catch (error) {
      let userMessage = error.message;
     if (error.message.includes("already exists")) {
        userMessage = t('adminProductList.collectionExists');
      }

      alert(userMessage);
    }
  };

  const nextPage = useCallback(() => {
    setFilters((prev) => ({
      ...prev,
      page: Math.min(prev.page + 1, totalPages - 1),
    }));
  }, [totalPages]);

  const prevPage = useCallback(() => {
    setFilters((prev) => ({
      ...prev,
      page: Math.max(prev.page - 1, 0),
    }));
  }, []);

  const filteredCategories = categories.filter(cat => cat.name.toLowerCase().includes(categorySearch.toLowerCase()));
  const paginatedCategories = filteredCategories.slice(categoryPage * pageSize, (categoryPage + 1) * pageSize);
  const filteredCollections = collections
    .filter(c => !filterPromotedCollections || c.isPromoted)
    .filter(c => !filterNotPromotedCollections || !c.isPromoted)
    .filter(c => c.name.toLowerCase().includes(collectionSearch.toLowerCase()));
  const paginatedCollections = filteredCollections.slice(collectionPage * pageSize, (collectionPage + 1) * pageSize);

  if (!role || role !== 'ADMIN') {
    return <NonAuthorise />;
  } else if (!checkTokenExpiration()) {
    return <SessionExpired />;
  }

  return (
    <>
      <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
        <div className="grid grid-cols-1 lg:grid-cols-2 p-4 m-4 gap-4">
          {/* COLUMNA 1 */}
          <div className="w-full h-full rounded-lg shadow-lg bg-white backdrop-blur-md md:p-8 p-4">
            <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
              <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                {t('adminProductList.productManagemente')}
              </p>
              <Link to="/product/new" className="w-full md:w-auto">
                <button
                  className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                >
                  {t('adminProductList.createNewProduct')}
                </button>
              </Link>
            </div>
            <input
              type="text"
              placeholder="Buscar producto..."
              value={filters.searchTerm}
              onChange={(e) => setFilters((prev) => ({
                ...prev,
                searchTerm: e.target.value,
                page: 0
              }))}
              className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
            />
            <div className="flex gap-4 mb-4">
              <label>
                <input
                  type="checkbox"
                  checked={filters.available === true}
                  onChange={() => handleFilterChange("available")}
                /> {t('adminProductList.available')}
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={filters.available === false}
                  onChange={() => handleFilterChange("notAvailable")}
                /> {t('adminProductList.unavailable')}
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={filters.promoted === true}
                  onChange={() => handleFilterChange("promoted")}
                /> {t('adminProductList.promoted')}
              </label>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-3 gap-6">

              {products.map((product) => (
                <div key={product.id} className="h-full">
                  <div className="h-full bg-white shadow-md rounded-lg p-4 border border-gray-200 hover:shadow-lg transition-shadow relative flex flex-col justify-between">

                    {/* Bloquear edición si está promocionado */}
                    {product.onPromotion ? (
                      <div className="w-full">
                        <LazyLoadImage
                          src={`/api/product${product.colors[0].images[0]}`}
                          alt={product.name}
                          className="w-full h-40 object-contain rounded-md mb-3"
                        />
                        <div className="flex justify-between items-center mb-2">
                          <h2 className="font-semibold text-base md:text-lg text-gray-800 truncate">
                            {product.name}
                          </h2>
                          {!product.available ? (
                            <span className="text-xs bg-red-100 text-red-600 px-2 py-1 rounded">
                              {t('adminProductList.notAvailable')}
                            </span>
                          ) : (
                            <span className="text-xs bg-green-100 text-green-600 px-2 py-1 rounded">
                              {t('adminProductList.active')}
                            </span>
                          )}
                        </div>
                        <div className="mb-2">
                          {product.onPromotion && product.discount > 0 ? (
                            <div className="flex items-center gap-2">
                              <span className="text-sm text-gray-500 line-through">
                                {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
                              </span>
                              <span className="text-base font-bold text-rose-600">
                                {product.price.toFixed(2)}€
                              </span>
                              <span className="text-xs bg-yellow-100 text-yellow-600 px-2 py-0.5 rounded">
                                -{product.discount}%
                              </span>
                            </div>
                          ) : (
                            <span className="text-base font-semibold text-gray-900">
                              {product.price.toFixed(2)}€
                            </span>
                          )}
                        </div>
                        <div className="text-xs text-gray-500 italic">
                          {Array.from(product.categories || [])
                            .sort((a, b) => a.name.localeCompare(b.name))
                            .map((cat) => cat.name)
                            .join(", ") || "Sin Categoría"}
                        </div>
                        {product.ratings?.length > 0 && (
                          <div className="absolute top-2 right-2 bg-yellow-400 text-white text-xs font-bold px-2 py-0.5 rounded">
                            ⭐{" "}
                            {(
                              product.ratings.reduce((acc, r) => acc + r.score, 0) /
                              product.ratings.length
                            ).toFixed(1)}
                          </div>
                        )}
                        <div className="text-sm text-red-500 font-semibold text-center mt-2">
                          {t('adminProductList.cannotEditWhilePromoted')}
                        </div>
                      </div>
                    ) : (
                      <Link to={`/product/edit/${product.id}`} className="w-full">
                        <LazyLoadImage
                          src={`/api/product${product.colors[0].images[0]}`}
                          alt={product.name}
                          className="w-full h-40 object-contain rounded-md mb-3"
                        />
                        <div className="flex justify-between items-center mb-2">
                          <h2 className="font-semibold text-base md:text-lg text-gray-800 truncate">
                            {product.name}
                          </h2>
                          {!product.available ? (
                            <span className="text-xs bg-red-100 text-red-600 px-2 py-1 rounded">
                              {t('adminProductList.notAvailable')}
                            </span>
                          ) : (
                            <span className="text-xs bg-green-100 text-green-600 px-2 py-1 rounded">
                              {t('adminProductList.active')}
                            </span>
                          )}
                        </div>
                        <div className="mb-2">
                          {product.onPromotion && product.discount > 0 ? (
                            <div className="flex items-center gap-2">
                              <span className="text-sm text-gray-500 line-through">
                                {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
                              </span>
                              <span className="text-base font-bold text-rose-600">
                                {product.price.toFixed(2)}€
                              </span>
                              <span className="text-xs bg-yellow-100 text-yellow-600 px-2 py-0.5 rounded">
                                -{product.discount}%
                              </span>
                            </div>
                          ) : (
                            <span className="text-base font-semibold text-gray-900">
                              {product.price.toFixed(2)}€
                            </span>
                          )}
                        </div>
                        <div className="text-sm text-gray-600 mb-2">
                          {product.colors && product.colors.length > 0 && (
                            <div>
                              {t('adminProductList.colors')}:{" "}
                              {product.colors
                                .map(color => color.colorName)
                                .join(", ")}
                            </div>
                          )}
                        </div>
                        <div className="text-xs text-gray-500 italic">
                          {Array.from(product.categories || [])
                            .sort((a, b) => a.name.localeCompare(b.name))
                            .map((cat) => cat.name)
                            .join(", ") || "Sin Categoría"}
                        </div>
                        {product.ratings?.length > 0 && (
                          <div className="absolute top-2 right-2 bg-yellow-400 text-white text-xs font-bold px-2 py-0.5 rounded">
                            ⭐{" "}
                            {(
                              product.ratings.reduce((acc, r) => acc + r.score, 0) /
                              product.ratings.length
                            ).toFixed(1)}
                          </div>
                        )}
                      </Link>
                    )}

                    {/* Botones de promoción/despromoción y disponibilidad */}
                    <div className="mt-2 flex flex-col gap-2 text-center">
                      {product.onPromotion ? (
                        <button
                          onClick={(e) => handleDemoteProduct(e, product.id)}
                          className="w-full bg-red-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-red-600"
                        >
                          {t('adminProductList.demote')}
                        </button>
                      ) : (
                        product.available && (
                          <Link
                            to={`/product/promote/${product.id}`}
                            className="w-full bg-green-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-green-600"
                          >
                            {t('adminProductList.promote')}
                          </Link>
                        )
                      )}

                      {product.onPromotion ? (
                        <div className="w-full bg-gray-300 text-gray-600 py-2 px-4 rounded-lg text-xs font-bold">
                          {t('adminProductList.cannotDisableWhilePromoted')}
                        </div>
                      ) : product.available ? (
                        <button
                          onClick={() => handleToggleAvailability(product.id, false)}
                          className="w-full bg-red-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-gray-600"
                        >
                          {t('adminProductList.disable')}
                        </button>
                      ) : (
                        <button
                          onClick={() => handleToggleAvailability(product.id, true)}
                          className="w-full bg-indigo-500 text-white py-2 px-4 rounded-lg text-xs font-bold transition-all hover:bg-indigo-600"
                        >
                          {t('adminProductList.enable')}
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}


            </div>

            {/* Paginación */}
            <Pagination
              page={filters.page}
              totalPages={totalPages}
              onPrev={prevPage}
              onNext={nextPage}
            />
          </div>

          {/* COLUMNA 2 */}
          <div className="w-full">
            <div className="bg-gray-50 p-4 rounded-lg mb-4 flex justify-around">
              <div className="flex flex-col sm:flex-row flex-wrap ">
                <MetricCard icon={faShirt} value={productManagement.totalProducts} title={t('adminProductList.totalProducts')} iconColor="text-blue-600" bgColor="bg-blue-300" />
                <MetricCard icon={faCheck} value={productManagement.availableProducts} title={t('adminProductList.availableProducts')} iconColor="text-green-600" bgColor="bg-green-300" />
                <MetricCard icon={faXmark} value={productManagement.notAvailableProducts} title={t('adminProductList.notAvailableProducts')} iconColor="text-red-600" bgColor="bg-red-300" />
                <MetricCard icon={faRectangleAd} value={productManagement.promotedProducts} title={t('adminProductList.promoteProducts')} iconColor="text-yellow-600" bgColor="bg-yellow-300" />
              </div>
            </div>

            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                  {t('adminProductList.labelsManagement')}
                </p>
                <button
                  onClick={() => setShowModal(true)}
                  className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                >
                  {t('adminProductList.createLabel')}
                </button>
              </div>

              <input
                type="text"
                placeholder="Buscar etiqueta..."
                value={categorySearch}
                onChange={(e) => { setCategorySearch(e.target.value); setCategoryPage(0); }}
                className="p-2 border rounded w-full mb-3"
              />

              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                  <tr>
                    <th className="px-4 py-3 text-left">{t('adminProductList.name')}</th>
                    <th className="px-4 py-3 text-center">{t('adminProductList.actions')}</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {paginatedCategories.length > 0 ?
                    paginatedCategories
                      .slice()
                      .sort((a, b) => a.id - b.id)
                      .map((category) => (
                        <tr key={category.id}>
                          <td className="px-4 py-3">{category.name}</td>
                          <td className="px-4 py-3 text-center">
                            <Link
                              to="#"
                              onClick={() => {
                                setCategoryToEdit(category);
                                setCategoryName(category.name);
                                setShowModal(true);
                              }}
                              className="text-blue-500 hover:text-blue-700 mr-3">
                              {t('adminProductList.edit')}
                            </Link>
                          </td>
                        </tr>
                      )) : (
                      <tr>
                        <td colSpan="3" className="text-center px-4 py-3 text-gray-500">
                          {t('adminProductList.notCategoryAvailable')}
                        </td>
                      </tr>
                    )}
                </tbody>
              </table>
              <Pagination
                page={categoryPage}
                totalPages={Math.ceil(filteredCategories.length / pageSize)}
                onPrev={() => setCategoryPage(Math.max(0, categoryPage - 1))}
                onNext={() => setCategoryPage(Math.min(Math.ceil(filteredCategories.length / pageSize) - 1, categoryPage + 1))}
              />
            </div>

            <div className="bg-gray-50 p-4 rounded-lg mt-4">
              <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 m-4">
                <p className="custom-font-footer-black text-xl md:text-2xl font-bold text-center md:text-left">
                  {t('adminProductList.collectionManagement')}
                </p>
                <button
                  onClick={() => setShowCollectionModal(true)}
                  className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                >
                  {t('adminProductList.newCollection')}
                </button>
              </div>

              <div className="flex gap-4 mb-4">
                <label><input type="checkbox" checked={filterPromotedCollections} onChange={() => setFilterPromotedCollections(!filterPromotedCollections)} /> {t('adminProductList.promotedCollection')}</label>
                <label><input type="checkbox" checked={filterNotPromotedCollections} onChange={() => setFilterNotPromotedCollections(!filterNotPromotedCollections)} /> {t('adminProductList.notPromotedCollecion')}</label>
              </div>

              <input
                type="text"
                placeholder="Buscar colección..."
                value={collectionSearch}
                onChange={(e) => { setCollectionSearch(e.target.value); setCollectionPage(0); }}
                className="p-2 border rounded w-full mb-3"
              />

              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                  <tr>
                    <th className="px-4 py-3 text-left">ID</th>
                    <th className="px-4 py-3 text-left">{t('adminProductList.name')}</th>
                    <th className="px-4 py-3 text-center">{t('adminProductList.promoted')}</th>
                    <th className="px-4 py-3 text-center">{t('adminProductList.actions')}</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {paginatedCollections.length > 0 ? paginatedCollections.map((collection) => (
                    <tr key={collection.id}>
                      <td className="px-4 py-3">{collection.id}</td>
                      <td className="px-4 py-3">{collection.name}</td>
                      <td className="px-4 py-3 text-center">
                        <span
                          className={`px-2 py-1 rounded text-white ${collection.isPromoted ? 'bg-green-500' : 'bg-red-500'
                            }`}
                        >
                          {collection.isPromoted ? t('adminProductList.promotedCollection') : t('adminProductList.notPromotedCollecion')}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        <Link
                          to="#"
                          onClick={() => {
                            setCollectionToEdit(collection);
                            setCollectionName(collection.name);
                            setIsPromoted(Boolean(collection.isPromoted));
                            setShowCollectionModal(true);
                          }}
                          className="text-blue-500 hover:text-blue-700 mr-3">
                          {t('adminProductList.edit')}
                        </Link>
                      </td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan="3" className="text-center px-4 py-3 text-gray-500">
                        {t('adminProductList.notCollectionsAvailable')}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>

              <Pagination
                page={collectionPage}
                totalPages={Math.ceil(filteredCollections.length / pageSize)}
                onPrev={() => setCollectionPage(Math.max(0, collectionPage - 1))}
                onNext={() => setCollectionPage(Math.min(Math.ceil(filteredCollections.length / pageSize) - 1, collectionPage + 1))}
              />
            </div>
          </div>
        </div>
      </div>
      {/* Modal para crear categoría y colleciones */}
      {showModal && (
        <ModalForm
          title={t('adminProductList.category')}
          placeholder={t('adminProductList.categoryPlaceholder')}
          value={categoryName}
          setValue={setCategoryName}
          onSubmit={(e) =>
            categoryToEdit
              ? handleEditCategory(e, categoryToEdit.id)
              : handleCreateCategory(e)
          }
          onCancel={() => {
            setShowModal(false);
            setCategoryToEdit(null);
            setCategoryName("");
          }}
          isEdit={!!categoryToEdit}
        />
      )}

      {showCollectionModal && (
        <ModalCollectionForm
          title={t('adminProductList.collection')}
          placeholder={t('adminProductList.collectionPlaceholder')}
          value={collectionName}
          setValue={setCollectionName}
          onSubmit={(e) =>
            collectionToEdit
              ? handleEditCollection(e, collectionToEdit.id)
              : handleCreateCollection(e)
          }
          onCancel={() => {
            setShowCollectionModal(false);
            setCollectionToEdit(null);
            setCollectionName("");
            setIsPromoted(false);
          }}
          isEdit={!!collectionToEdit}
          isPromoted={isPromoted}
          setIsPromoted={setIsPromoted}
        />
      )}
      <Footer />
    </>
  );
};

export default AdminProductList;
