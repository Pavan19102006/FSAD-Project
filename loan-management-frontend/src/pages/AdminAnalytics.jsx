import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

const AdminAnalytics = () => {
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const response = await adminAPI.getDashboard();
            setDashboard(response.data.data);
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

    const totalLoans = dashboard?.totalLoans || 0;
    const activeLoans = dashboard?.activeLoans || 0;
    const completedLoans = dashboard?.completedLoans || 0;
    const defaultedLoans = dashboard?.defaultedLoans || 0;
    const pendingApplications = dashboard?.pendingApplications || 0;

    // Calculate percentages for visual bars
    const activePercent = totalLoans ? ((activeLoans / totalLoans) * 100).toFixed(1) : 0;
    const completedPercent = totalLoans ? ((completedLoans / totalLoans) * 100).toFixed(1) : 0;
    const defaultPercent = totalLoans ? ((defaultedLoans / totalLoans) * 100).toFixed(1) : 0;

    const totalUsers = dashboard?.totalUsers || 0;
    const totalLenders = dashboard?.totalLenders || 0;
    const totalBorrowers = dashboard?.totalBorrowers || 0;
    const totalAnalysts = dashboard?.totalAnalysts || 0;

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">📈 Platform Analytics</h1>
                <p className="page-subtitle">Comprehensive data insights and performance metrics</p>
            </div>

            {/* Financial Overview */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">💰 Financial Overview</h3>
                </div>
                <div className="stats-grid">
                    <div className="stat-card" style={{ borderLeft: '4px solid #3b82f6' }}>
                        <div className="stat-label">Total Loan Volume</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalLoanAmount)}</div>
                    </div>
                    <div className="stat-card" style={{ borderLeft: '4px solid #10b981' }}>
                        <div className="stat-label">Active Loan Value</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalActiveAmount)}</div>
                    </div>
                    <div className="stat-card" style={{ borderLeft: '4px solid #f59e0b' }}>
                        <div className="stat-label">Total Interest Earned</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalInterestEarned)}</div>
                    </div>
                    <div className="stat-card" style={{ borderLeft: '4px solid #8b5cf6' }}>
                        <div className="stat-label">Avg Loan Amount</div>
                        <div className="stat-value">{formatCurrency(totalLoans ? (dashboard?.totalLoanAmount || 0) / totalLoans : 0)}</div>
                    </div>
                </div>
            </div>

            {/* Loan Distribution */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">📊 Loan Distribution</h3>
                </div>
                <div style={{ padding: '8px 0' }}>
                    <div style={{ marginBottom: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#9ca3af', fontSize: '14px' }}>🟢 Active Loans</span>
                            <span style={{ color: 'white', fontWeight: '600' }}>{activeLoans} ({activePercent}%)</span>
                        </div>
                        <div style={{ height: '12px', background: 'rgba(255,255,255,0.05)', borderRadius: '6px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${activePercent}%`, background: 'linear-gradient(90deg, #10b981, #34d399)', borderRadius: '6px', transition: 'width 1s ease' }}></div>
                        </div>
                    </div>
                    <div style={{ marginBottom: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#9ca3af', fontSize: '14px' }}>🔵 Completed Loans</span>
                            <span style={{ color: 'white', fontWeight: '600' }}>{completedLoans} ({completedPercent}%)</span>
                        </div>
                        <div style={{ height: '12px', background: 'rgba(255,255,255,0.05)', borderRadius: '6px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${completedPercent}%`, background: 'linear-gradient(90deg, #3b82f6, #60a5fa)', borderRadius: '6px', transition: 'width 1s ease' }}></div>
                        </div>
                    </div>
                    <div style={{ marginBottom: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#9ca3af', fontSize: '14px' }}>🔴 Defaulted Loans</span>
                            <span style={{ color: 'white', fontWeight: '600' }}>{defaultedLoans} ({defaultPercent}%)</span>
                        </div>
                        <div style={{ height: '12px', background: 'rgba(255,255,255,0.05)', borderRadius: '6px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${Math.max(defaultPercent, 2)}%`, background: 'linear-gradient(90deg, #ef4444, #f87171)', borderRadius: '6px', transition: 'width 1s ease' }}></div>
                        </div>
                    </div>
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#9ca3af', fontSize: '14px' }}>🟡 Pending Applications</span>
                            <span style={{ color: 'white', fontWeight: '600' }}>{pendingApplications}</span>
                        </div>
                        <div style={{ height: '12px', background: 'rgba(255,255,255,0.05)', borderRadius: '6px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${totalLoans ? Math.max((pendingApplications / totalLoans) * 100, 2) : 5}%`, background: 'linear-gradient(90deg, #f59e0b, #fbbf24)', borderRadius: '6px', transition: 'width 1s ease' }}></div>
                        </div>
                    </div>
                </div>
            </div>

            {/* User Breakdown */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">👥 User Breakdown</h3>
                </div>
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Users</div>
                        <div className="stat-value" style={{ color: '#8b5cf6' }}>{totalUsers}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Lenders</div>
                        <div className="stat-value" style={{ color: '#3b82f6' }}>{totalLenders}</div>
                        <div className="stat-change">{totalUsers ? ((totalLenders / totalUsers) * 100).toFixed(0) : 0}% of users</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Borrowers</div>
                        <div className="stat-value" style={{ color: '#10b981' }}>{totalBorrowers}</div>
                        <div className="stat-change">{totalUsers ? ((totalBorrowers / totalUsers) * 100).toFixed(0) : 0}% of users</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Analysts</div>
                        <div className="stat-value" style={{ color: '#f59e0b' }}>{totalAnalysts}</div>
                        <div className="stat-change">{totalUsers ? ((totalAnalysts / totalUsers) * 100).toFixed(0) : 0}% of users</div>
                    </div>
                </div>
            </div>

            {/* Key Metrics Summary */}
            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">🎯 Key Metrics</h3>
                </div>
                <div style={{ display: 'grid', gap: '16px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <span style={{ color: '#9ca3af' }}>Loan Success Rate</span>
                        <span style={{ color: defaultedLoans === 0 ? '#10b981' : '#f59e0b', fontWeight: '600' }}>
                            {totalLoans ? (((totalLoans - defaultedLoans) / totalLoans) * 100).toFixed(1) : 100}%
                        </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <span style={{ color: '#9ca3af' }}>Default Rate</span>
                        <span style={{ color: defaultPercent > 5 ? '#ef4444' : '#10b981', fontWeight: '600' }}>
                            {defaultPercent}%
                        </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <span style={{ color: '#9ca3af' }}>Borrower-to-Lender Ratio</span>
                        <span style={{ color: 'white', fontWeight: '600' }}>
                            {totalLenders ? (totalBorrowers / totalLenders).toFixed(2) : 'N/A'} : 1
                        </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <span style={{ color: '#9ca3af' }}>Avg Loan Per Lender</span>
                        <span style={{ color: 'white', fontWeight: '600' }}>
                            {formatCurrency(totalLenders ? (dashboard?.totalLoanAmount || 0) / totalLenders : 0)}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminAnalytics;
