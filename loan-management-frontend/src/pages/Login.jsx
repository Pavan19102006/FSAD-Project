import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'YOUR_GOOGLE_CLIENT_ID_HERE';

const Login = () => {
    const [loginMode, setLoginMode] = useState('password'); // 'password' or 'otp'
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [captchaAnswer, setCaptchaAnswer] = useState('');
    const [captchaData, setCaptchaData] = useState(null);
    const [captchaLoading, setCaptchaLoading] = useState(false);
    const [otpSent, setOtpSent] = useState(false);
    const [otpEmail, setOtpEmail] = useState('');
    const [maskedEmail, setMaskedEmail] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login, sendLoginOtp, googleLogin } = useAuth();
    const navigate = useNavigate();

    const fetchCaptcha = useCallback(async () => {
        setCaptchaLoading(true);
        setCaptchaAnswer('');
        try {
            const response = await authAPI.getCaptcha();
            setCaptchaData(response.data.data);
        } catch (err) {
            console.error('Failed to load CAPTCHA');
        } finally {
            setCaptchaLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchCaptcha();
    }, [fetchCaptcha]);

    // Initialize Google Sign-In
    useEffect(() => {
        if (GOOGLE_CLIENT_ID === 'YOUR_GOOGLE_CLIENT_ID_HERE') return;

        const initGoogle = () => {
            if (window.google?.accounts?.id) {
                window.google.accounts.id.initialize({
                    client_id: GOOGLE_CLIENT_ID,
                    callback: handleGoogleResponse,
                });
                window.google.accounts.id.renderButton(
                    document.getElementById('google-signin-btn-login'),
                    { theme: 'outline', size: 'large', width: '100%', text: 'signin_with', shape: 'rectangular' }
                );
            }
        };

        if (window.google?.accounts?.id) {
            initGoogle();
        } else {
            const interval = setInterval(() => {
                if (window.google?.accounts?.id) { initGoogle(); clearInterval(interval); }
            }, 200);
            return () => clearInterval(interval);
        }
    }, []);

    const handleGoogleResponse = async (response) => {
        setError('');
        setLoading(true);
        try {
            const user = await googleLogin(response.credential);
            navigateByRole(user.role);
        } catch (err) {
            setError(err.response?.data?.message || 'Google sign-in failed');
        } finally {
            setLoading(false);
        }
    };

    const navigateByRole = (role) => {
        switch (role) {
            case 'ADMIN': navigate('/admin'); break;
            case 'LENDER': navigate('/lender'); break;
            case 'BORROWER': navigate('/borrower'); break;
            case 'ANALYST': navigate('/analyst'); break;
            default: navigate('/');
        }
    };

    // Password login handler
    const handlePasswordLogin = async (e) => {
        e.preventDefault();
        setError('');

        if (!captchaAnswer.trim()) {
            setError('Please solve the CAPTCHA');
            return;
        }

        setLoading(true);
        try {
            const user = await login(email, password, captchaData?.captchaId, captchaAnswer);
            navigateByRole(user.role);
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed. Please try again.');
            fetchCaptcha();
        } finally {
            setLoading(false);
        }
    };

    // OTP login - send OTP
    const handleSendOtp = async (e) => {
        e.preventDefault();
        setError('');

        if (!email.trim()) {
            setError('Please enter your email');
            return;
        }

        setLoading(true);
        try {
            const result = await sendLoginOtp(email);
            setOtpSent(true);
            setOtpEmail(result.email);
            setMaskedEmail(result.maskedEmail);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to send OTP');
        } finally {
            setLoading(false);
        }
    };

    // When OTP is sent, redirect to OTP verification page
    useEffect(() => {
        if (otpSent && otpEmail) {
            navigate('/verify-otp', {
                state: { email: otpEmail, maskedEmail: maskedEmail }
            });
        }
    }, [otpSent, otpEmail, maskedEmail, navigate]);

    return (
        <div className="auth-container">
            <div className="auth-card fade-in">
                <div className="auth-logo">
                    <div className="auth-logo-icon">🏦</div>
                    <h1 className="auth-title">THE 12%CLUB</h1>
                    <p className="auth-subtitle">Sign in to earn 12% returns</p>
                </div>

                {error && (
                    <div className="alert alert-error">
                        <span>⚠️</span>
                        <span>{error}</span>
                    </div>
                )}

                {/* Login Mode Tabs */}
                <div className="login-tabs">
                    <button
                        className={`login-tab ${loginMode === 'password' ? 'active' : ''}`}
                        onClick={() => { setLoginMode('password'); setError(''); }}
                    >
                        🔑 Password
                    </button>
                    <button
                        className={`login-tab ${loginMode === 'otp' ? 'active' : ''}`}
                        onClick={() => { setLoginMode('otp'); setError(''); }}
                    >
                        📧 Email OTP
                    </button>
                </div>

                {/* PASSWORD LOGIN FORM */}
                {loginMode === 'password' && (
                    <form className="auth-form" onSubmit={handlePasswordLogin}>
                        <div className="form-group">
                            <label className="form-label">Email Address</label>
                            <input
                                type="email"
                                className="form-input"
                                placeholder="Enter your email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Password</label>
                            <input
                                type="password"
                                className="form-input"
                                placeholder="Enter your password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        {/* CAPTCHA */}
                        <div className="form-group">
                            <label className="form-label">Security Check</label>
                            <div className="captcha-container">
                                <div className="captcha-image-wrap">
                                    {captchaLoading ? (
                                        <div className="captcha-loading">
                                            <div className="loading-spinner" style={{ width: 24, height: 24 }}></div>
                                        </div>
                                    ) : captchaData ? (
                                        <img
                                            src={`data:image/png;base64,${captchaData.image}`}
                                            alt="CAPTCHA"
                                            className="captcha-image"
                                        />
                                    ) : (
                                        <div className="captcha-loading">Loading...</div>
                                    )}
                                </div>
                                <button
                                    type="button"
                                    className="captcha-refresh-btn"
                                    onClick={fetchCaptcha}
                                    title="Get new CAPTCHA"
                                    disabled={captchaLoading}
                                >
                                    🔄
                                </button>
                            </div>
                            <input
                                type="text"
                                className="form-input captcha-answer-input"
                                placeholder="Enter the answer"
                                value={captchaAnswer}
                                onChange={(e) => setCaptchaAnswer(e.target.value)}
                                required
                                autoComplete="off"
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary"
                            style={{ width: '100%' }}
                            disabled={loading}
                        >
                            {loading ? 'Signing In...' : 'Sign In'}
                        </button>
                    </form>
                )}

                {/* OTP LOGIN FORM */}
                {loginMode === 'otp' && (
                    <form className="auth-form" onSubmit={handleSendOtp}>
                        <div className="otp-login-info">
                            <p>We'll send a 6-digit verification code to your registered email address.</p>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Email Address</label>
                            <input
                                type="email"
                                className="form-input"
                                placeholder="Enter your registered email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary"
                            style={{ width: '100%' }}
                            disabled={loading}
                        >
                            {loading ? 'Sending OTP...' : 'Send OTP'}
                        </button>
                    </form>
                )}

                {/* Divider */}
                <div className="auth-divider">
                    <span className="auth-divider-text">or continue with</span>
                </div>

                {/* Google Sign-In */}
                {GOOGLE_CLIENT_ID !== 'YOUR_GOOGLE_CLIENT_ID_HERE' ? (
                    <div id="google-signin-btn-login" className="google-btn-container"></div>
                ) : (
                    <button
                        type="button"
                        className="google-signin-btn"
                        onClick={() => setError('Google OAuth is not configured. Set VITE_GOOGLE_CLIENT_ID in your .env file.')}
                    >
                        <svg className="google-icon" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                        </svg>
                        <span>Sign in with Google</span>
                    </button>
                )}

                <div className="auth-footer">
                    <p>Don't have an account? <Link to="/register">Create one</Link></p>
                </div>

                <div style={{ marginTop: '24px', padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)', fontSize: '13px' }}>
                    <p style={{ color: 'var(--text-secondary)', marginBottom: '12px', textAlign: 'center', fontWeight: '600' }}>Quick Demo Login</p>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                        {[
                            { role: '🛡️ Admin', email: 'admin@12club.com', password: 'admin123', color: '#ef4444' },
                            { role: '💰 Lender', email: 'lender@12club.com', password: 'lender123', color: '#3b82f6' },
                            { role: '🏦 Borrower', email: 'borrower@12club.com', password: 'borrower123', color: '#10b981' },
                            { role: '📊 Analyst', email: 'analyst@12club.com', password: 'analyst123', color: '#f59e0b' },
                        ].map((demo) => (
                            <button
                                key={demo.role}
                                type="button"
                                onClick={() => { setEmail(demo.email); setPassword(demo.password); setLoginMode('password'); }}
                                style={{
                                    padding: '10px 12px',
                                    background: `${demo.color}15`,
                                    border: `1px solid ${demo.color}40`,
                                    borderRadius: '8px',
                                    color: demo.color,
                                    cursor: 'pointer',
                                    fontSize: '13px',
                                    fontWeight: '600',
                                    transition: 'all 0.2s',
                                }}
                                onMouseEnter={(e) => { e.target.style.background = `${demo.color}30`; e.target.style.transform = 'scale(1.02)'; }}
                                onMouseLeave={(e) => { e.target.style.background = `${demo.color}15`; e.target.style.transform = 'scale(1)'; }}
                            >
                                {demo.role}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
