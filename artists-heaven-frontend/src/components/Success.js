import { useEffect, useState, useContext } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCheckCircle } from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from "react-i18next";
import { CartContext } from "../context/CartContext";

export default function Success() {
    const [status, setStatus] = useState("loading");
    const [details, setDetails] = useState(null);
    const { t } = useTranslation();
    const { shoppingCart, setShoppingCart, handleDeleteProduct } = useContext(CartContext);

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const sessionId = params.get("session_id");

        if (!sessionId) {
            setStatus("error");
            return;
        }

        fetch(`/api/payment_process/confirm?session_id=${sessionId}`)
            .then(res => res.json())
            .then(data => {
                if (data.status === "success") {
                    setStatus("success");
                    setDetails(data);
                    setShoppingCart({ items: [] });
                } else {
                    setStatus("pending");
                }
            })
            .catch(() => setStatus("error"));
    }, []);

    if (status === "loading") {
        return (
            <div className="flex items-center justify-center h-screen bg-gray-50">
                <div className="text-lg text-gray-600 animate-pulse">
                    {t("success.paymentProcessing")}
                </div>
            </div>
        );
    }

    return (
        <div className="flex items-center justify-center h-screen bg-gray-50">
            <div className="bg-white shadow-lg rounded-2xl p-10 text-center max-w-lg animate-fadeIn">
                <div className="flex justify-center mb-6">
                    <FontAwesomeIcon icon={faCheckCircle} className="text-green-500" size="5x" />
                </div>
                <h1 className="text-3xl font-bold text-gray-800 mb-4">
                    {t("success.paymentSuccess")}
                </h1>
                <p className="text-gray-600 text-lg mb-6">
                    {t("success.paymentReceived")}
                    {t("success.paymentStatus")} <span className="font-semibold text-green-600">{t("success.completed")}</span>
                </p>
                <div className="bg-gray-100 rounded-xl p-4 mb-6">
                    <p className="text-gray-800 text-lg">
                        <span className="font-semibold">{t("success.total")}: </span> {details.amount_total} {details.currency.toUpperCase()}
                    </p>
                    <p className="text-gray-600 mt-2">
                        {t("success.confirmationEmail")}
                        <span className="font-semibold"> {details.email}</span>
                    </p>
                </div>
                <a
                    href="/"
                    className="inline-block bg-green-500 text-white font-semibold py-3 px-6 rounded-xl shadow hover:bg-green-600 transition"
                >
                    {t("success.backToShop")}
                </a>
            </div>
        </div>
    );
}
