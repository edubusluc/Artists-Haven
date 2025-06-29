import { useEffect, useState, useCallback, Suspense, lazy } from 'react';
import { Link } from 'react-router-dom';
import Footer from './Footer';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import { Autoplay } from 'swiper/modules';

const TshirtViewer = lazy(() => import('./TshirtViewer'));

const HomePage = () => {
  const [activeSection, setActiveSection] = useState('intro');
  const [product12, setProducts12] = useState([]);
  const [artists, setArtists] = useState([]);

  const fetch12Products = useCallback(async () => {
    try {
      const response = await fetch('/api/product/sorted12Product');
      if (!response.ok) throw new Error('Error al obtener los productos');
      const data = await response.json();
      setProducts12(data);
    } catch (error) {
      console.error(error.message);
    }
  }, []);

  const fetchMainArtists = useCallback(async () => {
    try {
      const response = await fetch('/api/artists/main');
      if (!response.ok) throw new Error('Error al obtener los artistas');
      const data = await response.json();
      setArtists(data);
    } catch (error) {
      console.error(error.message);
    }
  }, []);

  useEffect(() => {
    fetch12Products();
    fetchMainArtists();

    const sections = document.querySelectorAll('section');
    const observerOptions = { root: null, rootMargin: '0px', threshold: 0.6 };

    const observerCallback = (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const cls = entry.target.classList;
          if (cls.contains('section-1')) setActiveSection('intro');
          if (cls.contains('section-2')) setActiveSection('shop');
          if (cls.contains('section-3')) setActiveSection('artistes');
          if (cls.contains('section-4')) setActiveSection('quize');
        }
      });
    };

    const observer = new IntersectionObserver(observerCallback, observerOptions);
    sections.forEach((s) => observer.observe(s));
    return () => sections.forEach((s) => observer.unobserve(s));
  }, [fetch12Products, fetchMainArtists]);

  const ProductCard = ({ product }) => (
    <div className="w-full group">
      <div className="relative w-full h-[300px] md:h-[600px] flex items-center justify-center bg-gray-100 overflow-hidden">
        <img
          src={`/api/product${product.images[0]}`}
          alt={product.name}
          loading="lazy"
          className="absolute object-contain transition-all duration-500 ease-in-out group-hover:opacity-0 group-hover:scale-95"
        />
        {product.images[1] && (
          <img
            src={`/api/product${product.images[1]}`}
            alt={`${product.name} hover`}
            loading="lazy"
            className="absolute object-contain opacity-0 transition-all duration-500 ease-in-out group-hover:opacity-100 group-hover:scale-100"
          />
        )}
      </div>
      <div className="mt-3 text-left">
        <p className="custom-font-shop-regular" style={{ color: 'black' }}>{product.name}</p>
        {product.onPromotion && product.discount > 0 ? (
          <div className="flex items-center gap-2">
            <span className="custom-font-shop-regular line-through" style={{ color: '#909497', fontSize: '15px' }}>{(product.price / ((100 - product.discount) / 100)).toFixed(2)}€</span>
            <span className="custom-font-shop-regular" style={{ color: 'red' }}>
              {product.price.toFixed(2)}€
            </span>
          </div>
        ) : (
          <span className="custom-font-shop-regular" style={{ color: '#909497', fontSize: '15px' }}>{product.price.toFixed(2)}€</span>
        )}
      </div>
    </div>
  );

  return (
    <>
      <div className="p-6 bg-white">
        <Suspense fallback={<div className="text-center">Cargando camiseta...</div>}>
          <TshirtViewer />
        </Suspense>

        <div className="flex justify-between mt-10">
          <p className="custom-font-shop text-gray-500 text-4xl">BEST SELLERS NEW ACCESSORIES</p>
          <p>Ver todo</p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
          {product12.map((product, index) => (
            <Link to={`/product/details/${product.id}`} key={index}>
              <div className="flex justify-center">
                <ProductCard product={product} />
              </div>
            </Link>
          ))}
        </div>

        <div className="mt-12 space-y-4">
          <p className="text-lg font-medium text-gray-700">Artistas</p>
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
              </SwiperSlide>
            ))}
          </Swiper>
        </div>
      </div>
      <Footer />
    </>
  );
};

export default HomePage;
