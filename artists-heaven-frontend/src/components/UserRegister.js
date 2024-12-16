import React, { useState } from 'react';  // Importa React
import { useNavigate } from 'react-router-dom';

const UserRegister = () => {
  const [user, setUser] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    username: ''
  });
  const navigate = useNavigate();

  const [errorMessage, setErrorMessage] = useState(''); // Para mostrar los errores

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUser({ ...user, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      // Realizar la solicitud POST al backend con fetch
      const response = await fetch('/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(user), // Convertir el objeto a JSON
      });

      if (!response.ok) {
        throw new Error('Error al registrar el usuario');
      }

      const data = await response.json();

      // Limpiar el formulario después del registro exitoso
      setUser({ firstName: '', lastName: '', email: '', password: '', username: '' });
      setErrorMessage(''); // Limpiar cualquier mensaje de error

      // Redirigir al usuario
      window.location.href = '/users';
    } catch (error) {
      // Manejar el error si ocurre
      setErrorMessage(error.message || 'Error al registrar el usuario');
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
          <label>Usuario:</label>
          <input
            type="text"
            name="username"
            value={user.username}
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

      <button onClick={() => navigate('/')} className="btn btn-primary">
        Volver al inicio
      </button>

      {/* Mostrar mensaje de error si existe */}
      {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
    </div>
  );
};

export default UserRegister;
