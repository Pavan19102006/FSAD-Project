import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }
        setLoading(false);
    }, []);

    /**
     * Password login (with CAPTCHA) - returns JWT directly
     */
    const login = async (email, password, captchaId, captchaAnswer) => {
        const response = await authAPI.login({ email, password, captchaId, captchaAnswer });
        const { data } = response.data;

        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify(data));
        setUser(data);
        return data;
    };

    /**
     * Register - sends OTP, user must verify before getting access
     */
    const register = async (userData) => {
        const response = await authAPI.register(userData);
        const { data } = response.data;

        // Registration requires OTP verification
        return {
            otpRequired: true,
            email: data.email,
            maskedEmail: data.maskedEmail,
            isRegistration: true,
        };
    };

    /**
     * Request OTP for passwordless login
     */
    const sendLoginOtp = async (email) => {
        const response = await authAPI.sendLoginOtp(email);
        const { data } = response.data;
        return data;
    };

    /**
     * Verify OTP - completes registration or OTP-based login
     */
    const verifyOtp = async (email, otp) => {
        const response = await authAPI.verifyOtp({ email, otp });
        const { data } = response.data;

        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify(data));
        setUser(data);

        return data;
    };

    const resendOtp = async (email) => {
        const response = await authAPI.resendOtp({ email });
        return response.data;
    };

    const googleLogin = async (idToken) => {
        const response = await authAPI.googleAuth(idToken);
        const { data } = response.data;

        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify(data));
        setUser(data);

        return data;
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        setUser(null);
    };

    const value = {
        user,
        loading,
        login,
        register,
        sendLoginOtp,
        verifyOtp,
        resendOtp,
        googleLogin,
        logout,
        isAuthenticated: !!user,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
