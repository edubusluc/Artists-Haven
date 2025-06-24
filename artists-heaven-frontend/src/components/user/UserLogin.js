import  { useState } from "react";
import { Link } from "react-router-dom";
import { GoogleLogin } from '@react-oauth/google';

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

            localStorage.setItem("authToken", token);
            localStorage.setItem("userEmail", email);
            localStorage.setItem("role", role);
            localStorage.setItem("firstTime", false);

            window.location.href = "/";
        } catch (err) {
            console.error("Error during login:", err);
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleGoogleLoginError = (error) => {
        console.log('Google login failed:', error);
    };

    const handleGoogleLoginSuccess = async (response) => {
        console.log('Google login successful:', response);
        const googleToken = response.credential;

        try {
            const backendResponse = await fetch('/api/auth/google-login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ idTokenString: googleToken }),
            });

            if (!backendResponse.ok) {
                throw new Error('Error during Google login');
            }

            const data = await backendResponse.json();
            const jwtToken = data.token;

            localStorage.setItem('authToken', jwtToken);
            localStorage.setItem('userEmail', data.email);
            localStorage.setItem('role', data.role);

            window.location.href = '/';
        } catch (error) {
            console.error('Error during Google login:', error);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-tr from-gray-100 via-white to-gray-200 px-6 py-12">
            <div className="w-full max-w-5xl bg-white rounded-3xl shadow-xl overflow-hidden grid grid-cols-1 md:grid-cols-2">
                {/* PANEL IZQUIERDA */}
                <div className="bg-gradient-to-br from-blue-600 to-blue-900 p-12 flex flex-col justify-center space-y-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label htmlFor="email" className="block text-sm font-semibold text-blue-200 mb-2">
                                Correo Electrónico
                            </label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                placeholder="tuemail@ejemplo.com"
                                className="w-full rounded-lg px-5 py-3 text-gray-800 font-medium focus:outline-none focus:ring-4 focus:ring-blue-400 shadow-md transition"
                            />
                        </div>

                        <div>
                            <label htmlFor="password" className="block text-sm font-semibold text-blue-200 mb-2">
                                Contraseña
                            </label>
                            <input
                                type="password"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                placeholder="••••••••"
                                className="w-full rounded-lg px-5 py-3 text-gray-800 font-medium focus:outline-none focus:ring-4 focus:ring-blue-400 shadow-md transition"
                            />
                        </div>

                        {error && (
                            <p className="text-sm text-red-700 bg-red-100 px-4 py-2 rounded-lg shadow-inner">
                                {error}
                            </p>
                        )}

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full py-3 bg-white bg-opacity-30 hover:bg-opacity-40 text-white font-semibold rounded-xl shadow-lg transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoading ? "Cargando..." : "Iniciar Sesión"}
                        </button>
                    </form>
                    <div className="w-full  bg-white bg-opacity-30 hover:bg-opacity-40 text-white font-semibold rounded-xl shadow-lg transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed">
                        <GoogleLogin onSuccess={handleGoogleLoginSuccess} onError={handleGoogleLoginError} />
                    </div>
                </div>

                {/* PANEL DERECHA */}
                <div className="bg-white p-12 flex flex-col justify-center items-center text-center">
                    <h2
                        className="text-5xl custom-font-shop custom-font-shop-black mb-6 tracking-tight"
                    >
                        ARTISTS HEAVEN
                    </h2>
                    <p className="text-gray-600 text-lg font-medium mb-8">
                        ¿No tienes cuenta? Regístrate como:
                    </p>
                    <div className="flex flex-col sm:flex-row gap-6 justify-center w-full max-w-md">
                        <Link to="/artists/register" className="w-full sm:w-auto">
                            <button className="w-full sm:w-auto px-20 py-3 bg-gradient-to-r from-yellow-400 to-yellow-500 hover:from-yellow-500 hover:to-yellow-600 text-white font-semibold rounded-2xl shadow-lg transition duration-300">
                                Artista
                            </button>
                        </Link>
                        <Link to="/user/register" className="w-full sm:w-auto">
                            <button className="w-full sm:w-auto px-20 py-3 bg-gradient-to-r from-blue-600 to-blue-900 hover:from-blue-900 hover:to-blue-800 text-white font-semibold rounded-2xl shadow-lg transition duration-300">
                                Usuario
                            </button>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserLogin;
