// src/utils/authUtils.js
import { jwtDecode } from 'jwt-decode';

export const checkTokenExpiration = () => {
    const token = localStorage.getItem('authToken');
        if (token) {
            const decodedToken = jwtDecode(token);
            const currentTime = Date.now();

            if (decodedToken.exp * 1000 < currentTime) {
                return false;
            }
            return true;
        }
        return true; 
};

