import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { GoogleLogin } from '@react-oauth/google';
import UserList from './components/UserList';
import UserRegister from './components/UserRegister';
import ArtistsRegister from './components/ArtistsRegister';
import UserLogin from './components/UserLogin';
import Logout from './components/Logout';
import UserProfile from './components/UserProfile';
import EditProfile from './components/EditProfile';
import EmailForm from './components/EmailForm';
import VerificationForm from './components/VerificationForm';
import VerificationList from './components/VerificationList';
import { checkTokenExpiration } from './utils/authUtils';

const HomePage = () => {
  const [userEmail, setUserEmail] = useState(null);
  const rol = localStorage.getItem("role");

  useEffect(() => {
    if (!localStorage.getItem('firstTime')) {
      localStorage.setItem('firstTime', true);
    }
    checkTokenExpiration();
    const storedEmail = localStorage.getItem('userEmail');
    if (storedEmail) {
      setUserEmail(storedEmail);
    }
  }, []);

  const handleGoogleLoginSuccess = async (response) => {
    console.log('Google login successful:', response);
    const googleToken = response.credential;

    try {
      // Enviar el token de Google al backend usando fetch
      const backendResponse = await fetch('/api/auth/google-login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ idTokenString: googleToken }),
      });

      if (!backendResponse.ok) {
        throw new Error('Error during Google login');
      }

      const data = await backendResponse.json();
      const jwtToken = data.token;  // Asegúrate de obtener el token del objeto JSON

      // Guardar el token JWT en el almacenamiento local
      localStorage.setItem('authToken', jwtToken);
      localStorage.setItem('userEmail', data.email);

      // Redirigir al usuario a la página principal (u otra página)
      window.location.href = '/';
    } catch (error) {
      console.error('Error during Google login:', error);
    }
  };


  const handleGoogleLoginError = (error) => {
    console.log('Google login failed:', error);
  };

  return (
    <div>
      <h1>Bienvenido a la App</h1>

      {userEmail ? (
        <>
          <p>Email: {userEmail}</p>
          <p>Ver Perfil:</p>
          <Link to="/users/profile">
            <button>Perfil</button>
          </Link>
        </>
      ) : (
        <>
          <p>No has iniciado sesión:</p>
          <Link to="/artists/register">
            <button>Registrar Artista</button>
          </Link>
          <p>Login de usuario:</p>
          <Link to="/auth/login">
            <button>Login Usuario</button>
          </Link>
          <p>O inicia sesión con Google:</p>
          <GoogleLogin
            onSuccess={handleGoogleLoginSuccess}
            onError={handleGoogleLoginError}
          />
        </>
      )}

      <p>Haz clic en el siguiente botón para ver la lista de usuarios:</p>
      <Link to="/users">
        <button>Ir a UserList</button>
      </Link>

      <p>Mandar reporte</p>
      <Link to="/email">
        <button>Report</button>
      </Link>

      <br />
      <p>O haz clic para registrar un nuevo usuario:</p>
      <Link to="/user/register">
        <button>Registrar Usuario</button>
      </Link>
      <p>O haz clic para registrar un nuevo artista:</p>

      {rol === "ARTIST" && <Link to="/verification">
        <button>Send Verification Request</button>
      </Link>}

      {rol === "ADMIN" && <Link to="/admin/verification/pending">
        <button>View Verification Request</button>
      </Link>}

      {userEmail && <Logout />}
    </div>
  );
};

const App = () => {
  return (
    <GoogleOAuthProvider clientId="1048927197271-g7tartu6gacs0jv8fgoa5braq8b2ck7p.apps.googleusercontent.com">
      <Router>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/users" element={<UserList />} />
          <Route path="/user/register" element={<UserRegister />} />
          <Route path="/artists/register" element={<ArtistsRegister />} />
          <Route path="/auth/login" element={<UserLogin />} />
          <Route path="/users/profile" element={<UserProfile />} />
          <Route path="/profile/edit" element={<EditProfile />} />
          <Route path="/email" element={<EmailForm />} />
          <Route path="/verification" element={<VerificationForm />} />
          <Route path="/admin/verification/pending" element={<VerificationList />} />
        </Routes>
      </Router>
    </GoogleOAuthProvider>
  );
};

export default App;
