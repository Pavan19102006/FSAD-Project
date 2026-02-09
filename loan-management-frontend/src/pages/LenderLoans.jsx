import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { lenderAPI } from '../services/api';

const LenderLoans = () => {
    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [filter, setFilter] = useState('ALL');

    useEffect(() => {
        fetchLoans();
    }, []);

    const fetchLoans = async () => {
        try {
            const response = await lenderAPI.getLoans();
            setLoans(response.data.data || []);
        } catch (err) {
            setError('Failed to load loans');
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

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    const getStatusBadge = (status) => {
        const statusClasses = {
            'ACTIVE': 'badge badge-success',
            'COMPLETED': 'badge badge-info',
            'PENDING': 'badge badge-warning',
            'DEFAULTED': 'badge badge-danger',
            'CANCELLED': 'badge badge-secondary'
        };
        return statusClasses[status] || 'badge';
    };

    const filteredLoans = filter === 'ALL' 
        ? loans 
        : loans.filter(loan => loan.status === filter);

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading loans...</p>
            </div>
        );
    }

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">💰 My Loans</h1>
                <p className="page-subtitle">Manage and track all your loan offers</p>
            </div>

            {error && <div className="alert alert-error">{error}</div>}

            {/* Filter Tabs */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                    {['ALL', 'ACTIVE', 'PENDING', 'COMPLETED', 'DEFAULTED'].map(status => (
                        <button
                            key={status}
                            className={`btn ${filter === status ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setFilter(status)}
                        >
                            {status === 'ALL' ? '📋 All' : status === 'ACTIVE' ? '✅ Active' : status === 'PENDING' ? '⏳ Pending' : status === 'COMPLETED' ? '🎉 Completed' : '⚠️ Defaulted'}
                        </button>
                    ))}
                    <Link to="/lender/create" className="btn btn-primary" style={{ marginLeft: 'auto' }}>
                        ➕ Create New Loan
                    </Link>
                </div>
            </div>

            {/* Loans Table */}
            {filteredLoans.length === 0 ? (
                <div className="card" style={{ textAlign: 'center', padding: '48px' }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>📭</div>
                    <h3 style={{ marginBottom: '8px' }}>No Loans Found</h3>
                    <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>
                        {filter === 'ALL' ? "You haven't created any loans yet." : `No ${filter.toLowerCase()} loans found.`}
                    </p>
                    <Link to="/lender/create" className="btn btn-primary">
                        ➕ Create Your First Loan
                    </Link>
                </div>
            ) : (
                <div className="card">
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Loan ID</th>
                                    <th>Borrower</th>
                                    <th>Principal Amount</th>
                                    <th>Interest Rate</th>
                                    <th>Term</th>
                                    <th>Status</th>
                                    <th>Start Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredLoans.map(loan => (
                                    <tr key={loan.id}>
                                        <td>
                                            <span style={{ fontWeight: '600' }}>#{loan.id}</span>
                                        </td>
                                        <td>
                                            {loan.borrowerName || 'Awaiting Borrower'}
                                        </td>
                                        <td style={{ fontWeight: '600', color: 'var(--accent-primary)' }}>
                                            {formatCurrency(loan.principalAmount)}
                                        </td>
                                        <td>{loan.interestRate}%</td>
                                        <td>{loan.termMonths} months</td>
                                        <td>
                                            <span className={getStatusBadge(loan.status)}>
                                                {loan.status}
                                            </span>
                                        </td>
                                        <td>{formatDate(loan.startDate)}</td>
                                        <td>
                                            <Link 
                                                to={`/lender/loans/${loan.id}`} 
                                                className="btn btn-secondary"
                                                style={{ padding: '6px 12px', fontSize: '13px' }}
                                            >
                                                View Details
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Summary Cards */}
            <div className="stats-grid" style={{ marginTop: '24px' }}>
                <div className="stat-card">
                    <div className="stat-label">Total Loans</div>
                    <div className="stat-value">{loans.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Active Loans</div>
                    <div className="stat-value">{loans.filter(l => l.status === 'ACTIVE').length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Principal</div>
                    <div className="stat-value">
                        {formatCurrency(loans.reduce((sum, l) => sum + (l.principalAmount || 0), 0))}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Expected Interest</div>
                    <div className="stat-value">
                        {formatCurrency(loans.reduce((sum, l) => sum + (l.totalInterest || 0), 0))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LenderLoans;
