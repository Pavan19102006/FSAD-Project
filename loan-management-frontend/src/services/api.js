import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add token to requests
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle token expiration
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Auth APIs
export const authAPI = {
    login: (data) => api.post('/auth/login', data),
    register: (data) => api.post('/auth/register', data),
    refresh: (refreshToken) => api.post(`/auth/refresh?refreshToken=${refreshToken}`),
};

// Admin APIs
export const adminAPI = {
    getDashboard: () => api.get('/admin/dashboard'),
    getUsers: () => api.get('/admin/users'),
    getUsersByRole: (role) => api.get(`/admin/users/role/${role}`),
    getUserDetails: (id) => api.get(`/admin/users/${id}`),
    updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
    deleteUser: (id) => api.delete(`/admin/users/${id}`),
    toggleUserStatus: (id) => api.patch(`/admin/users/${id}/toggle-status`),
    getUserStats: () => api.get('/admin/users/stats'),
    // Credit score management
    getCreditScore: (userId) => api.get(`/admin/users/${userId}/credit-score`),
    updateCreditScore: (userId, data) => api.put(`/admin/users/${userId}/credit-score`, data),
    calculateRisk: (userId) => api.post(`/admin/users/${userId}/calculate-risk`),
};

// Lender APIs
export const lenderAPI = {
    getDashboard: () => api.get('/lender/dashboard'),
    createLoan: (data) => api.post('/lender/loans', data),
    getLoans: () => api.get('/lender/loans'),
    getLoan: (id) => api.get(`/lender/loans/${id}`),
    getApplications: () => api.get('/lender/applications'),
    approveApplication: (id) => api.post(`/lender/applications/${id}/approve`),
    rejectApplication: (id, reason) => api.post(`/lender/applications/${id}/reject?reason=${reason}`),
    withdrawFunds: (data) => api.post('/lender/withdraw', data),
    // Payment approval APIs
    getPendingPayments: () => api.get('/lender/payments/pending'),
    approvePayment: (id) => api.post(`/lender/payments/${id}/approve`),
    rejectPayment: (id, reason) => api.post(`/lender/payments/${id}/reject?reason=${reason || ''}`),
};

// Borrower APIs
export const borrowerAPI = {
    getDashboard: () => api.get('/borrower/dashboard'),
    getLoanOffers: () => api.get('/borrower/loan-offers'),
    submitApplication: (data) => api.post('/borrower/applications', data),
    getApplications: () => api.get('/borrower/applications'),
    getLoans: () => api.get('/borrower/loans'),
    getLoan: (id) => api.get(`/borrower/loans/${id}`),
    getPaymentSchedule: (id) => api.get(`/borrower/loans/${id}/schedule`),
    getPayments: () => api.get('/borrower/payments'),
    makePayment: (data) => api.post('/borrower/payments', data),
    markPaymentAsPaid: (id, transactionRef) => api.post(`/borrower/payments/${id}/mark-paid?transactionReference=${transactionRef || ''}`),
    // Credit Score APIs
    getCreditScore: () => api.get('/borrower/credit-score'),
    updateCreditScore: (data) => api.put('/borrower/credit-score', data),
    calculateCreditScore: (data) => api.post('/borrower/credit-score/calculate', data),
};

// Analyst APIs
export const analystAPI = {
    getLoanAnalytics: () => api.get('/analyst/reports/loans'),
    getRiskAssessment: () => api.get('/analyst/reports/risk'),
    getPaymentAnalytics: () => api.get('/analyst/reports/payments'),
    getAllLoans: () => api.get('/analyst/loans'),
    getLoan: (id) => api.get(`/analyst/loans/${id}`),
    getLoanPayments: (id) => api.get(`/analyst/loans/${id}/payments`),
    getOverduePayments: () => api.get('/analyst/overdue-payments'),
};

export default api;
