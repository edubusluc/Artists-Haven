// src/utils/authUtils.js
import { jwtDecode } from 'jwt-decode';

export const checkTokenExpiration = () => {
    const token = localStorage.getItem('authToken');
    const firstView = localStorage.getItem('firstTime');
    console.log("EO " + firstView)
    if (firstView === "false") {
        console.log("ENTRE")
        if (token) {
            const decodedToken = jwtDecode(token);
            const currentTime = Date.now();

            if (decodedToken.exp * 1000 < currentTime) {
                localStorage.removeItem('authToken');
                localStorage.removeItem('userEmail');
                localStorage.removeItem('role');
                localStorage.setItem('firstTime', true)
                window.location.href = '/auth/login';
                return false;
            }
            return true;
        } else {
            if (!localStorage.getItem('firstTime') === true) {
                window.location.href = '/auth/login';
            }
            return false;
        }
    }
};
