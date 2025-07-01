import { useLocation, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Autoplay } from 'swiper/modules';
import 'swiper/css';
import Footer from './Footer';

const ArtistView = () => {
    const location = useLocation();
    const artistId = location.state?.artistId;
    const [artist, setArtist] = useState(null);
    const [bgColor, setBgColor] = useState(null);

    useEffect(() => {
        if (!artistId || isNaN(artistId)) return;

        const fetchArtist = async () => {
            try {
                const res = await fetch(`/api/artists/${artistId}`);
                if (!res.ok) throw new Error('Error al obtener artista');
                const data = await res.json();
                setArtist(data);
            } catch (error) {
                console.error(error);
            }
        };

        fetchArtist();
    }, [artistId]);

    useEffect(() => {
        if (artist?.primaryColor) {
            const timer = setTimeout(() => setBgColor(artist.primaryColor), 50);
            return () => clearTimeout(timer);
        } else {
            setBgColor('#ffffff');
        }
    }, [artist]);

    const getContrastTextColor = (bgColor) => {
        if (!bgColor) return 'black';
        const color = bgColor.charAt(0) === '#' ? bgColor.substring(1, 7) : bgColor;
        const r = parseInt(color.substring(0, 2), 16);
        const g = parseInt(color.substring(2, 4), 16);
        const b = parseInt(color.substring(4, 6), 16);
        const brightness = (r * 299 + g * 587 + b * 114) / 1000;
        return brightness > 160 ? 'black' : 'white';
    };

    const ProductCard = ({ product }) => {
        const textColor = getContrastTextColor(bgColor);

        return (
            <div className="w-full transition-transform duration-300 hover:scale-[1.03]">
                <div className="relative w-full h-[320px] md:h-[480px] rounded-2xl overflow-hidden shadow-xl bg-white/10 group">
                    {/* Imagen principal */}
                    <img
                        src={`/api/product${product.images[0]}`}
                        alt={product.name}
                        loading="lazy"
                        className="absolute inset-0 w-full h-full object-contain p-6 transition-opacity duration-500 group-hover:opacity-0"
                    />
                    {/* Imagen hover */}
                    {product.images[1] && (
                        <img
                            src={`/api/product${product.images[1]}`}
                            alt={`${product.name} hover`}
                            loading="lazy"
                            className="absolute inset-0 w-full h-full object-contain p-6 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                        />
                    )}
                    <div className="absolute inset-0 bg-black/10 group-hover:bg-black/20 transition duration-300" />
                </div>
                <div className="mt-3 px-2">
                    <h3 className="custom-font-shop-regular text-base md:text-lg font-semibold truncate" style={{ color: textColor }}>
                        {product.name}
                    </h3>
                    {product.onPromotion && product.discount > 0 ? (
                        <div className="flex items-center gap-2 mt-1">
                            <span className="line-through text-sm" style={{ color: textColor, opacity: 0.7 }}>
                                {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
                            </span>
                            <span className="text-sm font-bold text-red-500">
                                {product.price.toFixed(2)}€
                            </span>
                        </div>
                    ) : (
                        <span className="text-sm font-medium" style={{ color: textColor, opacity: 0.9 }}>
                            {product.price.toFixed(2)}€
                        </span>
                    )}
                </div>
            </div>
        );
    };
    if (!artist) return <p>Cargando artista...</p>;

    console.log(artist)
    return (
        <>
            <div
                className="artist-page min-h-screen p-4"
                style={{ backgroundColor: bgColor || '#ffffff', transition: 'background-color 0.8s ease' }}
            >
                {/* BANNER DEL ARTISTA */}
                <div className="group relative w-2/3 mx-auto items-center rounded-2xl overflow-hidden shadow-lg mb-6">
                    {/* Imagen de fondo */}
                    {artist.bannerPhoto && (
                        <img
                            src={`/api/artists/${artist.bannerPhoto}`}
                            alt={artist.name}
                            loading="lazy"
                            className="w-full h-auto object-contain transform transition duration-500 ease-in-out group-hover:scale-110"
                        />
                    )}
                </div>

                <h1 className='text-left custom-font uppercase'>Products</h1>
                <hr className='mb-2' style={{ height: '2px', backgroundColor: 'white' }} />
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
                    {artist.artistProducts.map((product, index) => (
                        <Link to={`/product/details/${product.id}`} key={index}>
                            <div className="flex justify-center">
                                <ProductCard product={product} />
                            </div>
                        </Link>
                    ))}
                </div>

                {/* EVENTOS */}
                <h1 className='text-left custom-font uppercase mt-8'>Latest Events</h1>
                <hr className='mb-2' style={{ height: '2px', backgroundColor: 'white' }} />

                {artist.artistEvents?.length > 0 ? (
                    <Swiper
                        modules={[Autoplay]}
                        spaceBetween={20}
                        slidesPerView={1.2}
                        loop
                        speed={800}
                        autoplay={{ delay: 3000, disableOnInteraction: false }}
                        breakpoints={{
                            640: { slidesPerView: 2 },
                            768: { slidesPerView: 3 },
                            1024: { slidesPerView: 4 },
                        }}
                        className="w-full"
                    >
                        {artist.artistEvents.map((event, index) => (
                            <SwiperSlide key={index}>
                                <div className="group relative w-full h-[600px] md:h-[800px] overflow-hidden shadow-lg">                                <img
                                    src={`/api/event/${event.image}`}
                                    alt={artist.name}
                                    loading="lazy"
                                    className="w-full h-full object-cover transform transition duration-500 ease-in-out group-hover:scale-110" />
                                </div>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                ) : (
                    <p className="text-white mt-4">No hay eventos disponibles</p>
                )}


            </div><Footer /></>
    );
};

export default ArtistView;
