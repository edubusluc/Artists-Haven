import { useEffect, useState, useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import Footer from '../Footer';
import { useTranslation } from 'react-i18next';
import SlidingPanel from '../../utils/SlidingPanel';
const ArtistiCard = ({ artist }) => {
    const [loaded, setLoaded] = useState(false);

    return (
        <div className="group relative w-full h-[400px] sm:h-[500px] md:h-[650px] lg:h-[750px] xl:h-[850px] overflow-hidden shadow-lg">
            <img
                src={`/api/artists/${artist.mainPhoto}`}
                alt={artist.name}
                loading="lazy"
                onLoad={() => setLoaded(true)}
                className={`
                    w-full h-full object-cover
                    transform group-hover:scale-110
                    transition-all duration-700 ease-in-out
                    ${loaded ? "opacity-100" : "opacity-0"}
                `}
            />
            <div className="absolute bottom-0 right-0 flex items-end justify-end h-full w-full pr-3 pb-3">
                <p
                    className="custom-font-shop text-white font-bold"
                    style={{
                        writingMode: 'vertical-rl',
                        transform: 'rotate(180deg)',
                        fontSize: 'clamp(36px, 8vw, 90px)',
                        lineHeight: 1,
                        textAlign: 'right',
                        whiteSpace: 'nowrap',
                        textShadow: '2px 2px 6px rgba(0, 0, 0, 0.4)',
                    }}
                >
                    {artist.name}
                </p>
            </div>
        </div>
    );
};

const ArtistList = () => {
    const [artists, setArtists] = useState([]);
    const [isFilterOpen, setIsFilterOpen] = useState(false);
    const [orderBy, setOrderBy] = useState('default'); // default, az, za
    const [searchTerm, setSearchTerm] = useState('');
    const { t } = useTranslation();

    const fetchData = useCallback(async (url) => {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`Error al obtener datos de ${url}`);
        return await res.json();
    }, []);

    const fetchMainArtists = useCallback(async () => {
        try {
            const response = await fetchData("/api/artists/main");
            setArtists(response.data);
        } catch (error) {
            console.error(error.message);
        }
    }, [fetchData]);

    useEffect(() => {
        fetchMainArtists();
    }, [fetchMainArtists]);

    // Filtrar y ordenar artistas
    const filteredArtists = useMemo(() => {
        let filtered = [...artists];

        // Buscar por nombre
        if (searchTerm) {
            filtered = filtered.filter(artist =>
                artist.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Ordenar alfabéticamente
        if (orderBy === 'az') {
            filtered.sort((a, b) => a.name.localeCompare(b.name));
        } else if (orderBy === 'za') {
            filtered.sort((a, b) => b.name.localeCompare(a.name));
        }

        return filtered;
    }, [artists, searchTerm, orderBy]);

    return (
        <>
            <div className="flex justify-between mt-20 p-4">
                <p className="custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    {filteredArtists.length} {t('artistList.artists')}
                </p>
                <p
                    className="custom-font-shop-regular mb-4 cursor-pointer"
                    style={{ color: "black" }}
                    onClick={() => setIsFilterOpen(true)}
                >
                    {t('productsList.filterAndSearch')}
                </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 w-full transition-opacity duration-500 px-2 sm:px-4 mb-1">
                {filteredArtists.map((artist) => (
                    <Link key={artist.id} to={`/artist/${artist.name}`} state={{ artistId: artist.id }}>
                        <ArtistiCard artist={artist} />
                    </Link>
                ))}
            </div>

            <SlidingPanel
                isOpen={isFilterOpen}
                position="right"
                title={t('Filters')}
                onClose={() => setIsFilterOpen(false)}
                maxWidth="400px"
            >
                <div className="p-4 custom-font-shop-regular " style={{ color: "black" }}>
                    <label htmlFor="searchName" className="block mb-2 font-semibold">{t('artistList.searchByName')}</label>
                    <input
                        id="searchName"
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        placeholder={t('artistList.searchPlaceholder')}
                        className="w-full border border-gray-300 rounded p-2"
                    />
                </div>

                <div className="p-4 custom-font-shop-regular mb-4" style={{ color: "black" }}>
                    <label htmlFor="orderBy" className="block mb-2 font-semibold">{t('artistList.sort')}</label>
                    <select
                        id="orderBy"
                        value={orderBy}
                        onChange={(e) => setOrderBy(e.target.value)}
                        className="w-full border border-gray-300 rounded p-2"
                    >
                        <option value="default">{t('productsList.default')}</option>
                        <option value="az">{t('productsList.sortedAZ')}</option>
                        <option value="za">{t('productsList.sortedZA')}</option>
                    </select>
                </div>
            </SlidingPanel>

            <Footer />
        </>
    );
};

export default ArtistList;
