import React, { useEffect, useState } from "react";
import { useNavigate } from 'react-router-dom';


const rol = localStorage.getItem("role");

const fetchVideoBlob = async (videoUrl, authToken) => {
    try {
        if (rol !== "ADMIN") {
            // Si el rol no es ADMIN, no hacemos la petición
            return;
        }
        const response = await fetch(`/api/admin${videoUrl}`, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch video: ${response.status}`);
        }

        const videoBlob = await response.blob();
        return URL.createObjectURL(videoBlob);
    } catch (error) {
        console.error("Error fetching video blob:", error);
        return null;
    }
};

// Función para hacer la verificación de un artista
const VerificationList = () => {
    const [verifications, setVerifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));
    const [users, setUsers] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        if (rol !== "ADMIN") {
            // Si el rol no es ADMIN, no hacemos la petición
            return;
        }
        const fetchVerifications = async () => {
            try {
                const response = await fetch("/api/admin/verification/pending", {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        'Authorization': `Bearer ${authToken}`
                    },
                });
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                const data = await response.json();
                setVerifications(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchVerifications();
    }, [authToken]);

    const verifyArtist = (id, verificationId) => {
        if (rol !== "ADMIN") {
            // Si el rol no es ADMIN, no hacemos la petición
            return;
        }
        fetch('/api/admin/validate_artist', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                id,
                verificationId
            }),
        })
            .then(response => {
                if (response.ok) {
                    setUsers(users.map(user =>
                        user.id === id ? { ...user, role: "ARTIST" } : user
                    ));
                    console.log("Artitsa verificado")
                    window.location.reload()
                } else {
                    console.log('Error al verificar al artista');
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    };

    if (loading) return <p>Loading...</p>;
    if (rol !== "ADMIN") return <><p>No tienes permiso para acceder a esta página</p><button onClick={() => navigate('/')} className="btn btn-primary">
        Volver al inicio
    </button></>;

    return (
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Artist</th>
                    <th>Video</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                {verifications.map((verification) => (
                    <tr key={verification.id}>
                        <td>{verification.id}</td>

                        <td>{verification.artist?.artistName || "Unknown"}</td>
                        <td>
                            <VideoLink videoUrl={verification.videoUrl} authToken={authToken} />
                        </td>
                        <td>{new Date(verification.date).toLocaleString()}</td>
                        <td>{verification.status}</td>
                        <td>
                            {!verification.artist.isVerificated ? (
                                <button onClick={() => verifyArtist(verification.artist.id, verification.id)}>Verificar Artista</button>
                            ) : (
                                <span>El artista ya está verificado</span>
                            )}
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
};

const VideoLink = ({ videoUrl, authToken }) => {
    const [videoSrc, setVideoSrc] = useState(null);

    useEffect(() => {
        const fetchAndSetVideo = async () => {
            const videoBlobUrl = await fetchVideoBlob(videoUrl, authToken);
            setVideoSrc(videoBlobUrl);
        };

        fetchAndSetVideo();
    }, [videoUrl, authToken]);

    return (
        <div>
            {videoSrc ? (
                <a href={videoSrc} target="_blank" rel="noopener noreferrer">
                    Open Video
                </a>
            ) : (
                <p>Loading video...</p>
            )}
        </div>
    );
};

export default VerificationList;
