import { useEffect, useState, useCallback, Suspense, lazy } from 'react';
import { Link } from 'react-router-dom';
import Footer from './Footer';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import { Autoplay } from 'swiper/modules';
import React from 'react';
import ConcertMap from './Event/ConcertMap';
import { useTranslation } from 'react-i18next';
import userProduct from '../util-image/userproduct1.png';
import ProductCard from './product/ProductCard';


const TshirtViewer = lazy(() => import('./TshirtViewer'));

// Constantes de API
const API = {
  sorted12: '/api/product/sorted12Product',
  mainArtists: '/api/artists/main',
  collections: '/api/product/promoted-collections',
  collection: (name) => `/api/product/collection/${name}`,
};

// Componente de tarjeta de producto

// Lista de 12 productos destacados
const Product12List = React.memo(({ products }) => (
  <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
    {products.map((product, index) => (
      <Link to={`/product/details/${product.id}`} key={index}>
        <div className="flex justify-center mt-5">
          <ProductCard product={product} />
        </div>
      </Link>
    ))}
  </div>
));

const HomePage = () => {
  const [product12, setProducts12] = useState([]);
  const [artists, setArtists] = useState([]);
  const [isVisible, setIsVisible] = useState(false);
  const [collections, setCollections] = useState([]);
  const [displayedProducts, setDisplayedProducts] = useState([]);
  const [activeCollection, setActiveCollection] = useState(null);
  const [fade, setFade] = useState(true);
  const [loadingCollection, setLoadingCollection] = useState(false);
  const { t, i18n } = useTranslation();
  const language = i18n.language;

  // Funciones de fetch
  const fetchData = useCallback(async (url) => {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`Error al obtener datos de ${url}`);
    return await res.json();
  }, []);

  const fetch12Products = useCallback(async () => {
    try {
      const response = await fetchData(API.sorted12);
      setProducts12(response.data);
    } catch (error) {
      console.error(error.message);
    }
  }, [fetchData]);

  const fetchMainArtists = useCallback(async () => {
    try {
      const response = await fetchData(API.mainArtists);
      setArtists(response.data);
    } catch (error) {
      console.error(error.message);
    }
  }, [fetchData]);

  const handleCollectionChange = async (collectionName) => {
    setFade(false);
    setLoadingCollection(true);
    try {
      const response = await fetchData(API.collection(collectionName));
      setTimeout(() => {
        setActiveCollection(collectionName);
        setDisplayedProducts(response.data);
        setFade(true);
        setLoadingCollection(false);
      }, 300);
    } catch (error) {
      console.error(error.message);
      setLoadingCollection(false);
    }
  };

  // Efectos
  useEffect(() => {
    fetch12Products();
    fetchMainArtists();
  }, [fetch12Products, fetchMainArtists]);

  useEffect(() => {
    const loadCollections = async () => {
      try {
        const response = await fetchData(API.collections);
        setCollections(response.data);
        // Solo si hay colecciones promocionadas, elegir la primera para mostrar productos
        const promotedCollections = response.data.filter(c => c.isPromoted);
        if (promotedCollections.length > 0) {
          setActiveCollection(promotedCollections[0].name);
          handleCollectionChange(promotedCollections[0].name);
        } else {
          // No hay colecciones promocionadas: limpiar productos y colección activa
          setActiveCollection(null);
          setDisplayedProducts([]);
        }
      } catch (error) {
        console.error(error.message);
      }
    };
    loadCollections();
  }, [fetchData]);

  useEffect(() => {
    if (!product12.length) return;
    const imageUrls = product12.flatMap(p => p.images.map(img => `/api/product${img}`));
    Promise.all(imageUrls.map(src => new Promise(res => {
      const img = new Image();
      img.src = src;
      img.onload = img.onerror = res;
    }))).then(() => setTimeout(() => setIsVisible(true), 30));
  }, [product12]);

  return (
    <>
      <div
        className="bg-white"
        style={{
          opacity: isVisible ? 1 : 0,
          transition: 'opacity 0.7s ease-in-out',
        }}
      >
        <Suspense fallback={<div className="text-center">Cargando camiseta...</div>}>
          <TshirtViewer />
        </Suspense>

        <div className="p-6">
          {/* Navegación colecciones */}
          <div className="flex flex-col sm:flex-row sm:justify-between gap-4 sm:gap-0">
            {/* Colecciones: scroll horizontal en móvil */}
            <div className="flex overflow-x-auto gap-4 scrollbar-hide sm:overflow-visible">
              {collections.filter(coll => coll.isPromoted).map((coll) => (
                <button
                  key={coll.name}
                  onClick={() => {
                    if (coll.name !== activeCollection) {
                      handleCollectionChange(coll.name);
                    }
                  }}
                  className={coll.name === activeCollection ? '' : 'hover:scale-105'}
                  style={{ flex: '0 0 auto' }} // evita que se contraigan
                >
                  <p
                    className={`custom-font-shop text-2xl sm:text-3xl whitespace-nowrap transition-transform duration-300 transform ${coll.name === activeCollection ? 'custom-font-shop-black' : 'text-gray-400'
                      }`}
                  >
                    {coll.name}
                  </p>
                </button>
              ))}
            </div>

            {/* Enlace "Ver TODO" */}
            <div className="flex justify-end sm:justify-start mt-2 sm:mt-0">
              {displayedProducts.length > 0 && (
                <Link to={`/shop/${activeCollection}`}>
                  <p className="custom-font-shop custom-font-shop-black text-base sm:text-lg">
                    {t('homepage.viewAll')}
                  </p>
                </Link>
              )}
            </div>
          </div>

          {/* Productos colección activa con efecto escalera */}

          {activeCollection && displayedProducts.length > 0 && (
            <div
              className={`grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full transition-opacity duration-500 ${fade ? 'opacity-100' : 'opacity-0'}`}
            >
              {displayedProducts.map((product, idx) => (
                <Link to={`/product/details/${product.id}`} key={idx}>
                  <div
                    className={`translate-y-5 ${loadingCollection ? 'opacity-50' : 'opacity-100'}`}
                    style={{
                      animation: loadingCollection ? 'none' : `staggerFade 0.4s ease forwards`,
                      animationDelay: loadingCollection ? '0s' : `${idx * 0.1}s`,
                    }}
                  >
                    <ProductCard product={product} />
                  </div>
                </Link>
              ))}
            </div>
          )}

          {/* Navegación categorías */}
          <div className="flex gap-4 mt-6 overflow-y-auto custom-font-shop text-3xl">
            <Link to="/shop" className="transition-all duration-300 transform  hover:scale-105 hover:text-black focus:scale-105 focus:text-black active:scale-105 active:text-black">{t('all')}</Link>
            <Link to="/shop/camisetas" className="transition-all duration-300 transform hover:scale-105 hover:text-black focus:scale-105 focus:text-black active:scale-105 active:text-black">{t('t-shirts')}</Link>
            <Link to="/shop/pantalones" className="transition-all duration-300 transform hover:scale-105 hover:text-black focus:scale-105 focus:text-black active:scale-105 active:text-black">{t('pants')}</Link>
            <Link to="/shop/sudaderas" className="transition-all duration-300 transform hover:scale-105 hover:text-black focus:scale-105 focus:text-black active:scale-105 active:text-black">{t('hoodies')}</Link>
            <Link to="/shop/accesorios" className="transition-all duration-300 transform hover:scale-105 hover:text-black focus:scale-105 focus:text-black active:scale-105 active:text-black">{t('accessories')}</Link>
          </div>

          {/* Lista 12 productos */}
          <Product12List products={product12} />

          {/* Carrusel artistas */}
          <div className="mt-12 space-y-4">
            <p className="custom-font-shop custom-font-shop-black text-3xl">{t('artists')}</p>
            <Swiper
              modules={[Autoplay]}
              spaceBetween={20}
              slidesPerView={1.2}
              loop
              autoplay={{ delay: 3000, disableOnInteraction: false }}
              breakpoints={{
                640: { slidesPerView: 2 },
                768: { slidesPerView: 3 },
                1024: { slidesPerView: 4 },
              }}
              className="w-full"
            >
              {artists.map((artist, index) => (
                <SwiperSlide key={index}>
                  <Link to={`/artist/${artist.name}`} state={{ artistId: artist.id }}>
                    <div className="group relative w-full h-[600px] md:h-[800px] overflow-hidden shadow-lg">
                      <img
                        src={`/api/artists/${artist.mainPhoto}`}
                        alt={artist.name}
                        loading="lazy"
                        className="w-full h-full object-cover transform transition duration-500 ease-in-out group-hover:scale-110"
                      />
                      <div className="absolute bottom-0 right-0 flex items-end justify-end h-full w-full pr-2 pb-2">
                        <p
                          className="custom-font-shop text-white font-bold"
                          style={{
                            writingMode: 'vertical-rl',
                            transform: 'rotate(180deg)',
                            fontSize: '80px',
                            lineHeight: 1,
                            textAlign: 'right',
                            whiteSpace: 'nowrap',
                            textShadow: '2px 2px 4px rgba(0, 0, 0, 0.3)',
                          }}
                        >
                          {artist.name}
                        </p>
                      </div>
                    </div>
                  </Link>
                </SwiperSlide>
              ))}
            </Swiper>
          </div>
          <p className="mt-5 custom-font-shop custom-font-shop-black text-3xl">{t('header.forFan')}</p>
          <div className="relative w-full h-[600px] md:h-[800px] mt-5 overflow-hidden">
            <img
              src={userProduct}
              alt="Producto"
              className="w-full h-full object-cover"
            />
            {/* Botón de orden */}
            <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2 z-10">
              <Link to="/forFan">
              <button className="button-yellow-border">
                UPLOAD YOUR ART
              </button>
              </Link>
            </div>
          </div>

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
