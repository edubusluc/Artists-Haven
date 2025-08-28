import { useState, useEffect } from 'react';
import twitter from '../util-image/twitter.png';
import instagram from '../util-image/instagram.png';
import tiktok from '../util-image/tiktok.png';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const Footer = () => {
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);
  const { t } = useTranslation();

  useEffect(() => {
    const handleResize = () => setWindowWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const isMobile = windowWidth <= 768;

  return (
    <footer
      style={{
        backgroundColor: '#ffffff',
        padding: '4rem 2rem',
        textAlign: 'center',
        color: 'white',
        boxShadow: '0 -2px 10px rgba(0,0,0,0.1)',
        display: 'flex',
        justifyContent: 'flex-end',
        flexDirection: 'column',
      }}
    >
      {/* Contenedor principal */}
      <div
        className="w-full max-w-6xl mx-auto"
        style={{
          backgroundColor: 'black',
          borderRadius: '24px',
          padding: '2rem',
          boxShadow: '0 4px 15px rgba(0,0,0,0.15)',
          color: '#fff',
          display: 'flex',
          flexDirection: isMobile ? 'column' : 'row',
          gap: isMobile ? '2rem' : '4rem',
          alignItems: isMobile ? 'center' : 'flex-start',
          justifyContent: 'space-between',
        }}
      >
        {/* Columna izquierda */}
        <div style={{ flex: 1, textAlign: isMobile ? 'center' : 'left' }}>
          <h2
            className="custom-font-footer"
            style={{ fontSize: '1.5rem', fontWeight: '700', marginBottom: '1rem' }}
          >
            ARTISTS' - HEAVEN
          </h2>
          <p style={{ fontSize: '0.95rem', lineHeight: '1.5' }}>
            {t('footer.description')}
          </p>

        </div>

        {/* Columna enlaces rápidos */}
        <div style={{ flex: 1, textAlign: isMobile ? 'center' : 'left' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '1rem' }}>
            {t('footer.quickLinks')}
          </h3>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, lineHeight: '2' }}>
            <li><Link to="/about" style={{ color: 'white' }}>{t('footer.aboutUs')}</Link></li>
            <li><Link to="/FAQ" style={{ color: 'white' }}>{t('footer.faq')}</Link></li>
          </ul>
        </div>

        {/* Columna legal */}
        <div style={{ flex: 1, textAlign: isMobile ? 'center' : 'left' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '1rem' }}>
            {t('footer.legal')}
          </h3>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, lineHeight: '2' }}>
            <li><Link to="/privacy" style={{ color: 'white' }}>{t('footer.privacyPolicy')}</Link></li>
            <li><Link to="/terms" style={{ color: 'white' }}>{t('footer.termsAndConditions')}</Link></li>
          </ul>
        </div>

        {/* Columna contacto */}
        <div style={{ flex: 1, textAlign: isMobile ? 'center' : 'left' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '1rem' }}>
            {t('footer.contactUs')}
          </h3>
          <p style={{ fontSize: '0.85rem' }}>{t('footer.email')}</p>
          <p style={{ fontSize: '0.85rem' }}>{t('footer.location')}</p>

        </div>
      </div>

      {/* Redes sociales */}
      <div
        className="w-full max-w-6xl mx-auto"
        style={{
          marginTop: '2rem',
          display: 'flex',
          flexDirection: 'column',
          gap: '1rem',
        }}
      >
        <div
          style={{
            display: 'flex',
            gap: '1rem',
            justifyContent: isMobile ? 'center' : 'flex-start',
          }}
        >
          <a href="https://www.tiktok.com/@thisis.argentina?lang=es" target="_blank" rel="noopener noreferrer" aria-label="Tiktok">
            <img
              src={tiktok}
              alt="tiktok"
              style={{ height: '32px', cursor: 'pointer', transition: '0.3s' }}
              onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.1)')}
              onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1)')}
            />
          </a>
          <a href="https://www.instagram.com/thisis.argentina/" target="_blank" rel="noopener noreferrer" aria-label="Instagram">
            <img
              src={instagram}
              alt="instagram"
              style={{ height: '32px', cursor: 'pointer', transition: '0.3s' }}
              onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.1)')}
              onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1)')}
            />
          </a>
        </div>
      </div>

      {/* Copyright */}
      <p style={{ marginTop: '2rem', fontSize: '0.9rem', color: '#444', textAlign: 'center' }}>
        {t('footer.copyright')}
      </p>
    </footer>
  );
};

export default Footer;
