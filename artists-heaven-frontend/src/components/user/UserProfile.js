import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { checkTokenExpiration } from '../../utils/authUtils';

const UserProfile = () => {
    const [profile, setProfile] = useState(null);
    const [formData, setFormData] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const [formErrors, setFormErrors] = useState({});

    useEffect(() => {
        if (!checkTokenExpiration()) {
            navigate('/login');
            return;
        }

        const token = localStorage.getItem("authToken");
        fetch('/api/users/profile', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
        })
            .then(response => {
                if (!response.ok) throw new Error('Error al obtener el perfil');
                return response.json();
            })
            .then(data => {
                setProfile(data);
                setFormData(data);
                setLoading(false);
            })
            .catch(error => {
                console.error('Error fetching profile:', error);
                setError('No se pudo cargar el perfil');
                setLoading(false);
            });
    }, [navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const validateForm = () => {
        const errors = {};

        // Validar email
        if (!formData.email || !/\S+@\S+\.\S+/.test(formData.email)) {
            errors.email = "Correo electrónico no válido";
        }

        // Validar teléfono (solo dígitos y longitud entre 7 y 15)
        if (!formData.phone || !/^\d{7,15}$/.test(formData.phone)) {
            errors.phone = "El teléfono debe contener solo números (7-15 dígitos)";
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSave = () => {
        if (!validateForm()) return;

        const token = localStorage.getItem("authToken");
        fetch('/api/users/profile/edit', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            body: JSON.stringify(formData),
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al actualizar el perfil: ' + response.statusText);
                }
                return response.json();
            })
            .then(() => {
                setProfile(formData);
                setIsEditing(false);
            })
            .catch(error => console.error('Error updating profile:', error));
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="text-lg text-gray-600">Cargando perfil...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-red-600 text-center mt-10">{error}</div>
        );
    }

    const renderField = (label, value, name, readOnly = false) => (
        <div>
            <p className="text-gray-500 text-sm">{label}</p>
            {isEditing && !readOnly ? (
                <>
                    <input
                        type="text"
                        name={name}
                        value={formData[name] || ''}
                        onChange={handleChange}
                        className={`border rounded px-2 py-1 w-full ${formErrors[name] ? 'border-red-500' : ''}`}
                    />
                    {formErrors[name] && <p className="text-red-600 text-sm mt-1">{formErrors[name]}</p>}
                </>
            ) : (
                <input
                    type="text"
                    name={name}
                    value={value || ''}
                    readOnly
                    className="border rounded px-2 py-1 w-full bg-gray-100 text-gray-700"
                />
            )}
        </div>
    );

    return (
        <div className="max-w-4xl mx-auto">
            <h1 className="text-3xl font-bold mb-8 text-gray-800 text-left">Mi Perfil</h1>

            <section className="mb-8">
                <h2 className="text-xl font-semibold text-gray-700 mb-4">Datos personales</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {renderField('Nombre', formData.firstName, 'firstName')}
                    {renderField('Apellido', formData.lastName, 'lastName')}
                    {renderField('Nombre de usuario', formData.username, 'username')}
                </div>
            </section>

            <section className="mb-8">
                <h2 className="text-xl font-semibold text-gray-700 mb-4">Información de contacto</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {renderField('Correo electrónico', formData.email, 'email', true)}
                    {renderField('Teléfono', formData.phone, 'phone')}
                </div>
            </section>

            <section className="mb-8">
                <h2 className="text-xl font-semibold text-gray-700 mb-4">Dirección</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {renderField('Ciudad', formData.city, 'city')}
                    {renderField('Dirección', formData.address, 'address')}
                    {renderField('Código postal', formData.postalCode, 'postalCode')}
                    {renderField('País', formData.country, 'country')}
                </div>
            </section>

            {formData.artistName && (
                <section className="mb-8 bg-indigo-50 border-l-4 border-indigo-400 p-4 rounded">
                    <h2 className="text-lg font-semibold text-indigo-700 mb-1">Información del Artista</h2>
                    {isEditing ? (
                        <input
                            type="text"
                            name="artistName"
                            value={formData.artistName}
                            onChange={handleChange}
                            className="border rounded px-2 py-1 w-full"
                        />
                    ) : (
                        <p className="text-gray-800"><strong>Nombre artístico:</strong> {formData.artistName}</p>
                    )}
                </section>
            )}

            {/* Botones de acción */}
            <div className="flex gap-4 mt-6">
                {isEditing ? (
                    <>
                        <button
                            onClick={handleSave}
                            className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                        >
                            Guardar
                        </button>
                        <button
                            onClick={() => {
                                setFormData(profile);
                                setIsEditing(false);
                            }}
                            className="px-6 py-2 bg-gray-400 text-white rounded-lg hover:bg-gray-500 transition"
                        >
                            Cancelar
                        </button>
                    </>
                ) : (
                    <button
                        onClick={() => setIsEditing(true)}
                        className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                    >
                        Editar Perfil
                    </button>
                )}
            </div>
        </div>
    );
};

export default UserProfile;
