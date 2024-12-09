// src/utils/authUtils.js
import { jwtDecode } from 'jwt-decode';

export const checkTokenExpiration = () => {
    const token = localStorage.getItem('authToken');
    if (token) {
        const decodedToken = jwtDecode(token);
        const currentTime = Date.now();

        if (decodedToken.exp * 1000 < currentTime) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('userEmail');
            localStorage.removeItem('role');
            window.location.href = '/auth/login';
            return false;
        }
        return true;
    } else{
        window.location.href = '/auth/login';
        return false;
    }
};
