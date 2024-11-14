import React from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import UserList from './components/UserList'; // Vista de usuarios
import UserRegister from './components/UserRegister'; // Vista de registro de usuario
import ArtistsRegister from './components/ArtistsRegister'; // Vista de registro de usuario

const HomePage = () => (
  <div>
    <h1>Bienvenido a la App</h1>
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
  </div>
);

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

        {/* Ruta para el registro de usuarios */}
        <Route path="/artists/register" element={<ArtistsRegister />} />

      </Routes>
    </Router>
  );
}

export default App;
