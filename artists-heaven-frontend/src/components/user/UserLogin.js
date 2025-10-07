import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { GoogleLogin } from "@react-oauth/google";
import { useTranslation } from "react-i18next";
import { Eye, EyeOff } from "lucide-react";

const UserLogin = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const { t, i18n } = useTranslation();
    const language = i18n.language;
    const [validationErrors, setValidationErrors] = useState({});
    const [showPassword, setShowPassword] = useState(false);

    useEffect(() => {
        setValidationErrors({})
        setError("");
    }, [language]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError("");

        setValidationErrors({});

        let errors = {};
        if (!email) errors.email = t('userLogin.error.requiredEmail');
        if (!password) errors.password = t('userLogin.error.requiredPassword');

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            setIsLoading(false);
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) {
                throw new Error(t('login.error'));
            }

            const data = await response.json();
            const { token, refreshToken, role } = data;

            localStorage.setItem("authToken", token);
            localStorage.setItem("refreshToken", refreshToken);
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
        console.log("Google login failed:", error);
    };

    const handleGoogleLoginSuccess = async (response) => {
        const googleToken = response.credential;

        try {
            const backendResponse = await fetch("http://localhost:8080/api/auth/google-login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ idTokenString: googleToken }),
            });

            if (!backendResponse.ok) {
                throw new Error("Error during Google login");
            }

            const data = await backendResponse.json();
            const { accessToken, refreshToken, email, role } = data;

            localStorage.setItem("authToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);
            localStorage.setItem("userEmail", email);
            localStorage.setItem("role", role);

            window.location.href = "/";
        } catch (error) {
            console.error("Error during Google login:", error);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-tr from-gray-100 via-white to-gray-200 px-6 py-12">
            <div className="w-full max-w-5xl bg-white rounded-3xl shadow-xl overflow-hidden grid grid-cols-1 md:grid-cols-2">
                {/* PANEL IZQUIERDO */}
                <div className="bg-gradient-to-br from-blue-600 to-blue-900 p-12 flex flex-col justify-center space-y-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* EMAIL */}
                        <div>
                            <label
                                htmlFor="email"
                                className="block text-sm font-semibold text-blue-200 mb-2"
                            >
                                {t("login.email")}
                            </label>
                            <input
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder={t("login.email")}
                                className="w-full rounded-lg px-5 py-3 text-gray-800 font-medium focus:outline-none focus:ring-4 focus:ring-blue-400 shadow-md transition"
                            />
                            {validationErrors.email && (
                                <p className="text-white text-sm">{validationErrors.email}</p>
                            )}
                        </div>

                        {/* PASSWORD con toggle 👁️ */}
                        <div className="relative">
                            <label
                                htmlFor="password"
                                className="block text-sm font-semibold text-blue-200 mb-2"
                            >
                                {t("login.password")}
                            </label>
                            <input
                                type={showPassword ? "text" : "password"}
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                className="w-full rounded-lg px-5 py-3 pr-12 text-gray-800 font-medium focus:outline-none focus:ring-4 focus:ring-blue-400 shadow-md transition"
                            />
                            <button
                                type="button"
                                onClick={() => setShowPassword(!showPassword)}
                                className="absolute right-3 top-[42px] text-gray-500 hover:text-gray-300"
                            >
                                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                            </button>
                            {validationErrors.password && (
                                <p className="text-white text-sm">{validationErrors.password}</p>
                            )}
                        </div>

                        {/* LINK RESET PASSWORD */}
                        <div className="text-right mt-2">
                            <Link
                                to="/forgot-password"
                                className="text-sm text-blue-200 hover:underline"
                            >
                                {t("login.forgotPassword")}
                            </Link>
                        </div>

                        {/* ERROR */}
                        {error && (
                            <p className="text-sm text-red-700 bg-red-100 px-4 py-2 rounded-lg shadow-inner">
                                {error}
                            </p>
                        )}

                        {/* LOGIN BUTTON */}
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full py-3 bg-white bg-opacity-30 hover:bg-opacity-40 text-white font-semibold rounded-xl shadow-lg transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoading
                                ? t("login.loginButton") + "..."
                                : t("login.loginButton")}
                        </button>
                    </form>

                    {/* GOOGLE LOGIN */}
                    <div className="w-full bg-white bg-opacity-30 hover:bg-opacity-40 text-white font-semibold rounded-xl shadow-lg transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed">
                        <GoogleLogin
                            onSuccess={handleGoogleLoginSuccess}
                            onError={handleGoogleLoginError}
                        />
                    </div>
                </div>

                {/* PANEL DERECHO */}
                <div className="bg-white p-12 flex flex-col justify-center items-center text-center">
                    <h2 className="text-5xl custom-font-shop custom-font-shop-black mb-6 tracking-tight">
                        {t("login.welcome")}
                    </h2>
                    <p className="text-gray-600 text-lg font-medium mb-8">
                        {t("login.noAccount")}
                    </p>
                    <div className="flex flex-col sm:flex-row gap-6 justify-center w-full">
                        <Link to="/artists/register" className="w-full">
                            <button className="w-full py-3 bg-gradient-to-r from-yellow-400 to-yellow-500 hover:from-yellow-500 hover:to-yellow-600 text-white font-semibold rounded-2xl shadow-lg transition duration-300">
                                {t("login.registerArtist")}
                            </button>
                        </Link>
                        <Link to="/user/register" className="w-full">
                            <button className="w-full py-3 bg-gradient-to-r from-blue-600 to-blue-900 hover:from-blue-900 hover:to-blue-800 text-white font-semibold rounded-2xl shadow-lg transition duration-300">
                                {t("login.registerUser")}
                            </button>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserLogin;
