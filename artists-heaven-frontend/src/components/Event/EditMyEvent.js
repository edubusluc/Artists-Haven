import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

const EditMyEvent = () => {
    const { id } = useParams();
    const navigate = useNavigate();

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

    const [authToken] = useState(localStorage.getItem("authToken"));
    const role = localStorage.getItem("role");

    useEffect(() => {
        if (role !== "ARTIST") {
            setErrorMessage("No tienes permisos para acceder a esta página.");
            return;
        }

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

                const data = await response.json();
                setEvent(data);
                setPreviewImage(`/api/event${data.image}`);
            } catch (error) {
                setErrorMessage(error.message);
            }
        };

        fetchEventDetails();
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

        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const eventDate = new Date(event.date);

        if (eventDate <= today) {
            setValidationError("La fecha del evento debe ser posterior a la fecha actual.");
            return;
        }

        if (!image) {
            const allowedExtensions = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"];
            const lowerPreview = previewImage ? previewImage.toLowerCase() : "";

            const isValidImage = allowedExtensions.some(ext => lowerPreview.endsWith(ext));

            if (!isValidImage) {
                setErrorMessage("La imagen actual no es un formato válido. Por favor selecciona una imagen válida.");
                return;
            }
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

            if (!response.ok) {
                throw new Error("Error al actualizar el evento");
            }

            setSuccessMessage("Evento actualizado con éxito");
            navigate("/event/all-my-Events");
        } catch (error) {
            setErrorMessage(error.message);
        }
    };

    if (role !== "ARTIST") {
        return <div className="text-red-600 p-4">No tienes permisos para acceder a esta página</div>;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white py-10 px-4 sm:px-10">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">Editar Evento</h2>

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
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Nombre del evento</label>
                        <input
                            type="text"
                            name="name"
                            value={event.name}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                            required
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Descripción</label>
                        <textarea
                            name="description"
                            rows="3"
                            value={event.description}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                            required
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Fecha</label>
                        <input
                            type="date"
                            name="date"
                            value={event.date}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                            required
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Ubicación</label>
                        <input
                            type="text"
                            name="location"
                            value={event.location}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                            required
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-1 text-sm text-gray-600">Información adicional</label>
                        <textarea
                            name="moreInfo"
                            rows="2"
                            value={event.moreInfo}
                            onChange={handleChange}
                            className="w-full border rounded px-4 py-2 text-sm"
                        />
                    </div>

                    <div>
                        <label className="block font-semibold mb-2 text-sm text-gray-600">Imagen actual</label>
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
                    </div>

                    <button
                        type="submit"
                        className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                    >
                        Actualizar Evento
                    </button>
                </form>
            </div>
        </div>
    );
};

export default EditMyEvent;
