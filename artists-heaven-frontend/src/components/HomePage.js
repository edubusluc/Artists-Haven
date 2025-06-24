import { useEffect, useState } from 'react';
import { useNavigate } from "react-router";
import { Link } from 'react-router-dom';
import Footer from './Footer';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import { Autoplay } from 'swiper/modules';
import TshirtViewer from './TshirtViewer';
import { Suspense } from 'react';


const HomePage = () => {
  const rol = localStorage.getItem('role');
  const [activeSection, setActiveSection] = useState('intro');
  const [product12, setProducts12] = useState([]);
  const [artists, setArtists] = useState([]);

  useEffect(() => {
    fecth12Products();
    fetchMainArtists();
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

  const fecth12Products = async () => {
    try {
      const response = await fetch('/api/product/sorted12Product', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Error al obtener los productos promocionados');
      }
      const data = await response.json();
      setProducts12(data);
    } catch (error) {
      console.error(error.message);
    }
  };

  const fetchMainArtists = async () => {
    try {
      const response = await fetch('/api/artists/main', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Error al obtener los artistas');
      }
      const data = await response.json();
      setArtists(data);
    } catch (error) {
      console.error(error.message);
    }
  };

  console.log(artists);

  const ProductCard = ({ product }) => {
    const [isHovered, setIsHovered] = useState(false);

    return (
      <div
        className="w-full"
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        <div
          className="relative w-full overflow-hiddens h-[300px] md:h-[600px] flex items-center justify-center"
          style={{
            backgroundColor: '#f7f7f7',
          }}
        >
          {/* Imagen original */}
          <img
            src={`/api/product${product.images[0]}`}
            alt={product.name}
            className={`absolute w-auto h-auto object-contain transition-all duration-400 ease-in-out transform
            ${isHovered ? 'opacity-0 scale-90 blur-sm' : 'opacity-100 scale-100'}`}
          />

          {/* Imagen hover */}
          {product.images[1] && (
            <img
              src={`/api/product${product.images[1]}`}
              alt={`${product.name} hover`}
              className={`absolute w-auto h-auto object-contain transition-all duration-400 ease-in-out transform
              ${isHovered ? 'opacity-100 scale-100 blur-0' : 'opacity-0 scale-90'}`}
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
  };

  return (
    <>
      <div className="p-6 bg-white">
        {/* CAMISETA 3D */}
        <Suspense fallback={<span>Cargando camiseta...</span>}>
          <TshirtViewer />
        </Suspense>

        {/* Título y subtítulo */}
        <div className='flex justify-between mt-10'>
          <p className="custom-font-shop" style={{ fontSize: '25px', color: '#909497', fontSize: '40px' }}>BEST SELLERS NEW ACCESSORIES</p>
          <p>Ver todo</p>
        </div>
        {/* Grid de productos */}
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
          {product12.map((product, index) => (
            <Link to={`/product/details/${product.id}`} key={index}>
              <div className="flex justify-center">
                <ProductCard product={product} />
              </div>
            </Link>
          ))}
        </div>

        {/* Sección de Artistas con carrusel */}
        <div className="mt-12 space-y-4">
          <p className="text-lg font-medium text-gray-700">Artistas</p>
          <Swiper
            modules={[Autoplay]}
            spaceBetween={20}
            slidesPerView={1.2}
            loop={true}
            autoplay={{
              delay: 3000,
              disableOnInteraction: false,
            }}
            breakpoints={{
              640: {
                slidesPerView: 2,
              },
              768: {
                slidesPerView: 3,
              },
              1024: {
                slidesPerView: 4,
              },
            }}
            className="w-full"
          >
            {artists.map((artist, index) => (

              <SwiperSlide key={index}>
                <div className="group relative w-full h-[600px] md:h-[800px] overflow-hidden shadow-lg">
                  <img
                    src={`/api/artists/${artist.mainPhoto}`}
                    alt={artist.name}
                    className="w-full h-full object-cover z-0 transform transition duration-500 ease-in-out group-hover:scale-110"
                  />
                  <div className="absolute bottom-0 right-0 z-10 flex items-end justify-end h-full w-full pr-2 pb-2">
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
