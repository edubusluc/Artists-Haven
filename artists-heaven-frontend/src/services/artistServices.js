const API_BASE = 'http://localhost:8080/api/artists';

// Función auxiliar para hacer peticiones fetch
const makeRequest = async (url, method = 'GET', body = null, authToken) => {
    const res = await fetch(url, {
        method,
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': body ? 'application/json' : undefined,
        },
        body: body ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
        const message = await res.text();
        throw new Error(message || 'Error en la petición');
    }

    return res.json();
};

export const getArtistDashboardStatistics = (authToken, year) => {
    return makeRequest(`${API_BASE}/dashboard?year=${year}`, 'GET', null, authToken);
};