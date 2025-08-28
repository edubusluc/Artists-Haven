import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NonAuthorise from "../NonAuthorise";
import { checkTokenExpiration } from "../../utils/authUtils";
import SessionExpired from "../SessionExpired";
import { useTranslation } from 'react-i18next';

const CreateEventForm = () => {
    const [event, setEvent] = useState({
        name: "",
        description: "",
        date: "",
        location: "",
        moreInfo: "",
    });

    const [images, setImages] = useState([]);
    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [validationError, setValidationError] = useState("");
    const [authToken] = useState(localStorage.getItem("authToken"));
    const role = localStorage.getItem("role");
    const token = localStorage.getItem("authToken");
    const [isVerified, setIsVerified] = useState(false);
    const navigate = useNavigate();
    const [validationErrors, setValidationErrors] = useState({});

    const { t, i18n } = useTranslation();
    const language = i18n.language;

    useEffect(() => {
        setValidationErrors({})
    }, [language]);

    const [loading, setLoading] = useState(true);


    useEffect(() => {
        const fetchVerificationStatus = async () => {
            try {
                const response = await fetch('/api/event/isVerified', {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                if (response.ok) {
                    const data = await response.json();
                    setIsVerified(data);
                } else {
                    setIsVerified(false);
                }
            } catch (error) {
                console.error("Error fetching verification status", error);
                setIsVerified(false);
            }
            finally {
                setLoading(false); // Terminó la carga
            }
        };
        fetchVerificationStatus();
    }, []);


    const handleChange = (e) => {
        const { name, value } = e.target;
        setEvent((prev) => ({ ...prev, [name]: value }));
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

    const handleSubmit = async (e) => {
        e.preventDefault();
        setValidationErrors({});
        let errors = {};

        if (!event.name) errors.name = t('eventForm.error.requiredName');
        if (!event.description) errors.description = t('eventForm.error.requiredDescription');
        if (!event.date) errors.date = t('eventForm.error.requiredDate');
        if (!event.location) errors.location = t('eventForm.error.requiredLocation');
        if (images.length === 0) errors.images = t('eventForm.error.requiredImage');

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            return;
        };

        // Validar fecha
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const eventDate = new Date(event.date);
        if (eventDate <= today) {
            setValidationError("La fecha del evento debe ser posterior a la fecha actual.");
            return;
        }

        if (!images) {
            const allowedExtensions = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"];
            const lowerPreview = images[0];

            const isValidImage = allowedExtensions.some(ext => lowerPreview.endsWith(ext));

            if (!isValidImage) {
                setErrorMessage("La imagen actual no es un formato válido. Por favor selecciona una imagen válida.");
                return;
            }
        }

        setValidationError("");
        setErrorMessage("");
        setSuccessMessage("");

        const formData = new FormData();
        formData.append("event", new Blob([JSON.stringify(event)], { type: "application/json" }));
        images.forEach((file) => formData.append("images", file));

        try {
            const response = await fetch("/api/event/new", {
                method: "POST",
                body: formData,
                headers: {
                    Authorization: `Bearer ${authToken}`,
                },
            });

            if (response.ok) {
                const data = await response.json();
                setSuccessMessage(`Evento "${data.name}" creado con éxito.`);
                setEvent({
                    name: "",
                    description: "",
                    date: "",
                    location: "",
                    moreInfo: "",
                });
                setImages([]);
                navigate("/event/all-my-events");
            } else {
                const err = await response.text();
                throw new Error(err || "Error al crear el evento.");
            }
        } catch (error) {
            setErrorMessage(error.message);
        }
    };

    if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 border-solid mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">Cargando información...</p>
                </div>
            </div>
        );
    }

    if (!role || role !== 'ARTIST' || !isVerified) {
        return <NonAuthorise />;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white py-10 px-4 sm:px-10">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">{t('eventForm.title')}</h2>

                {validationError && (
                    <div className="text-red-900 text-sm mt-2 bg-red-300 rounded-md p-4 mb-4">{validationError}</div>
                )}
                {errorMessage && (
                    <div className="text-red-900 text-sm mt-2 bg-red-300 rounded-md p-4 mb-4">{errorMessage}</div>
                )}
                {successMessage && (
                    <div className="text-green-900 text-sm mt-2 bg-green-300 rounded-md p-4 mb-4">{successMessage}</div>
                )}

                <form onSubmit={handleSubmit} className="space-y-6" encType="multipart/form-data">
                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventForm.label.name')}</label>
                        <input
                            type="text"
                            name="name"
                            value={event.name}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                        {validationErrors.name && (
                            <p className="text-red-600 text-sm">{validationErrors.name}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventForm.label.description')}</label>
                        <textarea
                            name="description"
                            rows="3"
                            value={event.description}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                        {validationErrors.description && (
                            <p className="text-red-600 text-sm">{validationErrors.description}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventForm.label.date')}</label>
                        <input
                            type="date"
                            name="date"
                            value={event.date}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                        {validationErrors.date && (
                            <p className="text-red-600 text-sm">{validationErrors.date}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventForm.label.location')}</label>
                        <input
                            type="text"
                            name="location"
                            placeholder={t('eventForm.placeholder')}
                            value={event.location}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                        {validationErrors.location && (
                            <p className="text-red-600 text-sm">{validationErrors.location}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventForm.label.moreInfo')}</label>
                        <textarea
                            name="moreInfo"
                            rows="2"
                            value={event.moreInfo}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('eventForm.label.images')}</label>
                        <input
                            type="file"
                            onChange={handleImageChange}
                            className="block w-full text-sm text-gray-500"
                            accept="image/*"

                        />
                        {validationErrors.images && (
                            <p className="text-red-600 text-sm">{validationErrors.images}</p>
                        )}
                    </div>

                    <button
                        type="submit"
                        className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                    >
                        {t('eventForm.button.create')}
                    </button>

                    {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
                </form>
            </div>
        </div>
    );
};

export default CreateEventForm;
