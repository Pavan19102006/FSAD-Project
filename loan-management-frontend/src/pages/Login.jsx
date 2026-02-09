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
                    <p style={{ color: 'var(--text-secondary)', marginBottom: '12px' }}>Demo Accounts:</p>
                    <p>Admin: admin@12club.com / admin123</p>
                    <p>Lender: lender@12club.com / lender123</p>
                    <p>Borrower: borrower@12club.com / borrower123</p>
                    <p>Analyst: analyst@12club.com / analyst123</p>
                </div>
            </div>
        </div>
    );
};

export default Login;
