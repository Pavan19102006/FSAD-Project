import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'YOUR_GOOGLE_CLIENT_ID_HERE';

const Register = () => {
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        phoneNumber: '',
        role: 'BORROWER',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { register, googleLogin } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
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
                    document.getElementById('google-signup-btn-register'),
                    { theme: 'outline', size: 'large', width: '100%', text: 'signup_with', shape: 'rectangular' }
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
            setError(err.response?.data?.message || 'Google sign-up failed');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const result = await register(formData);

            if (result.otpRequired) {
                // Redirect to OTP verification
                navigate('/verify-otp', {
                    state: {
                        email: result.email,
                        maskedEmail: result.maskedEmail,
                        isRegistration: true,
                    }
                });
            } else {
                navigateByRole(result.role);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card fade-in">
                <div className="auth-logo">
                    <div className="auth-logo-icon">💰</div>
                    <h1 className="auth-title">Create Account</h1>
                    <p className="auth-subtitle">Join our loan management platform</p>
                </div>

                {error && (
                    <div className="alert alert-error">
                        <span>⚠️</span>
                        <span>{error}</span>
                    </div>
                )}

                {/* Google Sign-Up */}
                {GOOGLE_CLIENT_ID !== 'YOUR_GOOGLE_CLIENT_ID_HERE' ? (
                    <div id="google-signup-btn-register" className="google-btn-container" style={{ marginBottom: '8px' }}></div>
                ) : (
                    <button
                        type="button"
                        className="google-signin-btn"
                        style={{ marginBottom: '8px' }}
                        onClick={() => setError('Google OAuth is not configured. Set VITE_GOOGLE_CLIENT_ID in your .env file.')}
                    >
                        <svg className="google-icon" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                        </svg>
                        <span>Sign up with Google</span>
                    </button>
                )}

                <div className="auth-divider">
                    <span className="auth-divider-text">or register with email</span>
                </div>

                <form className="auth-form" onSubmit={handleSubmit} style={{ marginTop: '16px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                        <div className="form-group">
                            <label className="form-label">First Name</label>
                            <input type="text" name="firstName" className="form-input" placeholder="John" value={formData.firstName} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Last Name</label>
                            <input type="text" name="lastName" className="form-input" placeholder="Doe" value={formData.lastName} onChange={handleChange} required />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Email Address</label>
                        <input type="email" name="email" className="form-input" placeholder="john@example.com" value={formData.email} onChange={handleChange} required />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Password</label>
                        <input type="password" name="password" className="form-input" placeholder="Min. 6 characters" value={formData.password} onChange={handleChange} required minLength={6} />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Phone Number</label>
                        <input type="tel" name="phoneNumber" className="form-input" placeholder="+1234567890" value={formData.phoneNumber} onChange={handleChange} />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Role</label>
                        <select name="role" className="form-input form-select" value={formData.role} onChange={handleChange} required>
                            <option value="BORROWER">Borrower</option>
                            <option value="LENDER">Lender</option>
                            <option value="ANALYST">Financial Analyst</option>
                        </select>
                    </div>

                    <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Already have an account? <Link to="/login">Sign in</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Register;
