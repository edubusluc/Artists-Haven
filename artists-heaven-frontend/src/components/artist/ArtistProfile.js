import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Edit3, Save, X } from "lucide-react"; // íconos modernos

const ArtistProfile = () => {
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

            const response = await fetch(`/api/users/profile/edit?lang=${currentLang}`, {
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

    const renderField = (label, value, name, readOnly = false) => (
        <div className="flex flex-col">
            <label className="text-gray-600 font-medium mb-1">{label}</label>
            {isEditing && !readOnly ? (
                <>
                    <input
                        type="text"
                        name={name}
                        value={formData[name] || ''}
                        onChange={handleChange}
                        className={`border rounded-lg px-3 py-2 w-full shadow-sm focus:ring-2 focus:ring-indigo-500 ${formErrors[name] ? 'border-red-500' : ''}`}
                    />
                    {formErrors[name] && <p className="text-red-500 text-xs mt-1">{formErrors[name]}</p>}
                </>
            ) : (
                <p className="px-3 py-2 border rounded-lg bg-gray-50 text-gray-700 shadow-sm">
                    {value || '—'}
                </p>
            )}
        </div>
    );

    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="animate-pulse text-lg text-gray-600">{t('userProfile.loadProfile')}</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-red-600 text-center mt-10 font-semibold">{error}</div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto p-6 space-y-10">
            {/* Datos personales */}
            <div className="bg-white shadow-lg rounded-2xl p-6">
                <div>
                    <h2 className="text-2xl font-semibold text-gray-800 border-b pb-2 mb-4">{t('userProfile.personalData')}</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        {renderField(t('userProfile.name'), formData.firstName, 'firstName')}
                        {renderField(t('userProfile.lastname'), formData.lastName, 'lastName')}
                        {renderField(t('userProfile.username'), formData.username, 'username')}
                    </div>
                </div>


                {/* Contacto */}
                <div className="mt-4">
                    <h2 className="text-2xl font-semibold text-gray-800 border-b pb-2 mb-4">{t('userProfile.contactData')}</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        {renderField(t('userProfile.email'), formData.email, 'email', true)}
                        {renderField(t('userProfile.phone'), formData.phone, 'phone')}
                    </div>
                </div>

                {/* Dirección */}
                <div className="mt-4">
                    <h2 className="text-2xl font-semibold text-gray-800 border-b pb-2 mb-4">{t('userProfile.address')}</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        {renderField(t('userProfile.city'), formData.city, 'city')}
                        {renderField(t('userProfile.address'), formData.address, 'address')}
                        {renderField(t('userProfile.postalCode'), formData.postalCode, 'postalCode')}
                        {renderField(t('userProfile.country'), formData.country, 'country')}
                    </div>
                </div>

                {/* Información de artista */}
                {formData.artistName && (
                    <div className="mt-4">
                        <h2 className="text-2xl font-semibold text-gray-800 border-b pb-2 mb-6">{t('userProfile.artistInformation')}</h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <div>
                                <p className="text-gray-600 font-medium mb-2">{t('userProfile.artistName')}</p>
                                <p className="text-lg font-semibold text-gray-800">{formData.artistName}</p>
                            </div>
                            <div>
                                <p className="text-gray-600 font-medium mb-2">{t('userProfile.color')}</p>
                                <div
                                    className="w-12 h-12 rounded-lg border shadow"
                                    style={{ backgroundColor: formData.color || '#000' }}
                                />
                            </div>
                            <div>
                                <p className="text-gray-600 font-medium mb-2">{t('userProfile.profileImage')}</p>
                                {formData.image ? (
                                    <img src={`/api/artists/${formData.image}`} alt="Perfil"
                                        className="w-48 h-48 object-cover rounded-xl border shadow" />
                                ) : (
                                    <p className="italic text-gray-400">{t('userProfile.noProfileImage')}</p>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {/* Botones */}
            <div className="flex justify-end gap-4">
                {!isEditing ? (
                    <button
                        onClick={() => setIsEditing(true)}
                        className="flex items-center gap-2 bg-indigo-600 text-white px-5 py-2 rounded-xl shadow hover:bg-indigo-700 transition"
                    >
                        <Edit3 size={18} /> {t('userProfile.editProfile')}
                    </button>
                ) : (
                    <>
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className="flex items-center gap-2 bg-green-600 text-white px-5 py-2 rounded-xl shadow hover:bg-green-700 transition disabled:opacity-50"
                        >
                            <Save size={18} /> {saving ? t('userProfile.saving') : t('userProfile.save')}
                        </button>
                        <button
                            onClick={() => { setIsEditing(false); setFormData(profile); }}
                            className="flex items-center gap-2 bg-red-600 text-white px-5 py-2 rounded-xl shadow hover:bg-red-700 transition"
                        >
                            <X size={18} /> {t('userProfile.cancel')}
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

export default ArtistProfile;
