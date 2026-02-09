import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Sidebar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getNavItems = () => {
        const baseRolePath = user?.role?.toLowerCase();
        const profileItem = { icon: '👤', label: 'Profile', path: `/${baseRolePath}/profile` };

        switch (user?.role) {
            case 'ADMIN':
                return [
                    { icon: '📊', label: 'Dashboard', path: '/admin' },
                    { icon: '👥', label: 'Users', path: '/admin/users' },
                    { icon: '📈', label: 'Analytics', path: '/admin/analytics' },
                    profileItem,
                ];
            case 'LENDER':
                return [
                    { icon: '📊', label: 'Dashboard', path: '/lender' },
                    { icon: '💰', label: 'My Loans', path: '/lender/loans' },
                    { icon: '📋', label: 'Loan Applications', path: '/lender/applications' },
                    { icon: '💳', label: 'Payment Approvals', path: '/lender/payments' },
                    { icon: '➕', label: 'Create Loan', path: '/lender/create' },
                    { icon: '💸', label: 'Withdraw Funds', path: '/lender/withdraw' },
                    profileItem,
                ];
            case 'BORROWER':
                return [
                    { icon: '📊', label: 'Dashboard', path: '/borrower' },
                    { icon: '💳', label: 'My Loans', path: '/borrower/loans' },
                    { icon: '📝', label: 'My Applications', path: '/borrower/applications' },
                    { icon: '💵', label: 'Payments', path: '/borrower/payments' },
                    { icon: '🔍', label: 'Browse Offers', path: '/borrower/offers' },
                    { icon: '📈', label: 'Credit Score', path: '/borrower/credit-score' },
                    profileItem,
                ];
            case 'ANALYST':
                return [
                    { icon: '📊', label: 'Loan Analytics', path: '/analyst' },
                    { icon: '⚠️', label: 'Risk Assessment', path: '/analyst/risk' },
                    { icon: '💸', label: 'Payment Reports', path: '/analyst/payments' },
                    { icon: '📋', label: 'All Loans', path: '/analyst/loans' },
                    profileItem,
                ];
            default:
                return [];
        }
    };

    return (
        <aside className="sidebar">
            <div className="sidebar-logo">
                <div className="sidebar-logo-icon">�</div>
                <div className="sidebar-logo-text">THE 12%CLUB</div>
            </div>

            <nav className="sidebar-nav">
                {getNavItems().map((item, index) => (
                    <Link
                        key={index}
                        to={item.path}
                        className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
                    >
                        <span className="nav-icon">{item.icon}</span>
                        <span>{item.label}</span>
                    </Link>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <div className="user-avatar">
                        {user?.firstName?.[0]}{user?.lastName?.[0]}
                    </div>
                    <div>
                        <div className="user-name">{user?.firstName} {user?.lastName}</div>
                        <div className="user-role">{user?.role?.toLowerCase()}</div>
                    </div>
                </div>
                <button
                    className="btn btn-secondary"
                    style={{ width: '100%', marginTop: '16px' }}
                    onClick={handleLogout}
                >
                    🚪 Logout
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
