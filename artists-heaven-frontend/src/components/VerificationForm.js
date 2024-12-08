import React, { useState } from 'react';
import { Link } from 'react-router-dom';

function VerificationForm() {
    const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));
    const [email, setEmail] = useState(localStorage.getItem("userEmail"));
    const [description, setDescription] = useState('Debe adjuntar un archivo de vídeo en el que se le muestre de manera reconocible recitando la siguiente oración: Soy ' + localStorage.getItem("userEmail") + " y solicito la activación de mi cuenta");
    const [video, setVideo] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
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
            window.location.href = '/'
        } catch (error) {
            alert(error.message || 'Error al enviar la solicitud');
        }
    };

    return (
        <><form onSubmit={handleSubmit}>
            <textarea
                placeholder="Descripción"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
                readOnly />
            <input
                type="file"
                accept="video/*"
                onChange={(e) => setVideo(e.target.files[0])}
                required />
            <button type="submit">Enviar</button>
        </form>
        <Link to="/">
        <button>Home</button>
      </Link></>
    );
}

export default VerificationForm;
