import { useState, useEffect } from 'react';
import { analystAPI } from '../services/api';

const AnalystPayments = () => {
    const [payments, setPayments] = useState(null);
    const [overduePayments, setOverduePayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [paymentsRes, overdueRes] = await Promise.all([
                analystAPI.getPaymentAnalytics(),
                analystAPI.getOverduePayments(),
            ]);
            setPayments(paymentsRes.data.data);
            setOverduePayments(overdueRes.data.data || []);
        } catch (err) {
            setError('Failed to load payment reports');
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
                <p>Loading payment reports...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    const totalPayments = payments?.totalPayments || 0;
    const completedPayments = payments?.completedPayments || 0;
    const pendingPayments = payments?.pendingPayments || 0;
    const completionRate = totalPayments ? ((completedPayments / totalPayments) * 100).toFixed(1) : 0;

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">💸 Payment Reports</h1>
                <p className="page-subtitle">Track payment flows and collection performance</p>
            </div>

            {/* Payment Stats */}
            <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: '4px solid #3b82f6' }}>
                    <div className="stat-label">Total Payments</div>
                    <div className="stat-value">{totalPayments}</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #10b981' }}>
                    <div className="stat-label">Completed</div>
                    <div className="stat-value">{completedPayments}</div>
                    <div className="stat-change positive">Successfully collected</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #f59e0b' }}>
                    <div className="stat-label">Pending</div>
                    <div className="stat-value">{pendingPayments}</div>
                    <div className="stat-change">Awaiting payment</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #8b5cf6' }}>
                    <div className="stat-label">Collection Rate</div>
                    <div className="stat-value">{completionRate}%</div>
                    <div className={`stat-change ${completionRate >= 80 ? 'positive' : 'negative'}`}>
                        {completionRate >= 80 ? 'On track' : 'Needs improvement'}
                    </div>
                </div>
            </div>

            {/* Financial Summary */}
            <div className="card" style={{ marginTop: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">💰 Financial Summary</h3>
                </div>
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Amount Collected</div>
                        <div className="stat-value" style={{ color: '#10b981' }}>{formatCurrency(payments?.totalAmountCollected)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Total Amount Pending</div>
                        <div className="stat-value" style={{ color: '#f59e0b' }}>{formatCurrency(payments?.totalAmountPending)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Average Payment Amount</div>
                        <div className="stat-value">{formatCurrency(payments?.averagePaymentAmount)}</div>
                    </div>
                </div>
            </div>

            {/* Collection Progress Bar */}
            <div className="card" style={{ marginTop: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">📊 Collection Progress</h3>
                </div>
                <div style={{ padding: '8px 0' }}>
                    <div style={{ marginBottom: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#9ca3af', fontSize: '14px' }}>🟢 Completed</span>
                            <span style={{ color: 'white', fontWeight: '600' }}>{completedPayments} of {totalPayments}</span>
                        </div>
                        <div style={{ height: '12px', background: 'rgba(255,255,255,0.05)', borderRadius: '6px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${completionRate}%`, background: 'linear-gradient(90deg, #10b981, #34d399)', borderRadius: '6px', transition: 'width 1s ease' }}></div>
                        </div>
                    </div>
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#9ca3af', fontSize: '14px' }}>🔴 Overdue</span>
                            <span style={{ color: 'white', fontWeight: '600' }}>{overduePayments.length}</span>
                        </div>
                        <div style={{ height: '12px', background: 'rgba(255,255,255,0.05)', borderRadius: '6px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${totalPayments ? Math.max((overduePayments.length / totalPayments) * 100, 2) : 2}%`, background: 'linear-gradient(90deg, #ef4444, #f87171)', borderRadius: '6px', transition: 'width 1s ease' }}></div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Overdue Payments */}
            {overduePayments.length > 0 && (
                <div className="card" style={{ marginTop: '24px' }}>
                    <div className="card-header">
                        <h3 className="card-title">🔴 Overdue Payments ({overduePayments.length})</h3>
                    </div>
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Amount</th>
                                    <th>Due Date</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {overduePayments.slice(0, 20).map((p, idx) => (
                                    <tr key={idx}>
                                        <td>#{p.id || idx + 1}</td>
                                        <td>{formatCurrency(p.amount)}</td>
                                        <td>{p.dueDate || 'N/A'}</td>
                                        <td><span style={{ color: '#ef4444', fontWeight: '600' }}>OVERDUE</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AnalystPayments;
