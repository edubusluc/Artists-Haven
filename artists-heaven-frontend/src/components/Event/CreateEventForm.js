import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

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
    const navigate = useNavigate();



    if (role !== "ARTIST") {
        return <div className="text-red-600 p-4">No tienes permisos para acceder a esta página</div>;
    }

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEvent((prev) => ({ ...prev, [name]: value }));
    };


    const handleImageChange = (e) => {
        setImages(Array.from(e.target.files));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (images.length === 0) {
            setValidationError("Debes seleccionar al menos una imagen.");
            return;
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const eventDate = new Date(event.date);
        if (eventDate <= today) {
            setValidationError("La fecha del evento debe ser posterior a la fecha actual.");
            return;
        }

        console.log("EVENT DATE" + eventDate)

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
                navigate("/");
            } else {
                const err = await response.text();
                throw new Error(err || "Error al crear el evento.");
            }
        } catch (error) {
            setErrorMessage(error.message);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white py-10 px-4 sm:px-10">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-6 text-gray-700 text-center">Crear Nuevo Evento</h2>

                {validationError && (
                    <div className="text-red-900 text-sm mt-2 bg-red-300 rounded-md p-4 mb-4">{validationError}</div>
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
                        <label className="block font-semibold mb-2 text-sm text-gray-600">Subir imágenes</label>
                        <input
                            type="file"
                            multiple
                            onChange={handleImageChange}
                            className="block w-full text-sm text-gray-500"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="w-full py-2 bg-yellow-400 text-black font-semibold rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                    >
                        Crear Evento
                    </button>

                    {successMessage && <div className="text-green-600 text-sm mt-3">{successMessage}</div>}
                    {errorMessage && <div className="text-red-600 text-sm mt-3">{errorMessage}</div>}
                </form>
            </div>
        </div>
    );
};

export default CreateEventForm;
