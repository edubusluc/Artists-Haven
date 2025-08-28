import { faWarning } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useTranslation } from 'react-i18next';

const SessionExpired = () => {
    const handleReload = () => {
        window.location.href = '/auth/login';
    };

    const { t } = useTranslation();

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200 p-6">
            <div className="bg-white shadow-xl rounded-2xl p-8 max-w-md text-center space-y-6">
                <div className="flex justify-center text-yellow-500">
                    <FontAwesomeIcon icon={faWarning} />
                </div>
                <h2 className="text-2xl font-semibold text-gray-800">
                    {t("sessionExpired.title")}
                </h2>
                <p className="text-gray-600">
                    {t("sessionExpired.message")}
                </p>
                <button
                    onClick={handleReload}
                    className="mt-4 px-6 py-3 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition duration-200 shadow-md"
                >
                    {t("sessionExpired.reLogin")}
                </button>
            </div>
        </div>
    );
};

export default SessionExpired;
