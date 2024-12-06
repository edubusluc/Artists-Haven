import React, { useState } from "react";
import axios from "axios";

const UserLogin = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError("");

        try {
            // Realizar la solicitud POST al backend
            const response = await axios.post("/api/auth/login", {
                email,
                password,
            });

            // Guardar el token en el almacenamiento local
            const token = response.data;
            localStorage.setItem("authToken", token);

            // Guardar el correo del usuario en el localStorage
            localStorage.setItem("userEmail", email);

            // Configurar el token en las cabeceras de axios para futuras solicitudes
            axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;

            // Redirigir al usuario a la página principal (u otra página)
            window.location.href = "/";  // Puedes cambiar esta ruta si es necesario

        } catch (err) {
            console.error("Error during login:", err);
            setError("Credenciales incorrectas. Por favor, inténtalo de nuevo.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="login-form-container">
            <h2>Iniciar Sesión</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="email">Correo Electrónico</label>
                    <input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="password">Contraseña</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>

                <button type="submit" disabled={isLoading}>
                    {isLoading ? "Cargando..." : "Iniciar Sesión"}
                </button>

                {error && <p className="error-message">{error}</p>}
            </form>
        </div>
    );
};

export default UserLogin;
