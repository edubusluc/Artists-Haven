import { useState } from "react";
import { useNavigate } from 'react-router-dom';

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

  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();
  const [images, setImages] = useState([]);
  const [bannerImage, setBannerImage] = useState([]);
  const [validationError, setValidationError] = useState("");

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];
    const invalidFiles = files.filter(file => !allowedTypes.includes(file.type));
    if (invalidFiles.length > 0) {
      setErrorMessage("Solo se permiten archivos de imagen válidos (jpg, png, gif, bmp, webp).");
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
      setErrorMessage("Solo se permiten archivos de imagen válidos (jpg, png, gif, bmp, webp).");
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
    setValidationError("");

    // Validar que haya imágenes
    if (images.length === 0 || bannerImage === 0) {
      setValidationError("Debes seleccionar al menos una imagen válida.");
      return;
    }

    const data = new FormData();

    // Añadir campos de texto
    Object.keys(formData).forEach(key => {
      data.append(key, formData[key]);
    });

    data.append("image", images[0]); // Asegúrate de que el backend espere `image`
    data.append("bannerImage", bannerImage[0]);

    try {
      const response = await fetch("/api/artists/register", {
        method: "POST",
        body: data,
      });

      if (!response.ok) {
        throw new Error("Error al registrar el artista");
      }

      await response.json();

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

  return (
    <div className="max-w-3xl mx-auto mt-10 p-8 bg-white shadow-2xl rounded-2xl">
      <h2 className="text-3xl font-bold text-center text-purple-700 mb-6">
        Registro de Artista
      </h2>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Nombre */}
          <div>
            <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
              Nombre <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="firstName"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
          </div>

          {/* Apellido */}
          <div>
            <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
              Apellido <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="lastName"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
          </div>
        </div>

        {/* Email */}
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
            Correo electrónico <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>

        {/* Contraseña */}
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
            Contraseña <span className="text-red-500">*</span>
          </label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>

        {/* Nombre artístico */}
        <div>
          <label htmlFor="artistName" className="block text-sm font-medium text-gray-700 mb-1">
            Nombre artístico <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="artistName"
            name="artistName"
            value={formData.artistName}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>

        {/* URL del artista */}
        <div>
          <label htmlFor="url" className="block text-sm font-medium text-gray-700 mb-1">
            URL del artista <span className="text-red-500">*</span>
          </label>
          <input
            type="url"
            id="url"
            name="url"
            value={formData.url}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>

        {/* Color picker */}
        <div className="relative">
          <label htmlFor="color" className="block text-sm font-medium text-gray-700 mb-2">
            Color preferido
          </label>
          <div className="flex items-center space-x-2">
            {/* Color preview */}
            <div
              className="w-8 h-8 rounded-full border border-gray-300"
              style={{ backgroundColor: formData.color }}
            ></div>
            {/* Color picker input */}
            <input
              type="color"
              id="color"
              name="color"
              value={formData.color}
              onChange={handleChange}
              className="w-full h-10 px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
          </div>
        </div>

        <div>
          <div className="mb-2">
            <label className="block font-semibold text-sm text-gray-600">Imágen Principal</label>
            <label className="text-xs"> Se recomienda un tamaño de 730x1300</label>
          </div>

          <input
            type="file"
            onChange={handleImageChange}
            className="block w-full text-sm text-gray-500"
            required
            accept="image/*"

          />
        </div>

        <div>
          <div className="mb-2">
            <label className="block font-semibold text-sm text-gray-600">Banner</label>
          </div>

          <input
            type="file"
            onChange={handleBannerImage}
            className="block w-full text-sm text-gray-500"
            accept="image/*"
          />
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
            Registrar Artista
          </button>
        </div>

      </form>

      {/* Volver */}
      <button
        onClick={() => navigate('/auth/login')}
        className="mt-6 text-sm text-purple-600 hover:underline block text-center"
      >
        Volver al inicio
      </button>
    </div>
  );
};

export default ArtistForm;
