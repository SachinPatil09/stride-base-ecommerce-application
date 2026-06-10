import axios from 'axios';

// Base URL points to Spring Boot backend
const API = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — automatically attach JWT token to every request
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — if 401 (token expired), redirect to login
API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ── Auth APIs ──────────────────────────────────────────
export const register = (data) => API.post('/auth/register', data);
export const login    = (data) => API.post('/auth/login',    data);

// ── Product APIs ───────────────────────────────────────
export const getProducts      = (params) => API.get('/products', { params });
export const getProduct       = (id)     => API.get(`/products/${id}`);
export const createProduct    = (data)   => API.post('/admin/products', data);
export const updateProduct    = (id, data) => API.put(`/admin/products/${id}`, data);
export const deleteProduct    = (id)     => API.delete(`/admin/products/${id}`);
export const getAdminProducts = ()       => API.get('/admin/products');

// ── Cart APIs ──────────────────────────────────────────
export const getCart        = ()         => API.get('/cart');
export const addToCart      = (data)     => API.post('/cart/add', data);
export const removeFromCart = (itemId)   => API.delete(`/cart/remove/${itemId}`);

// ── Order APIs ─────────────────────────────────────────
export const placeOrder       = ()       => API.post('/orders');
export const getMyOrders      = ()       => API.get('/orders');
export const getAdminOrders   = ()       => API.get('/admin/orders');
export const updateOrderStatus= (id, status) => API.put(`/admin/orders/${id}/status`, { status });

export default API;
