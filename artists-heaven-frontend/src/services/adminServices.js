// services/adminServices.js

const API_BASE = '/api/admin';

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

// Obtener todas las categorías
export const getAllCategories = (authToken) => makeRequest(`${API_BASE}/allCategories`, 'GET', null, authToken);

// Obtener gestión de productos
export const getProductManagement = (authToken) => makeRequest(`${API_BASE}/product-management`, 'GET', null, authToken);

// Obtener todos los productos con paginación y búsqueda
export const getAllProducts = (authToken, page, pageSize, searchTerm = '') => {
    const query = searchTerm ? `&search=${encodeURIComponent(searchTerm)}` : '';
    return makeRequest(`/api/product/allProducts?page=${page}&size=${pageSize}${query}`, 'GET', null, authToken);
};

// Cambiar disponibilidad del producto (habilitar/deshabilitar)
export const toggleProductAvailability = (authToken, id, shouldEnable) => {
    const endpoint = shouldEnable ? 'enable' : 'disable';
    return makeRequest(`/api/admin/${id}/${endpoint}`, 'POST', { id }, authToken);
};

// Degradar el producto
export const demoteProduct = (authToken, id) => makeRequest(`/api/product/demote/${id}`, 'PUT', null, authToken);

// Crear una nueva categoría
export const createCategory = (authToken, name) => {
    const body = { name };
    return makeRequest(`${API_BASE}/newCategory`, 'POST', body, authToken);
};

// Editar una categoría
export const editCategory = (authToken, id, name) => {
    const body = { id, name };
    return makeRequest(`${API_BASE}/editCategory`, 'POST', body, authToken);
};

// Obtener estadísticas del año (como ingresos, etc.)
export const getStatisticsPerYear = (authToken, year) =>
    makeRequest(`/api/admin/staticsPerYear?year=${year}`, 'GET', null, authToken);

// Obtener usuarios con paginación y búsqueda
export const getUsers = (authToken, page, pageSize, searchTerm = '') => {
    const query = searchTerm ? `&search=${encodeURIComponent(searchTerm)}` : '';
    return makeRequest(`/api/admin/users?page=${page}&size=${pageSize}${query}`, 'GET', null, authToken);
};

// Obtener las verificaciones pendientes de artistas
export const getPendingVerifications = (authToken) =>
    makeRequest('/api/admin/verification/pending', 'GET', null, authToken);

// Verificar un artista
export const getVerifyArtist = (authToken, id, verificationId) => {
    return makeRequest('/api/admin/validate_artist', 'POST', { id, verificationId }, authToken);
};

// Rechazar un artista
export const doRefuseArtist = (authToken, verificationId) =>
    makeRequest(`/api/admin/${verificationId}/refuse`, 'POST', null, authToken);




