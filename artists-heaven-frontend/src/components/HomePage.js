import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import Logout from './Logout';
import { checkTokenExpiration } from '../utils/authUtils';
import promoImg from '../util-image/image.png';
import Footer from '../components/Footer';


const HomePage = () => {
  const [userEmail, setUserEmail] = useState(null);
  const rol = localStorage.getItem('role');
  const [activeSection, setActiveSection] = useState('intro');
  const [promotedProducts, setPromotedProducts] = useState([]);

  useEffect(() => {
    fecthPromotedProducts();
    // if (!localStorage.getItem('firstTime')) {
    //   localStorage.setItem('firstTime', true);
    // }
    // checkTokenExpiration();
    // const storedEmail = localStorage.getItem('userEmail');
    // if (storedEmail) {
    //   setUserEmail(storedEmail);
    // }

    const sections = document.querySelectorAll('section');
    const observerOptions = {
      root: null,
      rootMargin: '0px',
      threshold: 0.6, // 60% visible
    };

    const observerCallback = (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const classList = entry.target.classList;
          if (classList.contains('section-1')) setActiveSection('intro');
          if (classList.contains('section-2')) setActiveSection('shop');
          if (classList.contains('section-3')) setActiveSection('artistes');
          if (classList.contains('section-4')) setActiveSection('quize');
        }
      });
    };

    const observer = new IntersectionObserver(observerCallback, observerOptions);
    sections.forEach((section) => observer.observe(section));

    return () => {
      sections.forEach((section) => observer.unobserve(section));
    };
  }, []);

  const fecthPromotedProducts = async () => {
    try {
      const response = await fetch('/api/product/allPromotedProducts', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Error al obtener los productos promocionados');
      }
      const data = await response.json();
      setPromotedProducts(data);
    } catch (error) {
      console.error(error.message);
    }
  };

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

  const scrollToSection = (id) => {
    const section = document.getElementById(id);
    if (section) {
      section.scrollIntoView({ behavior: 'smooth' });
    } else {
      console.warn(`Section with id "${id}" not found`);
    }
  };

  return (
    <div className="h-screen overflow-y-scroll snap-y snap-mandatory">
      <div className="fixed top-1/2 right-4 transform -translate-y-1/2 z-50 flex flex-col items-center space-y-4 text-white font-bold text-sm cursor-pointer">

        <div
          onClick={() => scrollToSection('intro')}
          className={`transition ${activeSection === 'intro' ? 'text-white' : 'text-gray-300'} custom-font`}
        >
          Intro
          <input
            type="checkbox"
            className="custom-checkbox"
            checked={activeSection === 'intro'}
            readOnly
          />
        </div>
        <div onClick={() => scrollToSection('shop')}
          className={`transition ${activeSection === 'shop' ? 'text-white' : 'text-gray-300'} custom-font`}>
          Shop
          <input type="checkbox"
            className="custom-checkbox"
            checked={activeSection === 'shop'}
            readOnly />
        </div>
        <div onClick={() => scrollToSection('artistes')}
          className={`transition ${activeSection === 'artistes' ? 'text-white' : 'text-gray-300'} custom-font`}>
          Artistes
          <input type="checkbox"
            className="custom-checkbox"
            checked={activeSection === 'artistes'}
            readOnly />
        </div>
      </div>
      <section id="intro" className="section-1 h-screen snap-start flex items-center justify-center bg-red-400">
        <h1 className="text-4xl text-white">Sección 1</h1>
      </section>

      <section id="shop" className="section-2 min-h-screen snap-start flex flex-col items-center justify-center bg-[#151e27] p-2 sm:p-4 overflow-y-auto">
        <div className="w-full h-full grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2">
          <div className='col-start-1 col-span-1'>
            <img
              src={promoImg}
              alt="Promoción"
              className="h-full object-cover rounded-xl"
            />
          </div>
          <div className='col-start-2 col-span-2 h-[60%] sm:h-[80%]'>
            {promotedProducts.length === 0 && (
              <p className="text-white">No hay productos promocionados.</p>
            )}

            <div className="grid grid-cols-2 gap-6 w-full max-w-6xl mx-auto h-full">
              {promotedProducts.map((product) => (
                <div key={product.id}>
                  <div className="bg-white p-6 rounded-3xl h-96 overflow-hidden">
                    {product.images.map((image, index) => (
                      <img
                        key={index}
                        src={`/api/product${image}`}
                        alt={image}
                        className="w-full h-full object-contain drop-shadow-strong"
                      />
                    ))}
                  </div>
                  <p className="custom-font mt-1 text-center">{product.name}</p>
                </div>
              ))}
            </div>
          </div>
          <div className='col-start-2 col-span-1 flex justify-center items-start mt-4 w-full h-full overflow-hidden'>
            <Link to="/product/all">
              <button className='button-yellow-border'>VIEW ALL</button>
            </Link>
          </div>
        </div>
      </section>

      <section id="artistes" className="section-3 h-screen snap-start flex items-center justify-center bg-blue-400">
        <h1 className="text-4xl text-white">Sección 3</h1>
      </section>
    </div>
  )
};

export default HomePage;
