import { useLocation, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Autoplay } from 'swiper/modules';
import 'swiper/css';
import Footer from './Footer';
import { useTranslation } from 'react-i18next';

const getContrastTextColor = (bgColor) => {
    if (!bgColor) return 'black';
    const color = bgColor.charAt(0) === '#' ? bgColor.substring(1, 7) : bgColor;
    const r = parseInt(color.substring(0, 2), 16);
    const g = parseInt(color.substring(2, 4), 16);
    const b = parseInt(color.substring(4, 6), 16);
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;
    return brightness > 160 ? 'black' : 'white';
};

const ProductCard = ({ product, bgColor }) => {
    const { t } = useTranslation();
    const textColor = getContrastTextColor(bgColor);

    // 🔹 Verificamos stock
    const hasStock = product.colors?.some(color =>
        product.section === "ACCESSORIES"
            ? (color.availableUnits ?? 0) > 0
            : Object.values(color.sizes || {}).some(qty => qty > 0)
    );

    const isDark = (hex) => {
        if (!hex) return false;
        let c = hex.substring(1); // quitar "#"
        if (c.length === 3) {
            c = c.split("").map(ch => ch + ch).join(""); // expandir #123 -> #112233
        }
        const r = parseInt(c.substr(0, 2), 16);
        const g = parseInt(c.substr(2, 2), 16);
        const b = parseInt(c.substr(4, 2), 16);

        // fórmula relativa de luminancia
        const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;

        return luminance < 0.5; // si < 0.5 lo consideramos oscuro
    };


    return (
        <div className="w-full group max-h-[700px] flex flex-col">
            {/* Contenedor de imagen */}
            <div
                className={`relative w-full h-[700px] flex items-center justify-center overflow-hidden rounded-xl shadow-md
          ${!hasStock ? "grayscale-50" : ""} ${product.onPromotion ? "promo-border" : ""}`}
                data-content={product.onPromotion ? t("promotion") : ""}
            >
                {/* Imagen principal */}
                <img
                    src={`http://localhost:8080/api/product${product.colors[0].images[0]}`}
                    alt={product.name}
                    loading="lazy"
                    decoding="async"
                    className="h-auto absolute object-contain transition-all duration-500 ease-in-out 
                     group-hover:opacity-0 group-hover:scale-110"
                />

                {/* Imagen hover */}
                {product.colors[0].images[1] && (
                    <img
                        src={`http://localhost:8080/api/product${product.colors[0].images[1]}`}
                        alt={`${product.name} hover`}
                        loading="lazy"
                        decoding="async"
                        className="h-auto absolute object-cover opacity-0 transition-all duration-500 
                       ease-in-out group-hover:opacity-100 group-hover:scale-110"
                    />
                )}

                {/* Overlay "Sin stock" */}
                {!hasStock && (
                    <div className="absolute inset-0 bg-gray-200 bg-opacity-70 flex items-center justify-center">
                        <div className="absolute top-10 right-0 ">
                            <div className="bg-black text-white text-xs font-bold px-2 py-1 transform -rotate-90 origin-center">
                                {t("productDetails.ProductOutOfStock")}
                            </div>
                        </div>
                    </div>
                )}

                {/* 🔹 Tallas disponibles en hover */}
                {hasStock && (
                    <div className="absolute bottom-0 left-0 w-full 
                  backdrop-blur-sm shadow-md
                  opacity-0 group-hover:opacity-100 
                  transition-all duration-300 ease-in-out 
                  p-3">
                        {product.section === "ACCESSORIES" ? (
                            <div className="flex flex-col items-center gap-1"></div>
                        ) : (
                            <div className="space-y-2 max-h-30 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300">
                                {product.colors.map((color, i) => {
                                    const sizes = Object.entries(color.sizes || {}).filter(([, qty]) => qty > 0);

                                    // Orden deseado
                                    const order = ["XS", "S", "M", "L", "XL", "XXL"];

                                    const sortedSizes = sizes.sort(
                                        ([a], [b]) => order.indexOf(a) - order.indexOf(b)
                                    );

                                    return (
                                        <div key={i} className="flex flex-col items-center">
                                            <div className="flex flex-wrap justify-center gap-1">
                                                {sortedSizes.length > 0 ? (
                                                    sortedSizes.map(([size]) => (
                                                        <span
                                                            key={size}
                                                            className={`w-16 h-8 flex items-center justify-center 
    text-sm inter-400 uppercase
    border border-gray-300 shadow-sm hover:opacity-80 transition rounded-sm`}
                                                            style={{ backgroundColor: color.hexCode, color: isDark(color.hexCode) ? "white" : "black" }}
                                                        >
                                                            {size}
                                                        </span>
                                                    ))
                                                ) : (
                                                    <span className="text-xs text-red-500">{t("NotAvailable")}</span>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Información del producto */}
            <div className="mt-3 text-left flex-1 ml-3">
                <p className="custom-font-shop-regular text-lg font-medium" style={{ color: textColor }}>
                    {product.name}
                </p>

                {product.onPromotion && product.discount > 0 ? (
                    <div className="flex items-center gap-2">
                        <span
                            className="custom-font-shop-regular line-through text-sm"
                            style={{ color: "#909497" }}
                        >
                            {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
                        </span>
                        <span className="custom-font-shop-regular font-semibold" style={{ color: "red" }}>
                            {product.price.toFixed(2)}€
                        </span>
                    </div>
                ) : (
                    <span
                        className="custom-font-shop-regular font-semibold text-base"
                        style={{ color: textColor }}
                    >
                        {product.price.toFixed(2)}€
                    </span>
                )}
            </div>

            {/* Colores disponibles */}
            <div className="flex gap-1 ml-3 mt-2">
                {product.colors?.map((color, i) => (
                    <span
                        key={i}
                        className="w-4 h-4 rounded-full border"
                        style={{ backgroundColor: color.hexCode }}
                    />
                ))}
            </div>
        </div>
    );
};

const ArtistView = () => {
    const location = useLocation();
    const artistId = location.state?.artistId;
    const [artist, setArtist] = useState(null);
    const [bgColor, setBgColor] = useState(null);
    const [events, setEvents] = useState([]);
    const { t } = useTranslation();

    useEffect(() => {
        if (!artistId || isNaN(artistId)) return;

        const fetchArtist = async () => {
            try {
                const res = await fetch(`http://localhost:8080/api/artists/${artistId}`);
                if (!res.ok) throw new Error('Error al obtener artista');
                const response = await res.json();
                setArtist(response.data);
            } catch (error) {
                console.error(error);
            }
        };

        const fetchFutureEvents = async () => {
            try {
                const res = await fetch(`http://localhost:8080/api/event/futureEvents/${artistId}`);

                if (res.status === 204) {
                    setEvents([]);
                    return;
                }

                if (!res.ok) throw new Error('Error al obtener los eventos');

                const response = await res.json();
                setEvents(response.data);
            } catch (error) {
                console.error(error);
            }
        };

        fetchArtist();
        fetchFutureEvents();
    }, [artistId]);

    useEffect(() => {
        if (artist?.primaryColor) {
            const timer = setTimeout(() => setBgColor(artist.primaryColor), 50);
            return () => clearTimeout(timer);
        } else {
            setBgColor('#ffffff');
        }
    }, [artist]);


    if (!artist) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 border-solid mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">{t('artistView.loadingArtist')}</p>
                </div>
            </div>
        );
    }




    return (
        <>
            <div
                className="artist-page min-h-screen p-4"
                style={{ backgroundColor: bgColor || '#ffffff', transition: 'background-color 0.8s ease' }}
            >
                <h1 className='text-left custom-font uppercase mt-20'>{t('artistView.products')}</h1>
                <hr className='mb-2' style={{ height: '2px', backgroundColor: 'white' }} />
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1 w-full">
                    {artist.artistProducts.map((product) => (
                        <Link to={`/product/details/${product.id}`} key={product.id}>
                            <div className="flex justify-center">
                                <ProductCard product={product} bgColor={bgColor} />
                            </div>
                        </Link>
                    ))}
                </div>

                {/* EVENTOS */}
                <h1 className='text-left custom-font uppercase mt-8'>{t('artistView.latestEvents')}</h1>
                <hr className='mb-2' style={{ height: '2px', backgroundColor: 'white' }} />

                {events.length > 0 ? (
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
                        {events.map((event, index) => (
                            <SwiperSlide key={index}>
                                <div className="group relative w-full h-[600px] md:h-[800px] overflow-hidden shadow-lg">
                                    {/* Imagen de fondo */}
                                    <img
                                        src={`http://localhost:8080/api/event${event.image}`}
                                        alt={artist.name}
                                        loading="lazy"
                                        decoding="async"
                                        className="w-full h-full object-cover transform transition duration-500 ease-in-out group-hover:scale-110"
                                    />

                                    {/* Overlay oscuro en hover */}
                                    <div className="absolute inset-0 bg-black/80 opacity-0 group-hover:opacity-80 transition-opacity duration-300"></div>

                                    {/* Información del evento */}
                                    <div className="inter-400 absolute inset-0 flex flex-col justify-center items-center px-6 text-center opacity-0 group-hover:opacity-100 transition-all duration-500">
                                        {/* Fondo translúcido para mejor legibilidad */}
                                        {/* Título del evento */}
                                        <h3 className="text-white text-3xl font-extrabold mb-3 tracking-wide">
                                            {event.title}
                                        </h3>

                                        {/* Fecha */}
                                        <p className="text-gray-300 flex items-center justify-center gap-2 text-lg">
                                            <i className="fa-solid fa-calendar-days"></i> {event.date}
                                        </p>

                                        {/* Lugar */}
                                        <p className="text-gray-300 flex items-center justify-center gap-2 text-lg mt-2">
                                            <i className="fa-solid fa-location-dot"></i> {event.location}
                                        </p>

                                        {/* Nombre del artista o evento */}
                                        <p className="text-white text-xl font-semibold mt-3">
                                            {event.name}
                                        </p>

                                        {/* Descripción */}
                                        {event.description && (
                                            <p className="text-gray-200 text-sm mt-4 leading-relaxed">
                                                {event.description}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                ) : (
                    <p className="text-white mt-4">{t('artistView.noEventsAvailable')}</p>
                )}


            </div><Footer /></>
    );
};

export default ArtistView;
