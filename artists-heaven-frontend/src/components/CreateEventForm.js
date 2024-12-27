import React, { useState, useEffect } from "react";

const CreateEventForm = () => {
    const [event, setEvent] = useState({
        name: "",
        description: "",
        date: "",
        location: "",
        moreInfo: "",
    });

    const [image, setImage] = useState(null);
    const [message, setMessage] = useState("");
      const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEvent({ ...event, [name]: value });
    };

    const handleFileChange = (e) => {
        setImage(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validar que se haya seleccionado una imagen
        if (!image) {
            setMessage("Por favor, selecciona una imagen.");
            return;
        }

        // Crear el FormData
        const data = new FormData();
        data.append("event", new Blob([JSON.stringify(event)], { type: "application/json" }));
        data.append("images", image);

        try {
            const response = await fetch("/api/event/new", {
                method: "POST",
                body: data,
                headers: {
                    'Authorization': `Bearer ${authToken}`
                },
            });

            if (response.ok) {
                setMessage("Evento creado con éxito.");
                setEvent({
                    name: "",
                    description: "",
                    date: "",
                    location: "",
                    moreInfo: "",
                    artist: "",
                });
                setImage(null);
                window.location.href = "/";
            } else {
                setMessage("Error al crear el evento.");
            }
        } catch (error) {
            console.error("Error:", error);
            setMessage("Hubo un error al enviar el formulario.");
        }
    };

    return (
        <div className="container mt-5">
            <h2>Crear Nuevo Evento</h2>
            <form onSubmit={handleSubmit} encType="multipart/form-data">
                <div className="mb-3">
                    <label htmlFor="name" className="form-label">Nombre del Evento</label>
                    <input
                        type="text"
                        className="form-control"
                        id="name"
                        name="name"
                        value={event.name}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label htmlFor="description" className="form-label">Descripción</label>
                    <textarea
                        className="form-control"
                        id="description"
                        name="description"
                        value={event.description}
                        onChange={handleChange}
                        required
                    ></textarea>
                </div>
                <div className="mb-3">
                    <label htmlFor="date" className="form-label">Fecha</label>
                    <input
                        type="date"
                        className="form-control"
                        id="date"
                        name="date"
                        value={event.date}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label htmlFor="location" className="form-label">Ubicación</label>
                    <input
                        type="text"
                        className="form-control"
                        id="location"
                        name="location"
                        value={event.location}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label htmlFor="moreInfo" className="form-label">Información Adicional</label>
                    <textarea
                        className="form-control"
                        id="moreInfo"
                        name="moreInfo"
                        value={event.moreInfo}
                        onChange={handleChange}
                    ></textarea>
                </div>
                <div className="mb-3">
                    <label htmlFor="images" className="form-label">Imagen del Evento</label>
                    <input
                        type="file"
                        className="form-control"
                        id="images"
                        name="images"
                        onChange={handleFileChange}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary">Crear Evento</button>
            </form>
            {message && <div className="alert alert-info mt-3">{message}</div>}
        </div>
    );
};

export default CreateEventForm;