// services/adminServices.js

const API_BASE = 'http://localhost:8080/api/admin';

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

    // Función para parsear el body según si es JSON o texto plano
    const parseBody = async (response) => {
        const text = await response.text();
        try {
            return JSON.parse(text);
        } catch {
            return { message: text };
        }
    };

    const data = await parseBody(res);

    if (!res.ok) {
        const errorMessage = data.message || data.error || 'Unknown error';
        throw new Error(errorMessage);
    }

    return data;

};


// Obtener todas las categorías
export const getAllCategories = (authToken) => makeRequest(`http://localhost:8080/api/product/categories`, 'GET', null, authToken);

// Obtner todas las colecciones
export const getAllCollections = (authToken) => makeRequest(`http://localhost:8080/api/product/allCollections`, 'GET', null, authToken);

// Obtener gestión de productos
export const getProductManagement = (authToken) => makeRequest(`${API_BASE}/product-management`, 'GET', null, authToken);

// Obtener todos los productos con paginación y búsqueda
export const getAllProducts = (
    authToken,
    page,
    pageSize,
    searchTerm = '',
    available = null,
    promoted = null
) => {
    const params = new URLSearchParams();
    params.append("page", page);
    params.append("size", pageSize);

    if (searchTerm) params.append("search", searchTerm);
    if (available !== null) params.append("available", available);
    if (promoted !== null) params.append("promoted", promoted);

    return makeRequest(
        `http://localhost:8080/api/product/allProducts?${params.toString()}`,
        'GET',
        null,
        authToken
    );
};

// Cambiar disponibilidad del producto (habilitar/deshabilitar)
export const toggleProductAvailability = (authToken, id, shouldEnable) => {
    const endpoint = shouldEnable ? 'enable' : 'disable';
    return makeRequest(`http://localhost:8080/api/admin/${id}/${endpoint}`, 'POST', { id }, authToken);
};

// Degradar el producto
export const demoteProduct = (authToken, id, language) => makeRequest(`http://localhost:8080/api/product/demote/${id}?lang=${language}`, 'PUT', null, authToken);

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

// Crear una nueva collecion
export const createCollection = (authToken, name) => {
    const body = { name };
    return makeRequest(`${API_BASE}/newCollection`, 'POST', body, authToken);
};

// Editar una collecion
export const editCollection = (authToken, id, name, isPromoted) => {
    const body = { id, name, isPromoted };
    return makeRequest(`${API_BASE}/editCollection`, 'POST', body, authToken);
};

// Obtener estadísticas del año (como ingresos, etc.)
export const getStatisticsPerYear = (authToken, year) =>
    makeRequest(`http://localhost:8080/api/admin/staticsPerYear?year=${year}`, 'GET', null, authToken);

// Obtener usuarios con paginación y búsqueda
export const getUsers = (authToken, page, pageSize, searchTerm = '') => {
    const query = searchTerm ? `&search=${encodeURIComponent(searchTerm)}` : '';
    return makeRequest(`http://localhost:8080/api/admin/users?page=${page}&size=${pageSize}${query}`, 'GET', null, authToken);
};

// Obtener las verificaciones pendientes de artistas
export const getPendingVerifications = (authToken) =>
    makeRequest('http://localhost:8080/api/admin/verification/pending', 'GET', null, authToken);

export const getPendingUserProducts = (authToken) =>
    makeRequest('http://localhost:8080/api/admin/userProduct/pending', 'GET', null, authToken);

export const approveUserProduct = (authToken, productId) =>
    makeRequest(`http://localhost:8080/api/admin/userProduct/${productId}/approve`, 'POST', null, authToken);

export const rejectUserProduct = (authToken, productId) =>
    makeRequest(`http://localhost:8080/api/admin/userProduct/${productId}/reject`, 'POST', null, authToken);

// Verificar un artista
export const getVerifyArtist = (authToken, id, verificationId) => {
    return makeRequest('http://localhost:8080/api/admin/validate_artist', 'POST', { id, verificationId }, authToken);
};

// Rechazar un artista
export const doRefuseArtist = (authToken, verificationId) =>
    makeRequest(`http://localhost:8080/api/admin/${verificationId}/refuse`, 'POST', null, authToken);

// Función para obtener lor pedidos
export const getOrders = (authToken, page, pageSize, status, search) => {
    let url = `http://localhost:8080/api/admin/orders?page=${page}&size=${pageSize}`;

    if (status) {
        url += `&status=${encodeURIComponent(status)}`;
    }
    if (search) {
        url += `&search=${encodeURIComponent(search)}`;
    }

    return makeRequest(url, 'GET', null, authToken);
};

//Función para actualizar el estado de un pedido
export const updateOrderStatus = (authToken, orderId, newStatus) => {
    return makeRequest('http://localhost:8080/api/admin/updateStatus', 'POST', { orderId, status: newStatus }, authToken);
};



