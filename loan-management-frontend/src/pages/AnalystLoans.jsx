import { useState, useEffect } from 'react';
import { analystAPI } from '../services/api';

const AnalystLoans = () => {
    const [loans, setLoans] = useState([]);
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [loanPayments, setLoanPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [filter, setFilter] = useState('ALL');

    useEffect(() => {
        fetchLoans();
    }, []);

    const fetchLoans = async () => {
        try {
            const response = await analystAPI.getAllLoans();
            setLoans(response.data.data || []);
        } catch (err) {
            setError('Failed to load loans');
        } finally {
            setLoading(false);
        }
    };

    const viewLoanDetails = async (loan) => {
        if (selectedLoan?.id === loan.id) {
            setSelectedLoan(null);
            setLoanPayments([]);
            return;
        }
        setSelectedLoan(loan);
        try {
            const res = await analystAPI.getLoanPayments(loan.id);
            setLoanPayments(res.data.data || []);
        } catch {
            setLoanPayments([]);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(amount || 0);
    };

    const getStatusColor = (status) => {
        const colors = {
            'ACTIVE': '#10b981',
            'COMPLETED': '#3b82f6',
            'DEFAULTED': '#ef4444',
            'PENDING': '#f59e0b',
            'CLOSED': '#6b7280',
        };
        return colors[status] || '#9ca3af';
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading loans...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    const filteredLoans = filter === 'ALL' ? loans : loans.filter(l => l.status === filter);
    const statuses = ['ALL', ...new Set(loans.map(l => l.status).filter(Boolean))];

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">📋 All Loans</h1>
                <p className="page-subtitle">Browse and analyze all loans on the platform</p>
            </div>

            {/* Summary Stats */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Loans</div>
                    <div className="stat-value">{loans.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Active</div>
                    <div className="stat-value" style={{ color: '#10b981' }}>{loans.filter(l => l.status === 'ACTIVE').length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Completed</div>
                    <div className="stat-value" style={{ color: '#3b82f6' }}>{loans.filter(l => l.status === 'COMPLETED').length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Defaulted</div>
                    <div className="stat-value" style={{ color: '#ef4444' }}>{loans.filter(l => l.status === 'DEFAULTED').length}</div>
                </div>
            </div>

            {/* Filter Buttons */}
            <div style={{ display: 'flex', gap: '8px', marginTop: '24px', marginBottom: '16px', flexWrap: 'wrap' }}>
                {statuses.map(status => (
                    <button
                        key={status}
                        onClick={() => setFilter(status)}
                        style={{
                            padding: '8px 16px',
                            borderRadius: '8px',
                            border: 'none',
                            background: filter === status ? (status === 'ALL' ? '#667eea' : getStatusColor(status)) : 'rgba(255,255,255,0.05)',
                            color: filter === status ? 'white' : '#9ca3af',
                            cursor: 'pointer',
                            fontSize: '13px',
                            fontWeight: '600',
                            transition: 'all 0.2s',
                        }}
                    >
                        {status} {status !== 'ALL' ? `(${loans.filter(l => l.status === status).length})` : `(${loans.length})`}
                    </button>
                ))}
            </div>

            {/* Loans Table */}
            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Loans ({filteredLoans.length})</h3>
                </div>
                {filteredLoans.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '32px', color: '#9ca3af' }}>
                        No loans found for this filter.
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Title</th>
                                    <th>Amount</th>
                                    <th>Interest</th>
                                    <th>Duration</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredLoans.map((loan) => (
                                    <>
                                        <tr key={loan.id} style={{ cursor: 'pointer' }} onClick={() => viewLoanDetails(loan)}>
                                            <td>#{loan.id}</td>
                                            <td>{loan.title || 'Untitled Loan'}</td>
                                            <td>{formatCurrency(loan.amount)}</td>
                                            <td>{loan.interestRate?.toFixed(1) || 0}%</td>
                                            <td>{loan.durationMonths || 0} months</td>
                                            <td>
                                                <span style={{
                                                    padding: '4px 10px',
                                                    borderRadius: '12px',
                                                    fontSize: '11px',
                                                    fontWeight: '700',
                                                    background: `${getStatusColor(loan.status)}20`,
                                                    color: getStatusColor(loan.status),
                                                }}>
                                                    {loan.status}
                                                </span>
                                            </td>
                                            <td>
                                                <button
                                                    className="btn btn-secondary"
                                                    style={{ fontSize: '12px', padding: '4px 10px' }}
                                                    onClick={(e) => { e.stopPropagation(); viewLoanDetails(loan); }}
                                                >
                                                    {selectedLoan?.id === loan.id ? '▲ Hide' : '▼ Details'}
                                                </button>
                                            </td>
                                        </tr>
                                        {selectedLoan?.id === loan.id && (
                                            <tr key={`${loan.id}-details`}>
                                                <td colSpan="7" style={{ background: 'rgba(102,126,234,0.05)', padding: '20px' }}>
                                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: loanPayments.length > 0 ? '16px' : '0' }}>
                                                        <div>
                                                            <span style={{ color: '#9ca3af', fontSize: '12px' }}>Lender</span>
                                                            <div style={{ color: 'white', fontWeight: '600' }}>{loan.lenderName || 'N/A'}</div>
                                                        </div>
                                                        <div>
                                                            <span style={{ color: '#9ca3af', fontSize: '12px' }}>Borrower</span>
                                                            <div style={{ color: 'white', fontWeight: '600' }}>{loan.borrowerName || 'N/A'}</div>
                                                        </div>
                                                        <div>
                                                            <span style={{ color: '#9ca3af', fontSize: '12px' }}>Created</span>
                                                            <div style={{ color: 'white', fontWeight: '600' }}>{loan.createdAt ? new Date(loan.createdAt).toLocaleDateString() : 'N/A'}</div>
                                                        </div>
                                                        <div>
                                                            <span style={{ color: '#9ca3af', fontSize: '12px' }}>Monthly EMI</span>
                                                            <div style={{ color: 'white', fontWeight: '600' }}>{formatCurrency(loan.emiAmount)}</div>
                                                        </div>
                                                    </div>
                                                    {loanPayments.length > 0 && (
                                                        <div>
                                                            <div style={{ fontSize: '13px', color: '#9ca3af', marginBottom: '8px', fontWeight: '600' }}>Payment History ({loanPayments.length})</div>
                                                            <table className="data-table" style={{ fontSize: '12px' }}>
                                                                <thead>
                                                                    <tr>
                                                                        <th>Due Date</th>
                                                                        <th>Amount</th>
                                                                        <th>Status</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    {loanPayments.slice(0, 10).map((p, idx) => (
                                                                        <tr key={idx}>
                                                                            <td>{p.dueDate || 'N/A'}</td>
                                                                            <td>{formatCurrency(p.amount)}</td>
                                                                            <td>
                                                                                <span style={{ color: getStatusColor(p.status), fontWeight: '600' }}>
                                                                                    {p.status}
                                                                                </span>
                                                                            </td>
                                                                        </tr>
                                                                    ))}
                                                                </tbody>
                                                            </table>
                                                        </div>
                                                    )}
                                                </td>
                                            </tr>
                                        )}
                                    </>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AnalystLoans;
