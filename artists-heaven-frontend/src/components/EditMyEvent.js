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
    const [previewImage, setPreviewImage] = useState(null); // Nueva imagen para previsualización
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
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
                        'Authorization': `Bearer ${authToken}`,
                    },
                });

                if (!response.ok) {
                    throw new Error("Error al obtener el evento");
                }

                const data = await response.json();
                setEvent(data);
                setPreviewImage(`/api/event${data.image}`); // Mostrar la imagen actual del evento
            } catch (error) {
                setErrorMessage(error.message);
            }
        };

        fetchEventDetails();
    }, [id, authToken, role]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEvent({ ...event, [name]: value });
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setImage(file);

        if (file) {
            // Generar la URL de previsualización
            const previewUrl = URL.createObjectURL(file);
            setPreviewImage(previewUrl);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const data = new FormData();
        data.append("event", new Blob([JSON.stringify(event)], { type: "application/json" }));
        if (image) data.append("image", image);

        try {
            const response = await fetch(`/api/event/edit/${id}`, {
                method: "PUT",
                headers: {
                    'Authorization': `Bearer ${authToken}`,
                },
                body: data,
            });

            if (!response.ok) {
                throw new Error("Error al actualizar el evento");
            }

            setSuccessMessage("Evento actualizado con éxito");
            navigate("/event/allMyEvents");
        } catch (error) {
            setErrorMessage(error.message);
        }
    };

    if(role !== "ARTIST") {
        return errorMessage ? <div className="alert alert-danger mt-3">{errorMessage}</div> : null;
    }

    return (
        <div>
            <h1>Editar Evento</h1>
            {errorMessage && <div className="alert alert-danger">{errorMessage}</div>}
            {successMessage && <div className="alert alert-success">{successMessage}</div>}
            <form onSubmit={handleSubmit}>
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
                    <label htmlFor="image" className="form-label">Imagen del Evento</label>
                    {previewImage && (
                        <div className="mb-2">
                            <img src={previewImage} alt={event.name} style={{ width: "150px", height: "150px", objectFit: "cover" }} />
                        </div>
                    )}
                    <input
                        type="file"
                        className="form-control"
                        id="image"
                        name="image"
                        onChange={handleFileChange}
                    />
                </div>
                <button type="submit" className="btn btn-primary">Actualizar Evento</button>
            </form>
        </div>
    );
};

export default EditMyEvent;
