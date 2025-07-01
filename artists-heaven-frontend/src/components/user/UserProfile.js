import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { checkTokenExpiration } from '../../utils/authUtils';

const UserProfile = () => {
    const [profile, setProfile] = useState(null);
    const [formData, setFormData] = useState({});
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [formErrors, setFormErrors] = useState({});
    const [images, setImages] = useState([]);
    const [bannerImage, setBannerImage] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState(null);

    const navigate = useNavigate();

    useEffect(() => {
        if (!checkTokenExpiration()) {
            // Limpiar token por seguridad
            localStorage.removeItem("authToken");
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

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);
        const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];
        const invalidFiles = files.filter(file => !allowedTypes.includes(file.type));
        if (invalidFiles.length > 0) {
            setErrorMessage("Solo se permiten archivos de imagen válidos (jpg, png, gif, bmp, webp).");
            setImages([]);
            return;
        }
        setErrorMessage("");
        setImages(files);
    };

    const handleBannerImage = (e) => {
        const files = Array.from(e.target.files);
        const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];
        const invalidFiles = files.filter(file => !allowedTypes.includes(file.type));
        if (invalidFiles.length > 0) {
            setErrorMessage("Solo se permiten archivos de imagen válidos (jpg, png, gif, bmp, webp).");
            setBannerImage([]);
            return;
        }
        setErrorMessage("");
        setBannerImage(files);
    };

    const validateForm = () => {
        const errors = {};
        if (!formData.email || !/\S+@\S+\.\S+/.test(formData.email)) {
            errors.email = "Correo electrónico no válido";
        }
        if (!formData.phone || !/^\d{7,15}$/.test(formData.phone)) {
            errors.phone = "El teléfono debe contener solo números (7-15 dígitos)";
        }
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSave = () => {
        setSaveError(null);
        if (!validateForm()) return;

        setSaving(true);

        const data = new FormData();
        Object.keys(formData).forEach(key => {
            if (key !== 'image' && key !== 'bannerImage') {
                data.append(key, formData[key] ?? '');
            }
        });

        if (images.length > 0) {
            data.append("image", images[0]);
        }
        if (bannerImage.length > 0) {
            data.append("bannerImage", bannerImage[0]);
        }

        const token = localStorage.getItem("authToken");
        fetch('/api/users/profile/edit', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
            body: data,
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al actualizar el perfil: ' + response.statusText);
                }
                return response.json();
            })
            .then(updatedProfile => {
                setProfile(updatedProfile);
                setFormData(updatedProfile);
                setIsEditing(false);
                setImages([]);
                setBannerImage([]);
                window.location.reload();
            })
            .catch(error => {
                console.error('Error updating profile:', error);
                setSaveError("Error al guardar el perfil. Intenta nuevamente.");
            })
            .finally(() => {
                setSaving(false);
            });
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
                <>
                    <h2 className="text-xl font-semibold mb-6 text-gray-700">Información del artista</h2>

                    {isEditing ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="space-y-4">
                                <div>
                                    <label htmlFor="artistName" className="block text-sm font-medium text-gray-700 mb-1">Nombre artístico</label>
                                    <input
                                        type="text"
                                        id="artistName"
                                        name="artistName"
                                        value={formData.artistName || ''}
                                        onChange={handleChange}
                                        className="border rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                    />
                                </div>

                                <div>
                                    <label htmlFor="color" className="block text-sm font-medium text-gray-700 mb-1">Color</label>
                                    <input
                                        type="color"
                                        id="color"
                                        name="color"
                                        value={formData.color || '#000000'}
                                        onChange={handleChange}
                                        className="w-full h-10 px-4 py-2 border border-gray-300 rounded-lg shadow-sm cursor-pointer focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                    />
                                </div>
                            </div>

                            <div className="space-y-6">
                                <div>
                                    <label htmlFor="image" className="block text-sm font-medium text-gray-700 mb-1">Imagen de perfil</label>
                                    <input
                                        type="file"
                                        onChange={handleImageChange}
                                        id="image"
                                        className="block w-full text-sm text-gray-500"
                                        accept="image/*"
                                    />
                                    {errorMessage && <p className="text-red-600 text-sm mt-1">{errorMessage}</p>}
                                    {images.length > 0 && (
                                        <img
                                            src={URL.createObjectURL(images[0])}
                                            alt="Preview de perfil"
                                            className="mt-2 w-48 h-48 object-cover rounded-lg border"
                                        />
                                    )}
                                </div>

                                <div>
                                    <label htmlFor="bannerImage" className="block text-sm font-medium text-gray-700 mb-1">Imagen de banner</label>
                                    <input
                                        type="file"
                                        onChange={handleBannerImage}
                                        id="bannerImage"
                                        className="block w-full text-sm text-gray-500"
                                        accept="image/*"
                                    />
                                    {bannerImage.length > 0 && (
                                        <img
                                            src={URL.createObjectURL(bannerImage[0])}
                                            alt="Preview de banner"
                                            className="mt-2 w-full h-32 object-cover rounded-lg border"
                                        />
                                    )}
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <div className="space-y-6">
                                <div>
                                    <p className="text-gray-600 font-semibold mb-1">Nombre artístico</p>
                                    <p className="text-gray-800">{formData.artistName}</p>
                                </div>
                                <div>
                                    <p className="text-gray-600 font-semibold mb-1">Color</p>
                                    <div
                                        style={{
                                            backgroundColor: formData.color || '#000',
                                            width: '50px',
                                            height: '50px',
                                            borderRadius: '8px',
                                            border: '1px solid #ccc',
                                        }}
                                    />
                                </div>
                            </div>

                            <div className="space-y-6">
                                <div>
                                    <p className="text-gray-600 font-semibold mb-1">Imagen de perfil</p>
                                    {formData.image ? (
                                        <img
                                            src={`/api/artists/${formData.image}`}
                                            alt="Imagen de perfil"
                                            className="w-48 h-48 object-cover rounded-lg border"
                                        />
                                    ) : (
                                        <p className="italic text-gray-400">No hay imagen de perfil</p>
                                    )}
                                </div>

                                <div>
                                    <p className="text-gray-600 font-semibold mb-1">Imagen de banner</p>
                                    {formData.bannerImage ? (
                                        <img
                                            src={`/api/artists/${formData.bannerImage}`}
                                            alt="Imagen de banner"
                                            className="w-full h-32 object-cover rounded-lg border"
                                        />
                                    ) : (
                                        <p className="italic text-gray-400">No hay imagen de banner</p>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </>
            )}


            <div className="mt-8 flex justify-between items-center">
                {!isEditing ? (
                    <button
                        onClick={() => setIsEditing(true)}
                        className="bg-indigo-600 text-white px-6 py-2 rounded hover:bg-indigo-700 transition"
                    >
                        Editar perfil
                    </button>
                ) : (
                    <>
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className={`bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700 transition disabled:opacity-50`}
                        >
                            {saving ? 'Guardando...' : 'Guardar cambios'}
                        </button>
                        <button
                            onClick={() => {
                                setIsEditing(false);
                                setFormData(profile);
                                setImages([]);
                                setBannerImage([]);
                                setFormErrors({});
                                setErrorMessage('');
                                setSaveError(null);
                            }}
                            className="bg-red-600 text-white px-6 py-2 rounded hover:bg-red-700 transition"
                        >
                            Cancelar
                        </button>
                    </>
                )}
            </div>

            {saveError && (
                <div className="mt-4 text-red-600 font-semibold">{saveError}</div>
            )}
        </div>
    );
};

export default UserProfile;
