import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { checkTokenExpiration } from '../../utils/authUtils';
import SessionExpired from '../SessionExpired';
import NonAuthorise from '../NonAuthorise';
import { useTranslation } from 'react-i18next';

const EditMyEvent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [isVerified, setIsVerified] = useState(false);
    const token = localStorage.getItem("authToken");

    const [event, setEvent] = useState({
        name: "",
        description: "",
        date: "",
        location: "",
        moreInfo: "",
    });

    const [image, setImage] = useState(null);
    const [previewImage, setPreviewImage] = useState(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [validationError, setValidationError] = useState("");
    const [validationErrors, setValidationErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);

    const [authToken] = useState(localStorage.getItem("authToken"));
    const role = localStorage.getItem("role");

    const [loading, setLoading] = useState(true);

    const { t, i18n } = useTranslation();
    const language = i18n.language;

    useEffect(() => {
        setValidationErrors({})
    }, [language]);

    useEffect(() => {
        if (role !== "ARTIST") {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            setLoading(false);
            return;
        }

        const fetchVerificationStatus = async () => {
            try {
                const response = await fetch('/api/event/isVerified', {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                if (response.ok) {
                    const dataVerification = await response.json();
                    setIsVerified(dataVerification.data);
                } else {
                    setIsVerified(false);
                }
            } catch (error) {
                console.error("Error fetching verification status", error);
                setIsVerified(false);
            } finally {
                setLoading(false); // Terminó la carga
            }
        };

        const fetchEventDetails = async () => {
            try {
                const response = await fetch(`/api/event/details/${id}`, {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${authToken}`,
                    },
                });

                if (!response.ok) {
                    throw new Error("Error al obtener el evento");
                }

                const eventDetails = await response.json();
                setEvent(eventDetails.data);
                setPreviewImage(`/api/event${eventDetails.data.image}`);
            } catch (error) {
                setErrorMessage(error.message);
            }
        };

        fetchEventDetails();
        fetchVerificationStatus();
    }, [id, authToken, role]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEvent((prev) => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file && file.type.startsWith("image/")) {
            setImage(file);
            const previewUrl = URL.createObjectURL(file);
            setPreviewImage(previewUrl);
        } else {
            setErrorMessage("Por favor selecciona un archivo de imagen válido.");
            setImage(null);
            setPreviewImage(null);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true); // inicio de animación

        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const eventDate = new Date(event.date);

        setValidationErrors({});
        let errors = {};

        if (!event.name) errors.name = t('eventForm.error.requiredName');
        if (!event.description) errors.description = t('eventForm.error.requiredDescription');
        if (!event.date) errors.date = t('eventForm.error.requiredDate');
        if (!event.location) errors.location = t('eventForm.error.requiredLocation');
        if (eventDate <= today) {
            errors.date = errors.date
                ? errors.date + ". " + t('eventForm.error.invalidDate')
                : t('eventForm.error.invalidDate');
        }

        if (!image && !previewImage) {
            errors.image = t('eventForm.error.requiredImage');
        } else if (image || previewImage) {
            const allowedExtensions = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"];
            const imgToCheck = image ? image.name : previewImage;
            const lowerName = imgToCheck.toLowerCase();
            const isValidImage = allowedExtensions.some(ext => lowerName.endsWith(ext));
            if (!isValidImage) {
                setErrorMessage("La imagen actual no es un formato válido. Por favor selecciona una imagen válida.");
                setSubmitting(false);
                return;
            }
        }

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            setSubmitting(false); // termina animación
            return;
        }

        setValidationError("");
        setErrorMessage("");
        setSuccessMessage("");

        const data = new FormData();
        data.append("event", new Blob([JSON.stringify(event)], { type: "application/json" }));
        if (image) data.append("image", image);

        try {
            const response = await fetch(`/api/event/edit/${id}`, {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${authToken}`,
                },
                body: data,
            });

            const result = await response.json();
            const errorMessage = result.message;

            if (!response.ok) throw new Error(errorMessage);

            setSuccessMessage("Evento actualizado con éxito");
            navigate("/event/all-my-Events");
        } catch (error) {
            setErrorMessage(error.message);
        } finally {
            setSubmitting(false); // fin animación
        }
    };


    if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }


    // Mientras se carga, mostramos un mensaje o spinner
    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 border-solid mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">{t('eventEditForm.loadingData')}</p>
                </div>
            </div>
        );
    }

    // Una vez cargado, verificamos autorización
    if (!role || role !== 'ARTIST' || !isVerified) {
        return <NonAuthorise />;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white py-10 px-4 sm:px-10">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">{t('eventEditForm.label')}</h2>

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
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventEditForm.label.name')}</label>
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
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventEditForm.label.description')}</label>
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
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventEditForm.label.date')}</label>
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
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventEditForm.label.location')}</label>
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
                        <label className="block font-semibold mb-1 text-sm text-gray-600">{t('eventEditForm.label.moreInfo')}</label>
                        <textarea
                            name="moreInfo"
                            rows="2"
                            value={event.moreInfo}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">{t('eventEditForm.label.images')}</label>
                        {previewImage && (
                            <div className="mb-3">
                                <img
                                    src={previewImage}
                                    alt={event.name}
                                    className="w-36 h-36 object-cover rounded shadow"
                                />
                            </div>
                        )}
                        <input
                            type="file"
                            name="image"
                            onChange={handleFileChange}
                            className="block w-full text-sm text-gray-500"
                            accept="image/*"
                        />
                        {validationErrors.image && (
                            <p className="text-red-600 text-sm">{validationErrors.image}</p>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={submitting}
                        className={`w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300 flex justify-center items-center ${submitting ? 'opacity-70 cursor-not-allowed' : ''}`}
                    >
                        {submitting ? (
                            <svg className="animate-spin h-5 w-5 text-black mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
                            </svg>
                        ) : null}
                        {submitting ? "Actualizando..." : t("eventEditForm.button.update")}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default EditMyEvent;
