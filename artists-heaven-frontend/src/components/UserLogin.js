import React, { useState } from "react";
import { Link } from 'react-router-dom';

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
            // Realizar la solicitud POST al backend con fetch
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                throw new Error("Credenciales incorrectas. Por favor, inténtalo de nuevo.");
            }

            const data = await response.json();
            const token = data.token;
            const role = data.role;

            // Guardar el token y el correo en el localStorage
            localStorage.setItem("authToken", token);
            localStorage.setItem("userEmail", email);
            localStorage.setItem("role", role);

            // Redirigir al usuario a la página principal (o a otra página)
            localStorage.setItem('firstTime', false);
            window.location.href = "/"; // Puedes cambiar esta ruta si es necesario

        } catch (err) {
            console.error("Error during login:", err);
            setError(err.message);
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
            <Link to="/">
                <button>Home</button>
            </Link>
        </div>
    );
};

export default UserLogin;
