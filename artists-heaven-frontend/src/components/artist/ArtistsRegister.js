import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import Footer from '../Footer'
import { useTranslation } from 'react-i18next';

const ArtistForm = () => {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    artistName: "",
    url: "",
    color: "#ffffff"
  });

  const { t, i18n } = useTranslation();
  const currentLang = i18n.language;


  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();
  const [images, setImages] = useState([]);
  const [bannerImage, setBannerImage] = useState([]);
  const [validationErrors, setValidationErrors] = useState({});

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];
    const invalidFiles = files.filter(file => !allowedTypes.includes(file.type));
    if (invalidFiles.length > 0) {
      setErrorMessage(t('artistForm.error.invalidImage'));
      setImages([]);
      return;
    }
    setErrorMessage("");
    setImages(files);
  };

  const handleBannerImage = (e) => {
    const files = Array.from(e.target.files);
    const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];
    const invalidFiles = files.filter(file => !allowedTypes.includes(file.type));
    if (invalidFiles.length > 0) {
      setErrorMessage(t('artistForm.error.invalidImage'));
      setBannerImage([]);
      return;
    }
    setErrorMessage("");
    setBannerImage(files);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setValidationErrors({});

    let errors = {};

    if (!formData.firstName) errors.firstName = t('form.error.requiredFirstName');
    if (!formData.lastName) errors.lastName = t('form.error.requiredLastName');
    if (!formData.email) errors.email = t('form.error.requiredEmail');
    if (!formData.password) errors.password = t('form.error.requiredPassword');
    if (!formData.artistName) errors.artistName = t('artistForm.error.requiredArtistName');
    if (!formData.url) errors.url = t('artistForm.error.requiredUrl');

    if (images.length === 0) errors.images = t('artistForm.error.requiredImage');
    if (bannerImage.length === 0) errors.bannerImage = t('artistForm.error.requiredBanner');



    const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];

    const invalidMainImages = images.filter(file => !allowedTypes.includes(file.type));
    const invalidBannerImages = bannerImage.filter(file => !allowedTypes.includes(file.type));

    if (invalidMainImages.length > 0 || invalidBannerImages.length > 0) {
      setValidationErrors(t('artistForm.error.invalidImage'));
      return;
    }

    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    const data = new FormData();

    // Añadir campos de texto
    Object.keys(formData).forEach(key => {
      data.append(key, formData[key]);
    });

    data.append("image", images);
    data.append("bannerImage", bannerImage);

    try {
      const response = await fetch(`/api/artists/register?lang=${currentLang}`, {
        method: "POST",
        body: data,
      });

      const result = await response.json();
      const errorMessage = result.message;

      if (!response.ok) {
        throw new Error(errorMessage);

      }

      // Limpiar estado tras registro
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        artistName: "",
        url: "",
        color: "#ffffff",
      });
      setImages([]);
      setBannerImage([]);
      navigate('/auth/login');
    } catch (error) {
      setErrorMessage(error.message || "Error al registrar el artista");
    }
  };

  console.log("EO")

  return (
    <><div className="max-w-3xl mx-auto mt-28 p-6 bg-white shadow-2xl rounded-2xl mb-10">
      <h2 className="text-3xl font-bold text-center text-purple-700 mb-6">
        {t('artistForm.title')}
      </h2>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Nombre */}
          <div>
            <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
              {t('artistForm.firstName')} <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="firstName"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
            {validationErrors.firstName && (
              <p className="text-red-600 text-sm">{validationErrors.firstName}</p>
            )}
          </div>

          {/* Apellido */}
          <div>
            <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
              {t('artistForm.lastName')} <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="lastName"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
            {validationErrors.lastName && (
              <p className="text-red-600 text-sm">{validationErrors.lastName}</p>
            )}
          </div>
        </div>

        {/* Email */}
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
            {t('artistForm.email')} <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
          {validationErrors.email && (
            <p className="text-red-600 text-sm">{validationErrors.email}</p>
          )}
        </div>

        {/* Contraseña */}
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
            {t('artistForm.password')} <span className="text-red-500">*</span>
          </label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
          {validationErrors.password && (
            <p className="text-red-600 text-sm">{validationErrors.password}</p>
          )}
        </div>

        {/* Nombre artístico */}
        <div>
          <label htmlFor="artistName" className="block text-sm font-medium text-gray-700 mb-1">
            {t('artistForm.artistName')} <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="artistName"
            name="artistName"
            value={formData.artistName}
            onChange={handleChange}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
          {validationErrors.artistName && (
            <p className="text-red-600 text-sm">{validationErrors.artistName}</p>
          )}
        </div>

        {/* URL del artista */}
        <div>
          <label htmlFor="url" className="block text-sm font-medium text-gray-700 mb-1">
            {t('artistForm.url')} <span className="text-red-500">*</span>
          </label>
          <input
            type="url"
            id="url"
            name="url"
            value={formData.url}
            onChange={handleChange}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
          {validationErrors.url && (
            <p className="text-red-600 text-sm">{validationErrors.url}</p>
          )}
        </div>

        {/* Color picker */}
        <div className="relative">
          <label htmlFor="color" className="block text-sm font-medium text-gray-700 mb-2">
            {t('artistForm.color')}
          </label>
          <div className="flex items-center space-x-2">
            <div
              className="w-8 h-8 rounded-full border border-gray-300"
              style={{ backgroundColor: formData.color }}
            ></div>
            <input
              type="color"
              id="color"
              name="color"
              value={formData.color}
              onChange={handleChange}
              className="w-full h-10 px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
        </div>

        <div>
          <div className="mb-2">
            <label className="block font-semibold text-sm text-gray-600">{t('artistForm.mainImage')}</label>
            <label className="text-xs"> {t('artistForm.mainImageHint')}</label>
          </div>

          <input
            type="file"
            onChange={handleImageChange}
            className="block w-full text-sm text-gray-500"
            accept="image/*" />
          {validationErrors.images && (
            <p className="text-red-600 text-sm">{validationErrors.images}</p>
          )}
        </div>

        <div>
          <div className="mb-2">
            <label className="block font-semibold text-sm text-gray-600">{t('artistForm.banner')}</label>
          </div>

          <input
            type="file"
            onChange={handleBannerImage}
            className="block w-full text-sm text-gray-500"
            accept="image/*" />
          {validationErrors.bannerImage && (
            <p className="text-red-600 text-sm">{validationErrors.bannerImage}</p>
          )}
        </div>

        {/* Mensaje de error */}
        {errorMessage && (
          <p className="text-red-600 text-sm font-medium text-center">{errorMessage}</p>
        )}
        {/* Botón de envío */}
        <div className="flex justify-center">
          <button
            type="submit"
            className="bg-purple-600 hover:bg-purple-700 text-white font-semibold w-full md:w-1/2 py-3 rounded-xl shadow transition-all"
          >
            {t('artistForm.submit')}
          </button>
        </div>

      </form>

      {/* Volver */}
      <button
        onClick={() => navigate('/auth/login')}
        className="mt-6 text-sm text-purple-600 hover:underline block text-center"
      >
        {t('artistForm.backToLogin')}
      </button>
    </div><Footer /></>
  );
};

export default ArtistForm;
