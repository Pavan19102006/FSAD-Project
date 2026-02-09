import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { analystAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WelcomeModal, TutorialModal, useOnboarding } from '../components/Onboarding';

const AnalystDashboard = () => {
    const { user } = useAuth();
    const [analytics, setAnalytics] = useState(null);
    const [risk, setRisk] = useState(null);
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
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [analyticsRes, riskRes] = await Promise.all([
                analystAPI.getLoanAnalytics(),
                analystAPI.getRiskAssessment(),
            ]);
            setAnalytics(analyticsRes.data.data);
            setRisk(riskRes.data.data);
        } catch (err) {
            setError('Failed to load analytics data');
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
                <p>Loading analytics...</p>
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
                    <h1 className="page-title">Financial Analytics</h1>
                    <p className="page-subtitle">Analyze loan data and assess risks</p>
                </div>

                <h2 style={{ marginBottom: '16px', fontSize: '18px' }}>Loan Portfolio Overview</h2>
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Average Loan Amount</div>
                        <div className="stat-value">{formatCurrency(analytics?.averageLoanAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Average Interest Rate</div>
                        <div className="stat-value">{analytics?.averageInterestRate?.toFixed(2) || 0}%</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Default Rate</div>
                        <div className="stat-value">{analytics?.defaultRate?.toFixed(2) || 0}%</div>
                        <div className={`stat-change ${analytics?.defaultRate > 5 ? 'negative' : 'positive'}`}>
                            {analytics?.defaultRate > 5 ? 'High risk' : 'Low risk'}
                        </div>
                    </div>
                </div>

                <h2 style={{ marginBottom: '16px', marginTop: '32px', fontSize: '18px' }}>Risk Assessment</h2>
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Overdue Payments</div>
                        <div className="stat-value">{risk?.overduePaymentsCount || 0}</div>
                        <div className="stat-change negative">Require attention</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Total Overdue Amount</div>
                        <div className="stat-value">{formatCurrency(risk?.totalOverdueAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Loans at Risk</div>
                        <div className="stat-value">{risk?.loansAtRisk || 0}</div>
                        <div className="stat-change negative">With missed payments</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Portfolio Risk Score</div>
                        <div className="stat-value">{risk?.portfolioRiskScore?.toFixed(1) || 0}</div>
                        <div className={`stat-change ${risk?.portfolioRiskScore > 20 ? 'negative' : 'positive'}`}>
                            out of 100
                        </div>
                    </div>
                </div>

                <h2 style={{ marginBottom: '16px', marginTop: '32px', fontSize: '18px' }}>Loan Status Distribution</h2>
                <div className="card">
                    <div style={{ display: 'flex', gap: '24px', flexWrap: 'wrap' }}>
                        {analytics?.statusDistribution && Object.entries(analytics.statusDistribution).map(([status, count]) => (
                            <div key={status} style={{ flex: '1', minWidth: '120px' }}>
                                <div style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                                    {status.replace('_', ' ')}
                                </div>
                                <div style={{ fontSize: '24px', fontWeight: '700' }}>{count}</div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="card" style={{ marginTop: '24px' }}>
                    <div className="card-header">
                        <h3 className="card-title">Quick Actions</h3>
                    </div>
                    <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <Link to="/analyst/loans" className="btn btn-primary">📋 View All Loans</Link>
                        <Link to="/analyst/risk" className="btn btn-secondary">⚠️ Risk Report</Link>
                        <Link to="/analyst/payments" className="btn btn-secondary">💸 Payment Analysis</Link>
                    </div>
                </div>
            </div>
            <button className="help-btn" onClick={resetTutorial} title="Show Tutorial">
                ❓
            </button>
        </>
    );
};

export default AnalystDashboard;
