import React, { useEffect, useState, useRef } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import { useTranslation } from "react-i18next";

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
                <button
                    onClick={() => move(0, -150)}
                    className="w-10 h-10 flex items-center justify-center rounded-md shadow bg-white/70"
                >
                    ▲
                </button>
                <div className="flex space-x-10 my-1">
                    <button
                        onClick={() => move(-150, 0)}
                        className="w-10 h-10 flex items-center justify-center bg-white/70 rounded-md shadow"
                    >
                        ◀
                    </button>
                    <button
                        onClick={() => move(150, 0)}
                        className="w-10 h-10 flex items-center justify-center bg-white/70 rounded-md shadow"
                    >
                        ▶
                    </button>
                </div>
                <button
                    onClick={() => move(0, 150)}
                    className="w-10 h-10 flex items-center justify-center bg-white/70 rounded-md shadow"
                >
                    ▼
                </button>
            </div>
        </div>
    );
}

function LocateButton({ userPosition }) {
    const map = useMap();
    const { t } = useTranslation();

    if (!userPosition) return null;

    const goToUser = () => {
        map.flyTo(userPosition, 10, { duration: 1.5 });
    };

    const goToWorld = () => {
        map.flyTo(userPosition, 3, { duration: 1.5 });
    };



    return (
        <div className="absolute bottom-4 left-4 z-[999] flex space-x-3">
            <button
                onClick={goToUser}
                className="bg-white/90 rounded-full shadow-md p-3 px-4 hover:bg-gray-200 transition"
                title={t('concertMap.goToMyLocation')}
                aria-label="Ir a mi ubicación"
            >
                <span aria-hidden="true">📍</span>
            </button>

            <button
                onClick={goToWorld}
                className="bg-white/90 rounded-full shadow-md p-3 hover:bg-gray-200 transition"
                title={t('concertMap.centerMap')}
                aria-label="Centrar mapa"
            >
                <span aria-hidden="true">🌎</span>
            </button>
        </div>
    );

}

function FlyToConcert({ position }) {
    const map = useMap();

    useEffect(() => {
        if (position) {
            map.flyTo(position, 10, { duration: 1.5 });
        }
    }, [position, map]);

    return null;
}


function FlyToUser({ position }) {
    const map = useMap();

    useEffect(() => {
        if (position) {
            map.flyTo(position, 10, { duration: 1.5 });
        }
    }, [position, map]);

    return null;
}

export default function ConcertMap() {
    const [concerts, setConcerts] = useState([]);
    const [showLegend, setShowLegend] = useState(false);
    const [targetPosition, setTargetPosition] = useState(null);
    const [userPosition, setUserPosition] = useState(null);
    const mapRef = useRef();
    const { t } = useTranslation();

    useEffect(() => {
        fetch("/api/event/allFutureEvents")
            .then((res) => res.json())
            .then((response) => {
                setConcerts(response.data);
            })
            .catch((err) => console.error(err));
    }, []);

    useEffect(() => {
        return () => {
            if (mapRef.current) {
                mapRef.current.stop();
                mapRef.current.remove();
            }
        };
    }, []);



    useEffect(() => {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const { latitude, longitude } = pos.coords;
                    setUserPosition([latitude, longitude]);
                },
                (err) => console.error("Error obteniendo ubicación:", err),
                { enableHighAccuracy: true }
            );
        }
    }, []);

    const createColoredIcon = (color) => {
        return L.divIcon({
            className: "",
            html: `
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="40" viewBox="0 0 30 40">
                <path fill="${color}" stroke="black" stroke-width="1" d="M15 0C7 0 0 7 0 15c0 11.25 15 25 15 25s15-13.75 15-25C30 7 23 0 15 0z"/>
                <circle cx="15" cy="15" r="6" fill="white"/>
            </svg>
        `,
            iconSize: [30, 40],
            iconAnchor: [15, 40],
            popupAnchor: [0, -35],
        });
    };

    // ✅ Icono especial para el usuario
    const userIcon = L.divIcon({
        className: "",
        html: `
        <svg xmlns="http://www.w3.org/2000/svg" width="30" height="40" viewBox="0 0 30 40">
            <path fill="#007BFF" stroke="black" stroke-width="1" d="M15 0C7 0 0 7 0 15c0 11.25 15 25 15 25s15-13.75 15-25C30 7 23 0 15 0z"/>
            <circle cx="15" cy="15" r="6" fill="white"/>
        </svg>
    `,
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
            {/* Leyenda en desktop */}
            <div className="hidden sm:flex flex-col absolute top-3 right-3 bg-white/90 p-3 rounded-lg z-[999] w-64 shadow-md">
                <h4 className="inter-400 text-md mb-2">{t('concertMap.event')}</h4>
                <div className="max-h-[300px] overflow-y-auto">
                    {concerts.length === 0 ? (
                        <p className="italic text-gray-600">{t('concertMap.loading')}</p>
                    ) : (
                        concerts.map((c) => (
                            <div
                                key={c.id}
                                className="border-b border-gray-200 py-1 cursor-pointer hover:bg-gray-100"
                                onClick={() => goToConcert(c.latitude, c.longitude)}
                            >
                                <strong className="inter-400 text-sm">{c.artistName}</strong>
                                <div className="inter-400 text-sm">📍 {c.location}</div>
                                <div className="inter-400 text-sm text-gray-500">
                                    📅 {new Date(c.date).toLocaleDateString()}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            {/* Leyenda en móvil */}
            <div className="sm:hidden absolute bottom-3 left-1/2 -translate-x-1/2 w-[90%] z-[999]">
                <button
                    className="w-full bg-white/90 rounded-t-lg p-2 shadow-md text-sm font-semibold"
                    onClick={() => setShowLegend((prev) => !prev)}
                >
                    {showLegend ? t('concertMap.hideEvents') : t('concertMap.showEvents')}
                </button>
                {showLegend && (
                    <div className="bg-white/90 rounded-b-lg shadow-md p-3 max-h-[150px] overflow-y-auto">
                        {concerts.length === 0 ? (
                            <p className="italic text-gray-600">{t('concertMap.loading')}</p>
                        ) : (
                            concerts.map((c) => (
                                <div
                                    key={c.id}
                                    className="border-b border-gray-200 py-1 cursor-pointer hover:bg-gray-100"
                                    onClick={() => goToConcert(c.latitude, c.longitude)}
                                >
                                    <strong className="inter-400 text-sm">{c.artistName}</strong>
                                    <div className="inter-400 text-sm">📍 {c.location}</div>
                                    <div className="inter-400 text-sm text-gray-500">
                                        📅 {new Date(c.date).toLocaleDateString()}
                                    </div>
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
                zoomControl={true}
                dragging={true}
                tap={true}
                touchZoom={false}
                doubleClickZoom={false}
                worldCopyJump={true}
                style={{ height: "600px", width: "100%" }}
                whenCreated={(mapInstance) => (mapRef.current = mapInstance)}
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                <FitBounds concerts={concerts} />
                <FlyToConcert position={targetPosition} />
                {userPosition && <FlyToUser position={userPosition} />}
                <MapControls hidden={showLegend} />

                <LocateButton userPosition={userPosition} />


                {/* Marcadores de conciertos */}
                {concerts.map((concert) => {
                    const positions = [
                        [concert.latitude, concert.longitude],
                        [concert.latitude, concert.longitude + 360],
                        [concert.latitude, concert.longitude - 360],
                    ];
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

                {/* Marcador del usuario */}
                {userPosition && (
                    <Marker position={userPosition} icon={userIcon}>
                        <Popup>{t('concertMap.here')}</Popup>
                    </Marker>
                )}
            </MapContainer>
        </div>
    );
}
