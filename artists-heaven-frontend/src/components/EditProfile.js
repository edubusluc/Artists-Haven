import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const EditProfile = () => {
    const [profile, setProfile] = useState({
        firstName: '',
        lastName: '',
        artistName: '',
    });
    const navigate = useNavigate();
    const token = localStorage.getItem("authToken");

    useEffect(() => {
        fetch('/api/users/profile', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
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
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setProfile({ ...profile, [name]: value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const token = localStorage.getItem("authToken");
        fetch('/api/users/profile/edit', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            body: JSON.stringify(profile),
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al actualizar el perfil: ' + response.statusText);
                }
                return response.json();
            })
            .then(() => {
                navigate('/users/profile');
            })
            .catch(error => console.error('Error updating profile:', error));
    };

    if (!token) {
        return <><p>No tienes permiso para acceder a esta página</p><button onClick={() => navigate('/')} className="btn btn-primary">
            Volver al inicio
        </button></>;
    }

    return (
        <div>
            <h1>Editar Perfil</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Nombre:</label>
                    <input
                        type="text"
                        name="firstName"
                        value={profile.firstName}
                        onChange={handleInputChange}
                    />
                </div>
                <div>
                    <label>Apellido:</label>
                    <input
                        type="text"
                        name="lastName"
                        value={profile.lastName}
                        onChange={handleInputChange}
                    />
                </div>
                {profile.artistName !== null && (
                    <div>
                        <label>Nombre artístico:</label>
                        <input
                            type="text"
                            name="artistName"
                            value={profile.artistName}
                            onChange={handleInputChange}
                        />
                    </div>
                )}
                <button type="submit" className="btn btn-success">Guardar cambios</button>
            </form>
        </div>
    );
};

export default EditProfile;
