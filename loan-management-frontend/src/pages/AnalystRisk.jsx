import { useState, useEffect } from 'react';
import { analystAPI } from '../services/api';

const AnalystRisk = () => {
    const [risk, setRisk] = useState(null);
    const [overduePayments, setOverduePayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [riskRes, overdueRes] = await Promise.all([
                analystAPI.getRiskAssessment(),
                analystAPI.getOverduePayments(),
            ]);
            setRisk(riskRes.data.data);
            setOverduePayments(overdueRes.data.data || []);
        } catch (err) {
            setError('Failed to load risk data');
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
                <p>Loading risk assessment...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    const riskScore = risk?.portfolioRiskScore || 0;
    const riskColor = riskScore > 50 ? '#ef4444' : riskScore > 20 ? '#f59e0b' : '#10b981';

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">⚠️ Risk Assessment</h1>
                <p className="page-subtitle">Portfolio risk analysis and overdue tracking</p>
            </div>

            {/* Risk Score Overview */}
            <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: `4px solid ${riskColor}` }}>
                    <div className="stat-label">Portfolio Risk Score</div>
                    <div className="stat-value" style={{ color: riskColor }}>{riskScore.toFixed(1)}</div>
                    <div className={`stat-change ${riskScore > 20 ? 'negative' : 'positive'}`}>
                        {riskScore > 50 ? 'Critical' : riskScore > 20 ? 'Moderate' : 'Low'} risk
                    </div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #ef4444' }}>
                    <div className="stat-label">Overdue Payments</div>
                    <div className="stat-value">{risk?.overduePaymentsCount || 0}</div>
                    <div className="stat-change negative">Require attention</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #f59e0b' }}>
                    <div className="stat-label">Total Overdue Amount</div>
                    <div className="stat-value">{formatCurrency(risk?.totalOverdueAmount)}</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #8b5cf6' }}>
                    <div className="stat-label">Loans at Risk</div>
                    <div className="stat-value">{risk?.loansAtRisk || 0}</div>
                    <div className="stat-change negative">With missed payments</div>
                </div>
            </div>

            {/* Risk Gauge */}
            <div className="card" style={{ marginTop: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">📊 Risk Gauge</h3>
                </div>
                <div style={{ padding: '20px 0' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                        <span style={{ color: '#10b981', fontSize: '13px' }}>Low Risk</span>
                        <span style={{ color: '#f59e0b', fontSize: '13px' }}>Moderate</span>
                        <span style={{ color: '#ef4444', fontSize: '13px' }}>High Risk</span>
                    </div>
                    <div style={{ height: '16px', background: 'rgba(255,255,255,0.05)', borderRadius: '8px', overflow: 'hidden', position: 'relative' }}>
                        <div style={{
                            height: '100%',
                            width: `${Math.min(riskScore, 100)}%`,
                            background: `linear-gradient(90deg, #10b981, #f59e0b, #ef4444)`,
                            borderRadius: '8px',
                            transition: 'width 1s ease',
                        }}></div>
                    </div>
                    <div style={{ textAlign: 'center', marginTop: '12px', color: riskColor, fontSize: '20px', fontWeight: '700' }}>
                        {riskScore.toFixed(1)} / 100
                    </div>
                </div>
            </div>

            {/* Overdue Payments List */}
            <div className="card" style={{ marginTop: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">🔴 Overdue Payments ({overduePayments.length})</h3>
                </div>
                {overduePayments.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '32px', color: '#9ca3af' }}>
                        ✅ No overdue payments found. Portfolio is in good standing.
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Payment ID</th>
                                    <th>Amount</th>
                                    <th>Due Date</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {overduePayments.slice(0, 20).map((payment, idx) => (
                                    <tr key={idx}>
                                        <td>#{payment.id || idx + 1}</td>
                                        <td>{formatCurrency(payment.amount)}</td>
                                        <td>{payment.dueDate || 'N/A'}</td>
                                        <td><span style={{ color: '#ef4444', fontWeight: '600' }}>OVERDUE</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AnalystRisk;
