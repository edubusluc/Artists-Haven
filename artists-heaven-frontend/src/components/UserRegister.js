import React, { useState } from 'react';
import axios from 'axios';  // Importa axios

const UserRegister = () => {
  const [user, setUser] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

  const [errorMessage, setErrorMessage] = useState(''); // Para mostrar los errores

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUser({ ...user, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      // Utiliza axios para enviar los datos al backend
      const response = await axios.post('/api/users/register', user);
      
      // Limpiar el formulario después del registro exitoso
      setUser({ firstName: '', lastName: '', email: '', password: ''});
      setErrorMessage(''); // Limpiar cualquier mensaje de error

      // Redirigir al usuario
      window.location.href = '/users';
    } catch (error) {
      // Manejar el error si ocurre
      setErrorMessage(error.response?.data || 'Error al registrar el usuario');
    }
  };

  return (
    <div>
      <h2>Registro de Usuario</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Nombre:</label>
          <input
            type="text"
            name="firstName"
            value={user.firstName}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Apellido:</label>
          <input
            type="text"
            name="lastName"
            value={user.lastName}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Correo Electrónico:</label>
          <input
            type="email"
            name="email"
            value={user.email}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Contraseña:</label>
          <input
            type="password"
            name="password"
            value={user.password}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit">Registrar Usuario</button>
      </form>

      {/* Mostrar mensaje de error si existe */}
      {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
    </div>
  );
};

export default UserRegister;
