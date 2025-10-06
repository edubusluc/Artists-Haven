import { useAuth } from "./useAuth";
import { useTranslation } from "react-i18next";

const Logout = () => {
  const { logout } = useAuth();
  const {t} = useTranslation();

  const handleLogout = () => {
    logout();
    window.location.reload();
  };

  return (
    <div>
      <button onClick={handleLogout} className="w-full rounded-lgtransition text-left">
        {t('logout')}
      </button>
    </div>
  );
};

export default Logout;
