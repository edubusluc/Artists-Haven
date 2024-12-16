import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';

const ArtistForm = () => {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    artistName: "",
    url: ""
  });

  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch("/api/artists/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        throw new Error("Error al registrar el artista");
      }

      const data = await response.json();

      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        artistName: "",
        url: ""
      });
      setErrorMessage("");
      navigate('/users');
    } catch (error) {
      setErrorMessage(error.message || "Error al registrar el artista");
    }
  };

  return (
    <div>
      <h2>Registro de Artista</h2>
      <form onSubmit={handleSubmit}>
        {/* Campos comunes de User */}
        <div>
          <label htmlFor="firstName">Nombre:</label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            value={formData.firstName}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label htmlFor="lastName">Apellido:</label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            value={formData.lastName}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label htmlFor="email">Correo electrónico:</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label htmlFor="password">Contraseña:</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>

        {/* Campos específicos de Artist */}
        <div>
          <label htmlFor="artistName">Nombre artístico:</label>
          <input
            type="text"
            id="artistName"
            name="artistName"
            value={formData.artistName}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label htmlFor="url">URL del artista:</label>
          <input
            type="url"
            id="url"
            name="url"
            value={formData.url}
            onChange={handleChange}
            required
          />
        </div>

        {/* Botón de envío */}
        <div>
          <button type="submit">Registrar Artista</button>
        </div>
      </form>

      {/* Mostrar mensajes de error */}
      {errorMessage && <p style={{ color: "red" }}>{errorMessage}</p>}
    </div>
  );
};

export default ArtistForm;
