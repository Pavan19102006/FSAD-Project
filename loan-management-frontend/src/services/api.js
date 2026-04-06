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
    // Password login (with CAPTCHA) - returns JWT directly
    login: (data) => api.post('/auth/login', data),
    // Register - returns otpRequired
    register: (data) => api.post('/auth/register', data),
    refresh: (refreshToken) => api.post(`/auth/refresh?refreshToken=${refreshToken}`),
    // CAPTCHA
    getCaptcha: () => api.get('/auth/captcha'),
    // OTP-based login
    sendLoginOtp: (email) => api.post('/auth/send-login-otp', { email }),
    // OTP verification (for both registration and OTP login)
    verifyOtp: (data) => api.post('/auth/verify-otp', data),
    resendOtp: (data) => api.post('/auth/resend-otp', data),
    // Google OAuth
    googleAuth: (idToken) => api.post('/auth/google', { idToken }),
};

// Admin APIs
export const adminAPI = {
    getDashboard: () => api.get('/admin/dashboard'),
    getTrends: () => api.get('/admin/trends'),
    getUsers: () => api.get('/admin/users'),
    getUsersByRole: (role) => api.get(`/admin/users/role/${role}`),
    getUserDetails: (id) => api.get(`/admin/users/${id}`),
    updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
    deleteUser: (id) => api.delete(`/admin/users/${id}`),
    toggleUserStatus: (id) => api.patch(`/admin/users/${id}/toggle-status`),
    getUserStats: () => api.get('/admin/users/stats'),
    getCreditScore: (userId) => api.get(`/admin/users/${userId}/credit-score`),
    updateCreditScore: (userId, data) => api.put(`/admin/users/${userId}/credit-score`, data),
    calculateRisk: (userId) => api.post(`/risk/calculate/${userId}`),
};

// Profile API (self-update)
export const profileAPI = {
    updateProfile: (data) => api.put('/auth/profile', data),
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
    getCreditScore: () => api.get('/borrower/credit-score'),
    updateCreditScore: (data) => api.put('/borrower/credit-score', data),
    calculateCreditScore: (data) => api.post('/borrower/credit-score/calculate', data),
};

// Analyst APIs
export const analystAPI = {
    getLoanAnalytics: () => api.get('/analyst/reports/loans'),
    getRiskAssessment: () => api.get('/analyst/reports/risk'),
    getPaymentAnalytics: () => api.get('/analyst/reports/payments'),
    getTrends: () => api.get('/analyst/reports/trends'),
    getAllLoans: () => api.get('/analyst/loans'),
    getLoan: (id) => api.get(`/analyst/loans/${id}`),
    getLoanPayments: (id) => api.get(`/analyst/loans/${id}/payments`),
    getOverduePayments: () => api.get('/analyst/overdue-payments'),
};

export default api;
