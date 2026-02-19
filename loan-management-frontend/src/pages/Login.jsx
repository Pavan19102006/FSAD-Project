import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const user = await login(email, password);

            // Redirect based on role
            switch (user.role) {
                case 'ADMIN':
                    navigate('/admin');
                    break;
                case 'LENDER':
                    navigate('/lender');
                    break;
                case 'BORROWER':
                    navigate('/borrower');
                    break;
                case 'ANALYST':
                    navigate('/analyst');
                    break;
                default:
                    navigate('/');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card fade-in">
                <div className="auth-logo">
                    <div className="auth-logo-icon">�</div>
                    <h1 className="auth-title">THE 12%CLUB</h1>
                    <p className="auth-subtitle">Sign in to earn 12% returns</p>
                </div>

                {error && (
                    <div className="alert alert-error">
                        <span>⚠️</span>
                        <span>{error}</span>
                    </div>
                )}

                <form className="auth-form" onSubmit={handleSubmit}>
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

                    <button
                        type="submit"
                        className="btn btn-primary"
                        style={{ width: '100%' }}
                        disabled={loading}
                    >
                        {loading ? 'Signing In...' : 'Sign In'}
                    </button>
                </form>

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
                                onClick={() => { setEmail(demo.email); setPassword(demo.password); }}
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
