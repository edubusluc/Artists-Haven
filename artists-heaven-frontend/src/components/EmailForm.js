import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPaperPlane, faSpinner } from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from "react-i18next";
import Footer from "./Footer";

const EmailForm = () => {
    const storedEmail = localStorage.getItem("userEmail");
    const [isAuthenticated] = useState(!!storedEmail);
    const [loading, setLoading] = useState(false);
    const [validationErrors, setValidationErrors] = useState({});
    const { t, i18n } = useTranslation();
    const language = i18n.language;

    useEffect(() => {
        setValidationErrors({})
    }, [language]);

    const [emailData, setEmailData] = useState({
        subject: "",
        sender: storedEmail ? storedEmail : "",
        username: storedEmail ? storedEmail : "Usuario No Autenticado",
        description: "",
        type: "BUG_REPORT",
    });

    const emailTypes = [
        { value: "BUG_REPORT", label: t('emailForm.bugReport') },
        { value: "FEATURE_REQUEST", label: t('emailForm.featureRequest') },
        { value: "ABUSE_REPORT", label: t('emailForm.abuseReport') },
        { value: "ISSUE_REPORT", label: t('emailForm.issueReport') },
        { value: "SECURITY_REPORT", label: t('emailForm.securityReport') },
    ];

    useEffect(() => {
        setEmailData((prevData) => ({
            ...prevData,
            subject: prevData.type + " " + (prevData.sender || "Usuario"),
        }));
    }, [emailData.type, emailData.sender]);

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === "sender") {
            setEmailData({ ...emailData, sender: value, username: value });
        } else {
            setEmailData({ ...emailData, [name]: value });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setValidationErrors({});
        let errors = {};

        if (!emailData.sender && !isAuthenticated) errors.sender = t('emailForm.error.requiredSender');
        if (!emailData.description) errors.description = t('emailForm.error.requiredDescription');

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            setLoading(false);
            return;
        }

        try {
            const response = await fetch("/api/emails/send", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                mode: "cors",
                body: JSON.stringify(emailData),
            });

            if (response.ok) {
                window.location.href = "/";
            } else {
                console.error("Error enviando el email", response.statusText);
                setLoading(false);
            }
        } catch (error) {
            console.error("Error enviando el email", error);
            setLoading(false);
        }
    };

    return (
        <><div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-gray-100 to-gray-200 p-6">
            <motion.div
                initial={{ opacity: 0, y: 40 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4 }}
                className="w-full max-w-lg bg-white rounded-2xl shadow-lg p-6"
            >
                <h2 className="text-2xl font-semibold text-gray-800 mb-4 text-center">
                    {t('emailForm.sendReport')}
                </h2>

                {isAuthenticated &&
                    <p className="text-sm text-gray-500 mb-6 text-center">
                        {t('emailForm.yourEmail')}:{" "}
                        <span className="font-medium text-gray-700">
                            {emailData.sender}
                        </span>
                    </p>}

                <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Email si no está autenticado */}
                    {!isAuthenticated && (
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t('emailForm.email')}
                            </label>
                            <input
                                type="email"
                                name="sender"
                                value={emailData.sender}
                                onChange={handleChange}
                                className="w-full rounded-xl border-2 border-gray-100 focus:border-indigo-500 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 p-2 transition-all duration-300 ease-in-out" />
                            {validationErrors.sender && (
                                <p className="text-red-600 text-sm">{validationErrors.sender}</p>
                            )}
                        </div>
                    )}

                    {/* Tipo de reporte */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t('emailForm.reportType')}
                        </label>
                        <select
                            name="type"
                            value={emailData.type}
                            onChange={handleChange}
                            className="w-full rounded-xl border-2 border-gray-100 focus:border-indigo-500 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 p-2 transition-all duration-300 ease-in-out"
                        >
                            {emailTypes.map((type) => (
                                <option key={type.value} value={type.value}>
                                    {type.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Descripción */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t('emailForm.description')}
                        </label>
                        <textarea
                            name="description"
                            placeholder={t('emailForm.placeholder')}
                            value={emailData.description}
                            onChange={handleChange}
                            className="w-full rounded-xl h-32 border-2 border-gray-100 focus:border-indigo-500 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 p-2 transition-all duration-300 ease-in-out"
                        ></textarea>
                        {validationErrors.description && (
                            <p className="text-red-600 text-sm">{validationErrors.description}</p>
                        )}
                    </div>

                    {/* Botón */}
                    <motion.button
                        whileHover={!loading ? { scale: 1.03 } : {}}
                        whileTap={!loading ? { scale: 0.97 } : {}}
                        type="submit"
                        disabled={loading}
                        className={`w-full flex items-center justify-center gap-2 font-medium py-2 px-4 rounded-xl shadow-md transition-colors ${loading
                            ? "bg-gray-400 text-white cursor-not-allowed"
                            : "bg-indigo-600 text-white hover:bg-indigo-700"}`}
                    >
                        {loading ? (
                            <>
                                <FontAwesomeIcon icon={faSpinner} spin className="w-4 h-4" />
                                {t('emailForm.processing')}
                            </>
                        ) : (
                            <>
                                <FontAwesomeIcon icon={faPaperPlane} className="w-4 h-4" />
                                {t('emailForm.sendEmail')}
                            </>
                        )}
                    </motion.button>
                </form>
            </motion.div>
        </div><Footer /></>
    );
};

export default EmailForm;
