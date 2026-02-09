import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { lenderAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WelcomeModal, TutorialModal, useOnboarding } from '../components/Onboarding';

const LenderDashboard = () => {
    const { user } = useAuth();
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showWithdrawModal, setShowWithdrawModal] = useState(false);
    const [withdrawAmount, setWithdrawAmount] = useState('');
    const [withdrawLoading, setWithdrawLoading] = useState(false);

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
            const response = await lenderAPI.getDashboard();
            setDashboard(response.data.data);
        } catch (err) {
            setError('Failed to load dashboard data');
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
        }).format(amount || 0);
    };

    const handleWithdraw = async () => {
        if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
            alert('Please enter a valid amount');
            return;
        }
        
        const availableBalance = (dashboard?.amountReclaimed || 0) + (dashboard?.totalInterestEarned || 0);
        if (parseFloat(withdrawAmount) > availableBalance) {
            alert('Insufficient balance for withdrawal');
            return;
        }

        setWithdrawLoading(true);
        try {
            await lenderAPI.withdrawFunds({ amount: parseFloat(withdrawAmount) });
            alert('Withdrawal request submitted successfully!');
            setShowWithdrawModal(false);
            setWithdrawAmount('');
            fetchDashboard();
        } catch (err) {
            alert(err.response?.data?.message || 'Withdrawal failed. Please try again.');
        } finally {
            setWithdrawLoading(false);
        }
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

    const availableBalance = (dashboard?.amountReclaimed || 0) + (dashboard?.totalInterestEarned || 0);

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
            
            {/* Withdraw Modal */}
            {showWithdrawModal && (
                <div className="modal-overlay" onClick={() => setShowWithdrawModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>💸 Withdraw Funds</h2>
                            <button className="modal-close" onClick={() => setShowWithdrawModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ marginBottom: '20px', padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                                <p style={{ color: 'var(--text-secondary)', marginBottom: '8px' }}>Available Balance</p>
                                <p style={{ fontSize: '24px', fontWeight: '700', color: 'var(--accent-primary)' }}>
                                    {formatCurrency(availableBalance)}
                                </p>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Withdrawal Amount (₹)</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    placeholder="Enter amount"
                                    value={withdrawAmount}
                                    onChange={(e) => setWithdrawAmount(e.target.value)}
                                    max={availableBalance}
                                    min="1"
                                />
                            </div>
                            <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                                <button 
                                    className="btn btn-secondary" 
                                    style={{ flex: 1 }}
                                    onClick={() => setShowWithdrawModal(false)}
                                >
                                    Cancel
                                </button>
                                <button 
                                    className="btn btn-primary" 
                                    style={{ flex: 1 }}
                                    onClick={handleWithdraw}
                                    disabled={withdrawLoading}
                                >
                                    {withdrawLoading ? 'Processing...' : 'Withdraw'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">📈 THE 12%CLUB Dashboard</h1>
                    <p className="page-subtitle">Earn up to 12% returns on your investments</p>
                </div>

                {/* Investment Overview Cards */}
                <div className="stats-grid">
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
                        <div className="stat-label" style={{ color: 'rgba(255,255,255,0.8)' }}>💰 Amount Invested</div>
                        <div className="stat-value" style={{ color: '#fff' }}>{formatCurrency(dashboard?.totalLentAmount)}</div>
                        <div className="stat-change" style={{ color: 'rgba(255,255,255,0.7)' }}>Total capital deployed</div>
                    </div>
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }}>
                        <div className="stat-label" style={{ color: 'rgba(255,255,255,0.8)' }}>📤 Total Distributed</div>
                        <div className="stat-value" style={{ color: '#fff' }}>{formatCurrency(dashboard?.totalDistributed)}</div>
                        <div className="stat-change" style={{ color: 'rgba(255,255,255,0.7)' }}>Across {dashboard?.activeLoans || 0} active loans</div>
                    </div>
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }}>
                        <div className="stat-label" style={{ color: 'rgba(255,255,255,0.8)' }}>📥 Amount Reclaimed</div>
                        <div className="stat-value" style={{ color: '#fff' }}>{formatCurrency(dashboard?.amountReclaimed)}</div>
                        <div className="stat-change" style={{ color: 'rgba(255,255,255,0.7)' }}>Principal returned</div>
                    </div>
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }}>
                        <div className="stat-label" style={{ color: 'rgba(255,255,255,0.8)' }}>🎯 Total Interest Earned</div>
                        <div className="stat-value" style={{ color: '#fff' }}>{formatCurrency(dashboard?.totalInterestEarned)}</div>
                        <div className="stat-change" style={{ color: 'rgba(255,255,255,0.7)' }}>~12% annual returns</div>
                    </div>
                </div>

                {/* Loan Stats Cards */}
                <div className="stats-grid" style={{ marginTop: '24px' }}>
                    <div className="stat-card">
                        <div className="stat-label">Total Loans Created</div>
                        <div className="stat-value">{dashboard?.totalLoansCreated || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active Loans</div>
                        <div className="stat-value">{dashboard?.activeLoans || 0}</div>
                        <div className="stat-change positive">Generating returns</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Completed Loans</div>
                        <div className="stat-value">{dashboard?.completedLoans || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Pending Offers</div>
                        <div className="stat-value">{dashboard?.pendingOffers || 0}</div>
                        <div className="stat-change">Awaiting borrowers</div>
                    </div>
                </div>

                {/* Available Balance & Withdraw */}
                <div className="card" style={{ marginTop: '24px', background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)', border: '1px solid var(--accent-primary)' }}>
                    <div className="card-header">
                        <h3 className="card-title">💳 Available Balance</h3>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
                        <div>
                            <p style={{ fontSize: '32px', fontWeight: '700', color: 'var(--accent-primary)' }}>
                                {formatCurrency(availableBalance)}
                            </p>
                            <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
                                Reclaimed: {formatCurrency(dashboard?.amountReclaimed)} + Interest: {formatCurrency(dashboard?.totalInterestEarned)}
                            </p>
                        </div>
                        <button 
                            className="btn btn-primary" 
                            style={{ fontSize: '16px', padding: '12px 32px' }}
                            onClick={() => setShowWithdrawModal(true)}
                        >
                            💸 Withdraw Funds
                        </button>
                    </div>
                </div>

                {/* Quick Actions */}
                <div className="card" style={{ marginTop: '24px' }}>
                    <div className="card-header">
                        <h3 className="card-title">⚡ Quick Actions</h3>
                    </div>
                    <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <Link to="/lender/create" className="btn btn-primary">➕ Create New Loan</Link>
                        <Link to="/lender/applications" className="btn btn-secondary">📋 View Applications</Link>
                        <Link to="/lender/loans" className="btn btn-secondary">💰 My Loans</Link>
                        <button className="btn btn-secondary" onClick={() => setShowWithdrawModal(true)}>💸 Withdraw</button>
                    </div>
                </div>
            </div>
            <button className="help-btn" onClick={resetTutorial} title="Show Tutorial">
                ❓
            </button>
        </>
    );
};

export default LenderDashboard;
