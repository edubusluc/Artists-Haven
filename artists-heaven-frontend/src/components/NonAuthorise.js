import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faShieldAlt, faArrowLeft } from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from "react-i18next";

const NonAuthorise = () => {
  const {t} = useTranslation();
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 px-4">
      <div className="bg-white shadow-xl rounded-2xl p-10 max-w-md w-full text-center border border-gray-200">
        <div className="flex justify-center mb-6">
          <div className="bg-red-100 text-red-600 p-5 rounded-full">
            <FontAwesomeIcon icon={faShieldAlt} size="2x" />
          </div>
        </div>

        <h1 className="text-3xl font-semibold text-gray-800 mb-3">
          {t("nonauthorise.accessDenied")}
        </h1>
        <p className="text-gray-600 mb-6">
          {t("nonauthorise.noPermissions")}
        </p>

        <button
          onClick={() => window.location.href="/"}
          className="flex items-center gap-2 mx-auto px-4 py-2 rounded-xl border border-gray-300 text-gray-700 hover:bg-gray-100 transition"
        >
          <FontAwesomeIcon icon={faArrowLeft} />
          {t("nonauthorise.backToHome")}
        </button>
      </div>
    </div>
  );
};

export default NonAuthorise;
