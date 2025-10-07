import { useState, useEffect } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get("token");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const {t, i18n} = useTranslation();
     const [validationErrors, setValidationErrors] = useState({});
    
    const language = i18n.language;
        useEffect(() => {
        setError("")
        setValidationErrors({})
    },[language]);


    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        let errors = {};
        if (!password) errors.password = t('resetPassword.error.requiredPassword');
        if (!confirmPassword) errors.confirmPassword = t('resetPassword.error.requitedConfirmPassword');

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            return;
        }


        if (password !== confirmPassword) {
            setError(t('resetPassword.noMatchingPassword'));
            return;
        }

        setLoading(true);

        try {
            const response = await fetch("http://localhost:8080/api/auth/reset-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ token, newPassword: password }),
            });

            if (!response.ok) {
                throw new Error(t('resetPassword.tokenExpire'));
            }

            setSuccess(t('resetPassword.successMessage'));
            setTimeout(() => navigate("/"), 2000);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200 px-6">
            <div className="bg-white p-10 rounded-2xl shadow-lg w-full max-w-md">
                <h2 className="text-3xl font-bold text-center text-blue-700 mb-6">{t('resetPassword.resetPassword')}</h2>

                {error && (
                    <p className="text-red-600 bg-red-100 px-4 py-2 rounded-lg mb-4">{error}</p>
                )}
                {success && (
                    <p className="text-green-600 bg-green-100 px-4 py-2 rounded-lg mb-4">{success}</p>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-gray-600 font-semibold mb-2">{t('resetPassword.newPassword')}</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            className="w-full px-4 py-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                        />
                        {validationErrors.password && (
                            <p className="text-red-600 text-sm">{validationErrors.password}</p>
                        )}
                    </div>

                    <div>
                        <label className="block text-gray-600 font-semibold mb-2">{t('resetPassword.confirmPassword')}</label>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="••••••••"
                            className="w-full px-4 py-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                        />
                        {validationErrors.confirmPassword && (
                            <p className="text-red-600 text-sm">{validationErrors.confirmPassword}</p>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold shadow-md hover:bg-blue-700 transition disabled:opacity-50"
                    >
                        {loading ? t('resetPassword.processing') : t('resetPassword.changePassword')}
                    </button>
                </form>
            </div>
        </div>
    );
}
