import { useEffect, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTimesCircle } from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from 'react-i18next';

export default function Cancel() {
    const [status, setStatus] = useState("loading");
    const [details, setDetails] = useState(null);
    const {t} = useTranslation();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const sessionId = params.get("session_id");

        if (!sessionId) {
            setStatus("error");
            return;
        }
    }, []);

    if (status === "loading") {
        return (
            <div className="flex items-center justify-center h-screen bg-gray-50">
                <div className="text-lg text-gray-600 animate-pulse">
                    {t('cancel.checkingPaymentStatus')}
                </div>
            </div>
        );
    }

    return (
        <div className="flex items-center justify-center h-screen bg-gray-50">
            <div className="bg-white shadow-lg rounded-2xl p-10 text-center max-w-lg animate-fadeIn">
                <div className="flex justify-center mb-6">
                    <FontAwesomeIcon icon={faTimesCircle} className="text-red-500" size="5x" />
                </div>
                <h1 className="text-3xl font-bold text-gray-800 mb-4">
                    {t('cancel.paymentCanceled')}
                </h1>
                <p className="text-gray-600 text-lg mb-6">
                    {t('cancel.failedTransaction')}
                </p>
                <div className="bg-gray-100 rounded-xl p-4 mb-6">
                    <p className="text-gray-600 mt-2">
                        {t('cancel.help')}
                    </p>
                </div>
                <a
                    href="/"
                    className="inline-block bg-red-500 text-white font-semibold py-3 px-6 rounded-xl shadow hover:bg-red-600 transition"
                >
                    {t('cancel.backToShop')}
                </a>
            </div>
        </div>
    );
}
