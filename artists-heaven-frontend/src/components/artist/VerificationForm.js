import React, { useState } from 'react';
import { Link } from 'react-router-dom';

function VerificationForm() {
    const [authToken] = useState(localStorage.getItem("authToken"));
    const [email] = useState(localStorage.getItem("userEmail"));
    const [description, setDescription] = useState(
        `Debe adjuntar un archivo de vídeo en el que se le muestre de manera reconocible recitando la siguiente oración: Soy ${email} y solicito la activación de mi cuenta`
    );
    const [video, setVideo] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!video) {
            alert("Por favor, adjunte un archivo de vídeo.");
            return;
        }

        const formData = new FormData();
        formData.append('email', email);
        formData.append('description', description);
        formData.append('video', video);

        try {
            const response = await fetch('/api/verification/send', {
                method: 'POST',
                body: formData,
                headers: {
                    'Authorization': `Bearer ${authToken}`
                },
            });

            if (!response.ok) {
                throw new Error('Error en la solicitud: ' + response.statusText);
            }
            alert("Solicitud enviada con éxito");
            window.location.href = '/';
        } catch (error) {
            alert(error.message || 'Error al enviar la solicitud');
        }
    };

    const handleVideoChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (!file.type.startsWith('video/')) {
                alert('Por favor, seleccione un archivo de vídeo válido.');
                e.target.value = null;
                setVideo(null);
                return;
            }
            setVideo(file);
        }
    };

    return (
        <div
            className="min-h-screen flex items-center justify-center p-6 bg-gradient-to-r from-gray-300 to-white "
            style={{ backgroundImage: `url('/someBackground.png')`, backgroundSize: 'cover', backgroundPosition: 'center' }}
        >
            <div className="w-full max-w-3xl bg-white rounded-lg shadow-lg p-8 backdrop-blur-md">
                <h2 className="text-2xl font-bold mb-6 text-center text-gray-900">
                    Verificación de Cuenta
                </h2>
                <p className="mb-4 text-sm font-semibold text-red-600">
                    EL TAMAÑO MÁXIMO DEL ARCHIVO SON 50MB
                </p>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label htmlFor="description" className="block mb-2 font-medium text-gray-700">
                            Descripción
                        </label>
                        <textarea
                            id="description"
                            className="w-full p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 resize-none"
                            rows="4"
                            value={description}
                            readOnly
                        />
                    </div>
                    <div>
                        <label htmlFor="video" className="block mb-2 font-medium text-gray-700">
                            Archivo de vídeo
                        </label>
                        <input
                            id="video"
                            type="file"
                            accept="video/*"
                            onChange={handleVideoChange}
                            required
                            className="w-full"
                        />
                    </div>
                    <div className="flex flex-col md:flex-row md:justify-between gap-4">
                        <button
                            type="submit"
                            className="w-full md:w-auto bg-yellow-400 text-black py-2 px-6 rounded-lg font-semibold hover:bg-yellow-500 transition"
                        >
                            Enviar
                        </button>
                        <Link to="/">
                            <button
                                type="button"
                                className="w-full md:w-auto bg-gray-300 text-gray-700 py-2 px-6 rounded-lg font-semibold hover:bg-gray-400 transition"
                            >
                                Home
                            </button>
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default VerificationForm;
