import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useEffect } from 'react';

const LandingPage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();

    // Redirect authenticated users to their dashboard
    useEffect(() => {
        if (user) {
            const dashboardPath = `/${user.role.toLowerCase()}/dashboard`;
            navigate(dashboardPath, { replace: true });
        }
    }, [user, navigate]);

    return (
        <div style={{ position: 'relative', width: '100vw', height: '100vh', overflow: 'hidden' }}>
            {/* Sample-1 Landing Page loaded directly */}
            <iframe
                id="landing-iframe"
                src="/landing/index.html"
                style={{
                    width: '100%',
                    height: '100%',
                    border: 'none',
                    position: 'absolute',
                    top: 0,
                    left: 0
                }}
                title="THE 12%CLUB"
            />
        </div>
    );
};

export default LandingPage;
