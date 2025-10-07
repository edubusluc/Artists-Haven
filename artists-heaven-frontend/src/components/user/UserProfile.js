import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const UserProfile = () => {
    const [profile, setProfile] = useState(null);
    const [formData, setFormData] = useState({});
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [formErrors, setFormErrors] = useState({});
    const [images, setImages] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState(null);
    const [validationErrors, setValidationErrors] = useState({});

    const { t, i18n } = useTranslation();
    const currentLang = i18n.language;

    useEffect(() => {
        setFormErrors({})
    }, [currentLang]);

    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("authToken");
        fetch('http://localhost:8080/api/users/profile', {
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

            .then(response => {
                setProfile(response.data);
                setFormData(response.data);
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
            setErrorMessage(t('userProfile.imageFormatError'));
            setImages([]);
            return;
        }
        setErrorMessage("");
        setImages(files);
    };

    const validateForm = () => {
        const errors = {};

        if (!formData.firstName || formData.firstName.trim() === '') {
            errors.firstName = t('form.error.requiredFirstName');
        }
        if (!formData.lastName || formData.lastName.trim() === '') {
            errors.lastName = t('form.error.requiredLastName');
        }
        if (!formData.username || formData.username.trim() === '') {
            errors.username = t('userForm.error.requiredUsername')
        }
        if (!formData.email || !/\S+@\S+\.\S+/.test(formData.email)) {
            errors.email = t('form.email_required_valid');
        }
        if (!formData.phone || !/^\d{9,15}$/.test(formData.phone)) {
            errors.phone = t('form.phone_required_numeric');
        }
        if (!formData.city || formData.city.trim() === '') {
            errors.city = t('userForm.error.requiredCity');
        }
        if (!formData.address || formData.address.trim() === '') {
            errors.address = t('userForm.error.requiredAddress');
        }
        if (!formData.postalCode || formData.postalCode.trim() === '') {
            errors.postalCode = t('userForm.error.requiredPostalCode');
        }
        if (!formData.country || formData.country.trim() === '') {
            errors.country = t('userForm.error.requiredCountry');
        }

        if (formData.artistName) {
            if (!formData.artistName || formData.artistName.trim() === '') {
                errors.artistName = t('artistForm.error.requiredArtistName');
            }
            if (images.length === 0 && !formData.image) {
                errors.image = t('artistForm.error.requiredImage');
            }

            const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];

            const invalidMainImages = images.filter(file => !allowedTypes.includes(file.type));

            if (invalidMainImages.length > 0) {
                setValidationErrors(t('artistForm.error.invalidImage'));
                return;
            }

        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSave = async (e) => {
        setSaveError(null);
        if (!validateForm()) return;

        setSaving(true);

        const data = new FormData();
        Object.keys(formData).forEach(key => {
            if (key !== 'image') {
                data.append(key, formData[key] ?? '');
            }
        });

        if (images.length > 0) data.append("image", images[0]);

        const token = localStorage.getItem("authToken");

        try {
            setFormErrors({});

            const response = await fetch(`http://localhost:8080/api/users/profile/edit?lang=${currentLang}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
                body: data,
            });

            const result = await response.json();

            if (!response.ok) {
                if (result.errors) {
                    setFormErrors(result.errors);
                }
                throw new Error(result.message || 'Error al guardar el perfil');
            }

            setProfile(data);
            setFormData(data);
            setIsEditing(false);
            setImages([]);
            window.location.reload();
        } catch (error) {
            setSaveError(error.message);
        } finally {
            setSaving(false);
        }
    };


    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="text-lg text-gray-600">{t('userProfile.loadProfile')}</div>
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
            <h1 className="text-3xl font-bold mb-8 text-gray-800 text-left">{t('userProfile.myProfile')}</h1>

            <section className="mb-8">
                <h2 className="text-xl font-semibold text-gray-700 mb-4">{t('userProfile.personalData')}</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {renderField(t('userProfile.name'), formData.firstName, 'firstName')}
                    {renderField(t('userProfile.lastname'), formData.lastName, 'lastName')}
                    {renderField(t('userProfile.username'), formData.username, 'username')}
                </div>
            </section>

            <section className="mb-8">
                <h2 className="text-xl font-semibold text-gray-700 mb-4">{t('userProfile.contactData')}</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {renderField(t('userProfile.email'), formData.email, 'email', true)}
                    {renderField(t('userProfile.phone'), formData.phone, 'phone')}
                </div>
            </section>

            <section className="mb-8">
                <h2 className="text-xl font-semibold text-gray-700 mb-4">{t('userProfile.address')}</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {renderField(t('userProfile.city'), formData.city, 'city')}
                    {renderField(t('userProfile.address'), formData.address, 'address')}
                    {renderField(t('userProfile.postalCode'), formData.postalCode, 'postalCode')}
                    {renderField(t('userProfile.country'), formData.country, 'country')}
                </div>
            </section>

            {formData.artistName && (
                <>
                    <h2 className="text-xl font-semibold mb-6 text-gray-700">{t('userProfile.artistInformation')}</h2>

                    {isEditing ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="space-y-4">
                                <div>
                                    <label htmlFor="artistName" className="block text-sm font-medium text-gray-700 mb-1">{t('userProfile.artistName')}</label>
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
                                    <label htmlFor="color" className="block text-sm font-medium text-gray-700 mb-1">{t('userProfile.color')}</label>
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
                                    <label htmlFor="image" className="block text-sm font-medium text-gray-700 mb-1">{t('userProfile.profileImage')}</label>
                                    <input
                                        type="file"
                                        onChange={handleImageChange}
                                        id="image"
                                        className="block w-full text-sm text-gray-500"
                                        accept="image/*"
                                    />
                                    {errorMessage && <p className="text-red-600 text-sm mt-1">{errorMessage}</p>}

                                    {/* Si el usuario carga una nueva imagen, mostrar preview */}
                                    {images.length > 0 ? (
                                        <img
                                            src={URL.createObjectURL(images[0])}
                                            alt="Preview de perfil"
                                            className="mt-2 w-48 h-48 object-cover rounded-lg border"
                                        />
                                    ) : (
                                        // Si no hay imagen nueva, mostrar la actual
                                        formData.image && (
                                            <img
                                                src={`http://localhost:8080/api/artists/${formData.image}`}
                                                alt="Imagen actual de perfil"
                                                className="mt-2 w-48 h-48 object-cover rounded-lg border"
                                            />
                                        )
                                    )}
                                </div>

                            </div>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <div className="space-y-6">
                                <div>
                                    <p className="text-gray-600 font-semibold mb-1">{t('userProfile.artistName')}</p>
                                    <p className="text-gray-800">{formData.artistName}</p>
                                </div>
                                <div>
                                    <p className="text-gray-600 font-semibold mb-1">{t('userProfile.color')}</p>
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
                                    <p className="text-gray-600 font-semibold mb-1">{t('userProfile.profileImage')}</p>
                                    {formData.image ? (
                                        <img
                                            src={`http://localhost:8080/api/artists/${formData.image}`}
                                            alt="Imagen de perfil"
                                            className="w-48 h-48 object-cover rounded-lg border"
                                        />
                                    ) : (
                                        <p className="italic text-gray-400">{t('userProfile.noProfileImage')}</p>
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
                        {t('userProfile.editProfile')}
                    </button>
                ) : (
                    <>
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className={`bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700 transition disabled:opacity-50`}
                        >
                            {saving ? t('userProfile.saving') : t('userProfile.save')}
                        </button>
                        <button
                            onClick={() => {
                                setIsEditing(false);
                                setFormData(profile);
                                setImages([]);
                                setFormErrors({});
                                setErrorMessage('');
                                setSaveError(null);
                            }}
                            className="bg-red-600 text-white px-6 py-2 rounded hover:bg-red-700 transition"
                        >
                            {t('userProfile.cancel')}
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
