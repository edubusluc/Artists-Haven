import { useAuth } from "./useAuth";

const Logout = () => {
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
    window.location.reload();
  };

  return (
    <div>
      <button onClick={handleLogout} className="w-full rounded-lgtransition text-left">
        Cerrar Sesión
      </button>
    </div>
  );
};

export default Logout;
