import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { checkTokenExpiration } from '../utils/authUtils';

const Profile = () => {
    const [profile, setProfile] = useState(null);
    const navigate = useNavigate();
    useEffect(() => {
        if (checkTokenExpiration()) { // Verifica si el token es válido
            const token = localStorage.getItem("authToken");
            // Realizar la solicitud GET a la API de Spring Boot para obtener el perfil
            fetch('/api/users/profile', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,  // Pasar el token en el header
                },
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error al obtener el perfil: ' + response.statusText);
                    }
                    return response.json();
                })
                .then(data => setProfile(data))
                .catch(error => console.error('Error fetching profile:', error));
        }
    }, [navigate]);

    if (!profile) {
        return <div>Cargando...</div>;
    }

    return (
        <div>
            <h1>Mi Perfil</h1>
            <p><strong>Nombre:</strong> {profile.firstName} {profile.lastName}</p>
            <p><strong>Correo electrónico:</strong> {profile.email}</p>
            <p><strong>Nombre Usuario:</strong> {profile.username}</p>

            {/* Si es un artista, mostrar información adicional */}
            {profile.artistName && (
                <div>
                    <h3>Información del Artista</h3>
                    <p><strong>Nombre artístico:</strong> {profile.artistName}</p>
                </div>
            )}

            {/* Botón para editar perfil */}
            <button onClick={() => navigate('/profile/edit')} className="btn btn-primary">
                Editar Perfil
            </button>

            <button onClick={() => navigate('/')} className="btn btn-primary">
                Volver al inicio
            </button>
        </div>
    );
};

export default Profile;
