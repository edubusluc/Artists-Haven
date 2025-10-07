import React, { useEffect, useState, useRef, useMemo } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import { ZoomControl } from "react-leaflet";
import { useTranslation } from "react-i18next";

// Configuración de iconos de Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
    iconUrl: require("leaflet/dist/images/marker-icon.png"),
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

function FitBounds({ concerts }) {
    const map = useMap();
    const firstFitDone = useRef(false);

    useEffect(() => {
        if (concerts.length === 0 || firstFitDone.current) return;
        const bounds = L.latLngBounds(concerts.map((c) => [c.latitude, c.longitude]));
        map.fitBounds(bounds, { padding: [50, 50] });
        firstFitDone.current = true;
    }, [concerts, map]);

    return null;
}

function MapControls({ hidden }) {
    const map = useMap();
    const move = (x, y) => {
        map.panBy([x, y], { animate: true });
    };
    if (hidden) return null;
    return (
        <div className="sm:hidden absolute bottom-12 right-4 z-[999]">
            <div className="rounded-lg p-1 flex flex-col items-center">
                <button onClick={() => move(0, -150)} className="w-10 h-10 flex items-center justify-center rounded-md shadow bg-white/70">▲</button>
                <div className="flex space-x-10 my-1">
                    <button onClick={() => move(-150, 0)} className="w-10 h-10 flex items-center justify-center bg-white/70 rounded-md shadow">◀</button>
                    <button onClick={() => move(150, 0)} className="w-10 h-10 flex items-center justify-center bg-white/70 rounded-md shadow">▶</button>
                </div>
                <button onClick={() => move(0, 150)} className="w-10 h-10 flex items-center justify-center bg-white/70 rounded-md shadow">▼</button>
            </div>
        </div>
    );
}

function LocateButton({ userPosition }) {
    const map = useMap();
    const { t } = useTranslation();
    if (!userPosition) return null;

    const goToUser = () => map.flyTo(userPosition, 10, { duration: 1.5 });
    const goToWorld = () => map.flyTo(userPosition, 3, { duration: 1.5 });

    return (
        <div className="absolute bottom-4 left-4 z-[999] flex space-x-3">
            <button onClick={goToUser} className="bg-white/90 rounded-full shadow-md p-3 px-4 hover:bg-gray-200 transition" title={t('concertMap.goToMyLocation')} aria-label="Ir a mi ubicación">
                <span aria-hidden="true">📍</span>
            </button>
            <button onClick={goToWorld} className="bg-white/90 rounded-full shadow-md p-3 hover:bg-gray-200 transition" title={t('concertMap.centerMap')} aria-label="Centrar mapa">
                <span aria-hidden="true">🌎</span>
            </button>
        </div>
    );
}

function FlyToConcert({ position }) {
    const map = useMap();
    useEffect(() => {
        if (!position || !map || !map._loaded) return;
        map.flyTo(position, 10, { duration: 1.5 });
    }, [position, map]);
    return null;
}
function FlyToUser({ position }) {
    const map = useMap();
    useEffect(() => {
        if (!position || !map || !map._loaded) return;
        map.flyTo(position, 10, { duration: 1.5 });
    }, [position, map]);
    return null;
}
export default function ConcertMap() {
    const [concerts, setConcerts] = useState([]);
    const [showLegend, setShowLegend] = useState(false);
    const [showFilters, setShowFilters] = useState(false);
    const [targetPosition, setTargetPosition] = useState(null);
    const [userPosition, setUserPosition] = useState(null);
    const [filters, setFilters] = useState({ artist: "", distance: "", date: "" });
    const mapRef = useRef();
    const { t } = useTranslation();

    useEffect(() => {
        fetch("http://localhost:8080/api/event/allFutureEvents")
            .then((res) => res.json())
            .then((response) => setConcerts(response.data))
            .catch((err) => console.error(err));
    }, []);

    useEffect(() => {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                (pos) => setUserPosition([pos.coords.latitude, pos.coords.longitude]),
                (err) => console.error("Error obteniendo ubicación:", err),
                { enableHighAccuracy: true }
            );
        }
    }, []);

    // Filtro de conciertos
    const filteredConcerts = useMemo(() => {
        return concerts.filter((c) => {
            let pass = true;
            if (filters.artist && !c.artistName.toLowerCase().includes(filters.artist.toLowerCase())) pass = false;
            if (filters.date && new Date(c.date).toISOString().slice(0, 10) !== filters.date) pass = false;
            if (filters.distance && userPosition) {
                const distanceKm = L.latLng(userPosition).distanceTo(L.latLng(c.latitude, c.longitude)) / 1000;
                if (distanceKm > Number(filters.distance)) pass = false;
            }
            return pass;
        });
    }, [concerts, filters, userPosition]);

    const createColoredIcon = (color) => L.divIcon({
        className: "",
        html: `<svg xmlns="http://www.w3.org/2000/svg" width="30" height="40" viewBox="0 0 30 40">
                <path fill="${color}" stroke="black" stroke-width="1" d="M15 0C7 0 0 7 0 15c0 11.25 15 25 15 25s15-13.75 15-25C30 7 23 0 15 0z"/>
                <circle cx="15" cy="15" r="6" fill="white"/>
            </svg>`,
        iconSize: [30, 40],
        iconAnchor: [15, 40],
        popupAnchor: [0, -35],
    });

    const userIcon = L.divIcon({
        className: "",
        html: `<svg xmlns="http://www.w3.org/2000/svg" width="30" height="40" viewBox="0 0 30 40">
            <path fill="#007BFF" stroke="black" stroke-width="1" d="M15 0C7 0 0 7 0 15c0 11.25 15 25 15 25s15-13.75 15-25C30 7 23 0 15 0z"/>
            <circle cx="15" cy="15" r="6" fill="white"/>
        </svg>`,
        iconSize: [30, 40],
        iconAnchor: [15, 40],
        popupAnchor: [0, -35],
    });

    const goToConcert = (lat, lng) => {
        setTargetPosition([lat, lng]);
        setShowLegend(false);
    };

    return (
        <div className="relative">
            {/* Filtros desktop */}
            <div className="hidden sm:block absolute top-3 left-3 z-[1000] bg-white/90 p-3 rounded-lg shadow-md w-80">
                <h4 className="font-semibold text-sm mb-2">{t('concertMap.filters')}</h4>
                <div className="space-y-2 text-sm">
                    <input type="text" placeholder={t('concertMap.filterArtist')} className="w-full p-2 border rounded-md" value={filters.artist} onChange={(e) => setFilters({ ...filters, artist: e.target.value })} />
                    <input type="date" className="w-full p-2 border rounded-md" value={filters.date} onChange={(e) => setFilters({ ...filters, date: e.target.value })} />
                    <select className="w-full p-2 border rounded-md" value={filters.distance} onChange={(e) => setFilters({ ...filters, distance: e.target.value })}>
                        <option value="">{t('concertMap.anyDistance')}</option>
                        <option value="10">10 km</option>
                        <option value="50">50 km</option>
                        <option value="100">100 km</option>
                        <option value="500">500 km</option>
                    </select>
                </div>
            </div>

            {/* Filtros móvil */}
            <div className="sm:hidden absolute top-3 left-1/2 -translate-x-1/2 w-[90%] z-[1000]">
                <button className="w-full bg-white/90 rounded-t-lg p-2 shadow-md text-sm font-semibold" onClick={() => setShowFilters((prev) => !prev)}>
                    {showFilters ? t('concertMap.hideFilters') : t('concertMap.showFilters')}
                </button>
                {showFilters && (
                    <div className="bg-white/90 rounded-b-lg shadow-md p-3 space-y-2 text-sm">
                        <input type="text" placeholder={t('concertMap.filterArtist')} className="w-full p-2 border rounded-md" value={filters.artist} onChange={(e) => setFilters({ ...filters, artist: e.target.value })} />
                        <input type="date" className="w-full p-2 border rounded-md" value={filters.date} onChange={(e) => setFilters({ ...filters, date: e.target.value })} />
                        <select className="w-full p-2 border rounded-md" value={filters.distance} onChange={(e) => setFilters({ ...filters, distance: e.target.value })}>
                            <option value="">{t('concertMap.anyDistance')}</option>
                            <option value="10">10 km</option>
                            <option value="50">50 km</option>
                            <option value="100">100 km</option>
                            <option value="500">500 km</option>
                        </select>
                    </div>
                )}
            </div>

            {/* Leyenda desktop */}
            <div className="hidden sm:flex flex-col absolute top-3 right-3 bg-white/90 p-3 rounded-lg z-[999] w-64 shadow-md">
                <h4 className="inter-400 text-md mb-2">{t('concertMap.event')}</h4>
                <div className="max-h-[300px] overflow-y-auto">
                    {filteredConcerts.length === 0 ? (
                        <p className="italic text-gray-600">{t('concertMap.noEvents')}</p>
                    ) : (
                        filteredConcerts.map((c) => (
                            <div key={c.id} className="border-b border-gray-200 py-1 cursor-pointer hover:bg-gray-100" onClick={() => goToConcert(c.latitude, c.longitude)}>
                                <strong className="inter-400 text-sm">{c.artistName}</strong>
                                <div className="inter-400 text-sm">📍 {c.location}</div>
                                <div className="inter-400 text-sm text-gray-500">📅 {new Date(c.date).toLocaleDateString()}</div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            {/* Leyenda móvil */}
            <div className="sm:hidden absolute bottom-3 left-1/2 -translate-x-1/2 w-[90%] z-[999]">
                <button className="w-full bg-white/90 rounded-t-lg p-2 shadow-md text-sm font-semibold" onClick={() => setShowLegend((prev) => !prev)}>
                    {showLegend ? t('concertMap.hideEvents') : t('concertMap.showEvents')}
                </button>
                {showLegend && (
                    <div className="bg-white/90 rounded-b-lg shadow-md p-3 max-h-[150px] overflow-y-auto">
                        {filteredConcerts.length === 0 ? (
                            <p className="italic text-gray-600">{t('concertMap.noEvents')}</p>
                        ) : (
                            filteredConcerts.map((c) => (
                                <div key={c.id} className="border-b border-gray-200 py-1 cursor-pointer hover:bg-gray-100" onClick={() => goToConcert(c.latitude, c.longitude)}>
                                    <strong className="inter-400 text-sm">{c.artistName}</strong>
                                    <div className="inter-400 text-sm">📍 {c.location}</div>
                                    <div className="inter-400 text-sm text-gray-500">📅 {new Date(c.date).toLocaleDateString()}</div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </div>

            {/* Mapa */}
            <MapContainer
                center={[40.4168, -3.7038]}
                zoom={2}
                scrollWheelZoom={false}
                style={{ height: "600px", width: "100%" }}
                whenCreated={(mapInstance) => (mapRef.current = mapInstance)}
                zoomControl={false} // desactiva el control por defecto
            >
                <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                <FitBounds concerts={filteredConcerts} />
                <FlyToConcert position={targetPosition} />
                {userPosition && <FlyToUser position={userPosition} />}
                <MapControls hidden={showLegend} />
                <LocateButton userPosition={userPosition} />

                {/* Agrega el control de zoom en otra esquina */}
                <ZoomControl position="bottomright" />

                {filteredConcerts.map((concert) => {
                    const positions = [[concert.latitude, concert.longitude], [concert.latitude, concert.longitude + 360], [concert.latitude, concert.longitude - 360]];
                    return positions.map((pos, i) => (
                        <Marker key={`${concert.id}-${i}`} position={pos} icon={createColoredIcon(concert.color)}>
                            <Popup>
                                <div className="max-w-[250px] text-xs inter-400 leading-snug space-y-1 break-words">
                                    <div>{t('concertMap.artist')}: {concert.artistName}</div>
                                    <div>{concert.description}</div>
                                    <div>📍 <span>{concert.location}</span></div>
                                    <div>📅 <span>{new Date(concert.date).toLocaleDateString()}</span></div>
                                </div>
                            </Popup>
                        </Marker>
                    ));
                })}

                {userPosition && (
                    <Marker position={userPosition} icon={userIcon}>
                        <Popup>{t('concertMap.here')}</Popup>
                    </Marker>
                )}
            </MapContainer>
        </div>
    );
}
