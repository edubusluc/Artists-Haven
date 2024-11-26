import React from "react";
import { useNavigate } from "react-router-dom";

const Logout = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Eliminar el token y el correo del localStorage
    localStorage.removeItem("authToken");
    localStorage.removeItem("userEmail");

    // Redirigir al usuario a la página de inicio (o login)
    navigate("/");  // Puedes redirigir a la página que desees
    window.location.reload();
  };

  return (
    <div>
      <button onClick={handleLogout}>Cerrar Sesión</button>
    </div>
  );
};

export default Logout;
