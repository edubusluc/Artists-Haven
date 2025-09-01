import { useState } from "react";
import { useTranslation } from "react-i18next";

const ReturnRequestModal = ({ isOpen, onClose, onSubmit }) => {
  const [reason, setReason] = useState("");
  const [email, setEmail] = useState("");
  const authToken = localStorage.getItem("authToken");
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (reason.trim() === "") {
      alert(t('returnRequestModal.reasonError'));
      return;
    }

    if (authToken == null) {
      if (email.trim() === "") {
        alert(t('returnRequestModal.emailError'));
        return;
      }
      // Validación básica de formato de correo
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        alert(t('returnRequestModal.emailRegexError'));
        return;
      }
    }

    try {
      setLoading(true);
      await onSubmit(reason, email);
      setReason("");
      setEmail("");
    } finally {
      setLoading(false);
    }
  };


  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-xl font-semibold mb-4">{t('returnRequestModal.reasonForReturn')}</h2>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {authToken == null && (
            <input
              type="email"
              className="border rounded-md p-2"
              placeholder={t('returnRequestModal.emailPlaceholder')}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          )}
          <textarea
            className="border rounded-md p-2 resize-none"
            rows={4}
            placeholder={t('returnRequestModal.reasonPlaceholder')}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={() => {
                setReason("");
                if (setEmail) setEmail("");
                onClose();
              }}
              className="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400"
            >
              {t('returnRequestModal.cancel')}
            </button>
            <button
              type="submit"
              disabled={loading}
              className={`px-4 py-2 rounded font-semibold flex items-center justify-center gap-2
                ${loading
                  ? "bg-yellow-200 cursor-not-allowed text-gray-600"
                  : "bg-yellow-400 hover:bg-yellow-500 text-black"
                }`}
            >
              {loading ? (
                <>
                  <div className="inline-block animate-spin border-t-2 border-b-2 border-black rounded-full w-5 h-5"></div>
                  {t("returnRequestModal.submitting")}
                </>
              ) : (
                t("returnRequestModal.submit")
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReturnRequestModal;
