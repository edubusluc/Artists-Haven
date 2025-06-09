import React, { useState, useEffect } from 'react';
import twitter from '../util-image/twitter.png';
import instagram from '../util-image/instagram.png';
import tiktok from '../util-image/tiktok.png';

const Footer = () => {
  // Estado para controlar ancho de pantalla
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handleResize = () => setWindowWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Decide estilos basados en ancho
  const isMobile = windowWidth <= 768;

  return (
    <footer
      style={{
        backgroundColor: '#ebebeb',
        padding: '4rem 2rem',
        textAlign: 'center',
        color: 'white',
        boxShadow: '0 -2px 10px rgba(0,0,0,0.1)',
        display: 'flex',
        justifyContent: 'flex-end',
        flexDirection: 'column',
      }}
    >
      <div
        className="w-full max-w-6xl mx-auto"
        style={{
          backgroundColor: 'black',
          borderRadius: '24px',
          padding: '2rem',
          boxShadow: '0 4px 15px rgba(0,0,0,0.15)',
          color: '#333',
          display: 'flex',
          flexDirection: isMobile ? 'column' : 'row',
          gap: isMobile ? '1.5rem' : '2rem',
          alignItems: isMobile ? 'center' : 'flex-start',
          justifyContent: 'space-between',
        }}
      >
        {/* Texto a la izquierda (o arriba en móvil) */}
        <p
          className="custom-font-footer text-white"
          style={{
            width: isMobile ? '100%' : 'calc(100% - 25rem)',
            minWidth: isMobile ? 'auto' : '250px',
            textAlign: isMobile ? 'center' : 'left',
          }}
        >
          STAY UP TO DATE ABOUT OUR LATEST OFFERS
        </p>

        {/* Columna con dos divs a la derecha (o debajo en móvil) */}
        <div
          className="flex flex-col gap-4"
          style={{
            width: isMobile ? '100%' : '25rem',
            minWidth: isMobile ? 'auto' : '25rem',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <div
            style={{
              backgroundColor: 'white',
              borderRadius: '24px',
              padding: '1rem',
              height: '4rem',
              width: '100%',
              textAlign: 'center',
              boxSizing: 'border-box',
            }}
          >
            Promo 1
          </div>

          <div
            style={{
              backgroundColor: 'white',
              borderRadius: '24px',
              padding: '1rem',
              height: '4rem',
              width: '100%',
              textAlign: 'center',
              boxSizing: 'border-box',

            }}
          >
            <p className="custom-font-footer" style={{ color: 'black' }}>subscribe</p>
          </div>
        </div>
      </div>

      {/* Contenedor para las imágenes con mismo ancho y centrado que el div negro */}
      <div
        className="w-full max-w-6xl mx-auto"
        style={{
          marginTop: '1.5rem',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'flex-start',
          gap: '0.5rem',
          alignItems: isMobile ? 'center' : 'flex-start',
        }}
      >
        <p className="custom-font-footer" style={{ color: 'black', textAlign: isMobile ? 'center' : 'left' }}>
          ARTISTS' - HEAVEN
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: isMobile ? 'center' : 'flex-start' }}>
          <img src={twitter} alt="twitter" style={{ height: '32px', cursor: 'pointer' }} />
          <a href="https://www.tiktok.com/@thisis.argentina?lang=es" target="_blank" rel="noopener noreferrer">
            <img src={tiktok} alt="tiktok" style={{ height: '32px', cursor: 'pointer' }} />
          </a>
          <img src={instagram} alt="instagram" style={{ height: '32px', cursor: 'pointer' }} />
        </div>
      </div>

      <p
        style={{
          marginTop: '3rem',
          fontSize: '0.9rem',
          color: '#444',
          textAlign: 'center',
        }}
      >
        © 2025 Mi Sitio Web. Todos los derechos reservados.
      </p>
    </footer>
  );
};

export default Footer;
