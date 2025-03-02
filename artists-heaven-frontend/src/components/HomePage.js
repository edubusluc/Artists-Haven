import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import Logout from './Logout';
import { checkTokenExpiration } from '../utils/authUtils';

const HomePage = () => {
  const [userEmail, setUserEmail] = useState(null);
  const rol = localStorage.getItem('role');

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
      const backendResponse = await fetch('/api/auth/google-login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idTokenString: googleToken }),
      });

      if (!backendResponse.ok) {
        throw new Error('Error during Google login');
      }

      const data = await backendResponse.json();
      const jwtToken = data.token;

      localStorage.setItem('authToken', jwtToken);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('role', data.role);

      window.location.href = '/';
    } catch (error) {
      console.error('Error during Google login:', error);
    }
  };

  const handleGoogleLoginError = (error) => {
    console.log('Google login failed:', error);
  };

  const renderUserActions = () => (
    <div className="actions">
      <p>Email: {userEmail}</p>
      <p>Ver Perfil:</p>
      <Link to="/users/profile">
        <button>Perfil</button>
      </Link>
      <Logout />
    </div>
  );

  const renderGuestActions = () => (
    <div className="actions">
      <p>No has iniciado sesión:</p>
      <Link to="/artists/register">
        <button>Registrar Artista</button>
      </Link>
      <p>Login de usuario:</p>
      <Link to="/auth/login">
        <button>Login Usuario</button>
      </Link>
      <p>O inicia sesión con Google:</p>
      <GoogleLogin onSuccess={handleGoogleLoginSuccess} onError={handleGoogleLoginError} />
    </div>
  );

  const renderAdminActions = () => (
    <div className="admin-actions">
      {rol === 'ADMIN' && (
        <Link to="/admin/verification/pending">
          <button>View Verification Request</button>
        </Link>
      )}
    </div>
  );

  const renderLinks = () => (
    <div className="links-container">
      <div className="category">
        <h3>User Registration</h3>
        <p>O haz clic para registrar un nuevo usuario:</p>
        <Link to="/user/register">
          <button>Registrar Usuario</button>
        </Link>
      </div>

      <div className="category">
        <h3>Login/Logout</h3>
        {userEmail ? renderUserActions() : renderGuestActions()}
      </div>

      <div className="category">
        <h3>Product</h3>
        <p>Listado de Producto</p>
        <Link to="/product/all">
          <button>All Product</button>
        </Link>
        {rol === 'ADMIN' && (
          <><p>Nuevo Producto</p><Link to="/product/new">
            <button>New Product</button>
          </Link></>
        )}
      </div>

      <div className="category">
        <h3>Orders</h3>
        <Link to="/orders/myOrders">
          <button>My Orders</button>
        </Link>
      </div>

      <div className="category">
        <h3>Event</h3>
        {rol == 'ARTIST' && (
          <>
            <p>Nuevo Evento</p>
            <Link to="/event/new">
              <button>New Event</button>
            </Link>
            <p>Mis Eventos</p>
            <Link to="/event/allMyEvents">
              <button>My Events</button>
            </Link>
          </>
        )}
        <p>Eventos</p>
        <Link to="/event/allEvents">
          <button>Events</button>
        </Link>

      </div>

      <div className="category">
        <h3>Verification</h3>
        {renderAdminActions()}
        {rol === 'ARTIST' && (
          <div className="category">
            <Link to="/verification">
              <button>Send Verification Request</button>
            </Link>
          </div>
        )}
      </div>

      <div className="category">
        <h3>FAQ</h3>
        <Link to="/FAQ">
          <button>FAQ</button>
        </Link>
      </div>

      <div className="category">
        <h3>Report</h3>
        <p>Mandar reporte</p>
        <Link to="/email">
          <button>Report</button>
        </Link>
      </div>
    </div>
  );

  return (
    <div className="container">
      <h1>ARTISTS - HEAVEN</h1>
      {renderLinks()}
    </div>
  );
};

export default HomePage;
