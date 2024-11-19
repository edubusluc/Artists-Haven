import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import UserList from './components/UserList';
import UserRegister from './components/UserRegister';
import ArtistsRegister from './components/ArtistsRegister';
import UserLogin from './components/UserLogin';
import Logout from './components/Logout';

const HomePage = () => {
  // Estado para almacenar el email del usuario si está logueado
  const [userEmail, setUserEmail] = useState(null);

  useEffect(() => {
    // Verifica si hay un email almacenado en el localStorage (o desde el contexto si lo usas)
    const storedEmail = localStorage.getItem('userEmail');
    if (storedEmail) {
      setUserEmail(storedEmail);
    }
  }, []);

  return (
    <div>
      <h1>Bienvenido a la App</h1>
      
      {/* Mostrar el email del usuario si está registrado */}
      {userEmail ? (
        <p>Usuario registrado: {userEmail}</p>
      ) : (
        <p>No has iniciado sesión. Regístrate o inicia sesión.</p>
      )}

      <p>Haz clic en el siguiente botón para ver la lista de usuarios:</p>
      <Link to="/users">
        <button>Ir a UserList</button>
      </Link>
      <br />
      <p>O haz clic para registrar un nuevo usuario:</p>
      <Link to="/user/register">
        <button>Registrar Usuario</button>
      </Link>
      <p>O haz clic para registrar un nuevo artista:</p>
      <Link to="/artists/register">
        <button>Registrar Artista</button>
      </Link>
      <p>Login de usuario:</p>
      <Link to="/auth/login">
        <button>Login Usuario</button>
      </Link>

      {/* Botón de logout si el usuario está autenticado */}
      {userEmail && <Logout />}
    </div>
  );
};

const App = () => {
  return (
    <Router>
      <Routes>
        {/* Ruta para la página de inicio */}
        <Route path="/" element={<HomePage />} />
        
        {/* Ruta para la lista de usuarios */}
        <Route path="/users" element={<UserList />} />

        {/* Ruta para el registro de usuarios */}
        <Route path="/user/register" element={<UserRegister />} />

        {/* Ruta para el registro de artistas */}
        <Route path="/artists/register" element={<ArtistsRegister />} />

        {/* Ruta para el login de usuarios */}
        <Route path="/auth/login" element={<UserLogin />} />
      </Routes>
    </Router>
  );
};

export default App;
