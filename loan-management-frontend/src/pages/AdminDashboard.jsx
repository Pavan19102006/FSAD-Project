import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WelcomeModal, TutorialModal, useOnboarding } from '../components/Onboarding';

const AdminDashboard = () => {
    const { user } = useAuth();
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const {
        showWelcome,
        showTutorial,
        tutorialStep,
        totalSteps,
        checkFirstTimeUser,
        startTutorial,
        closeWelcome,
        closeTutorial,
        nextStep,
        prevStep,
        resetTutorial
    } = useOnboarding(user);

    useEffect(() => {
        checkFirstTimeUser();
    }, [user]);

    useEffect(() => {
        fetchDashboard();
    }, []);

    const fetchDashboard = async () => {
        try {
            const response = await adminAPI.getDashboard();
            setDashboard(response.data.data);
        } catch (err) {
            setError('Failed to load dashboard data');
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(amount || 0);
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading dashboard...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    return (
        <>
            {showWelcome && (
                <WelcomeModal
                    user={user}
                    onClose={closeWelcome}
                    onStartTutorial={startTutorial}
                />
            )}
            {showTutorial && (
                <TutorialModal
                    user={user}
                    currentStep={tutorialStep}
                    totalSteps={totalSteps}
                    onNext={nextStep}
                    onPrev={prevStep}
                    onClose={closeTutorial}
                />
            )}
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">Admin Dashboard</h1>
                    <p className="page-subtitle">Platform overview and management</p>
                </div>

                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Users</div>
                        <div className="stat-value">{dashboard?.totalUsers || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Lenders</div>
                        <div className="stat-value">{dashboard?.totalLenders || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Borrowers</div>
                        <div className="stat-value">{dashboard?.totalBorrowers || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Analysts</div>
                        <div className="stat-value">{dashboard?.totalAnalysts || 0}</div>
                    </div>
                </div>

                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Loans</div>
                        <div className="stat-value">{dashboard?.totalLoans || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active Loans</div>
                        <div className="stat-value">{dashboard?.activeLoans || 0}</div>
                        <div className="stat-change positive">Currently active</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Completed Loans</div>
                        <div className="stat-value">{dashboard?.completedLoans || 0}</div>
                        <div className="stat-change positive">Successfully completed</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Defaulted Loans</div>
                        <div className="stat-value">{dashboard?.defaultedLoans || 0}</div>
                        <div className="stat-change negative">In default</div>
                    </div>
                </div>

                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Loan Amount</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalLoanAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active Loan Value</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalActiveAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Total Interest Earned</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalInterestEarned)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Pending Applications</div>
                        <div className="stat-value">{dashboard?.pendingApplications || 0}</div>
                        <div className="stat-change">Awaiting review</div>
                    </div>
                </div>

                <div className="card" style={{ marginTop: '24px' }}>
                    <div className="card-header">
                        <h3 className="card-title">Quick Actions</h3>
                    </div>
                    <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <Link to="/admin/users" className="btn btn-primary">👥 Manage Users</Link>
                        <Link to="/admin/analytics" className="btn btn-secondary">📈 View Analytics</Link>
                    </div>
                </div>
            </div>
            <button className="help-btn" onClick={resetTutorial} title="Show Tutorial">
                ❓
            </button>
        </>
    );
};

export default AdminDashboard;
