import { useState } from "react";
import { useNavigate } from "react-router-dom";

export function useAuth() {
  const navigate = useNavigate();
  const [isRefreshing, setIsRefreshing] = useState(false);

  function logout() {
    localStorage.removeItem("authToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("role");
    localStorage.removeItem("shoppingCart");
    navigate("/");
  }

  async function fetchWithAuth(url, options = {}) {
    let token = localStorage.getItem("authToken");
    if (!options.headers) options.headers = {};
    options.headers["Authorization"] = `Bearer ${token}`;

    let response = await fetch(url, options);

    if (response.status === 401) {
      const refreshToken = localStorage.getItem("refreshToken");
      if (!refreshToken) {
        logout();
        throw new Error("Sesión expirada. Por favor inicia sesión de nuevo.");
      }

      setIsRefreshing(true);

      const refreshResponse = await fetch("/api/auth/refresh-token", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });

      setIsRefreshing(false);

      if (!refreshResponse.ok) {
        logout();
        throw new Error("Sesión expirada. Por favor inicia sesión de nuevo.");
      }

      const refreshData = await refreshResponse.json();
      localStorage.setItem("authToken", refreshData.accessToken);

      // Reintenta la petición original con el nuevo token
      options.headers["Authorization"] = `Bearer ${refreshData.accessToken}`;
      response = await fetch(url, options);
    }

    return response;
  }

  return {
    fetchWithAuth,
    logout,
  };
}
