import { useState, useEffect, Suspense, lazy } from 'react';
import Footer from './Footer';
import 'swiper/css';
import ConcertMap from './Event/ConcertMap';
import { useTranslation } from 'react-i18next';


const TshirtViewer = lazy(() => import('./TshirtViewer'));

const HomePage = () => {

  const [isVisible, setIsVisible] = useState(false);
  const { t } = useTranslation();

  useEffect(() => {
    const timeout = setTimeout(() => setIsVisible(true), 50);
    return () => clearTimeout(timeout);
  }, []);


  return (
    <>
      <div
        className="bg-white"
        style={{
          opacity: isVisible ? 1 : 0,
          transition: 'opacity 0.8s ease-in-out',
        }}
      >
        <Suspense fallback={<div className="text-center">Cargando camiseta...</div>}>
          <TshirtViewer />
        </Suspense>

        <div className="p-6">
          <div id="upcoming-events" className='mt-5'>
            <p className='custom-font-shop text-3xl custom-font-shop-black mb-5'>{t('upCommingEvents')}</p>
            <ConcertMap />
          </div>
        </div>

      </div >
      {isVisible && <Footer />
      }
    </>
  );
};

export default HomePage;
