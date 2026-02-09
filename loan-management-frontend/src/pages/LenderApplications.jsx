import { useState, useEffect } from 'react';
import { lenderAPI } from '../services/api';

const LenderApplications = () => {
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedApp, setSelectedApp] = useState(null);
    const [showApproveModal, setShowApproveModal] = useState(false);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [rejectReason, setRejectReason] = useState('');
    const [processing, setProcessing] = useState(false);

    useEffect(() => {
        fetchApplications();
    }, []);

    const fetchApplications = async () => {
        try {
            const response = await lenderAPI.getApplications();
            setApplications(response.data.data || []);
        } catch (err) {
            setError('Failed to load applications');
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

    const handleApprove = async () => {
        setProcessing(true);
        try {
            await lenderAPI.approveApplication(selectedApp.id);
            alert('Application approved successfully! The loan is now active.');
            setShowApproveModal(false);
            setSelectedApp(null);
            fetchApplications();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to approve application');
        } finally {
            setProcessing(false);
        }
    };

    const handleReject = async () => {
        if (!rejectReason.trim()) {
            alert('Please provide a reason for rejection');
            return;
        }

        setProcessing(true);
        try {
            await lenderAPI.rejectApplication(selectedApp.id, rejectReason);
            alert('Application rejected');
            setShowRejectModal(false);
            setSelectedApp(null);
            setRejectReason('');
            fetchApplications();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to reject application');
        } finally {
            setProcessing(false);
        }
    };

    const openApproveModal = (app) => {
        setSelectedApp(app);
        setShowApproveModal(true);
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading applications...</p>
            </div>
        );
    }

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">📋 Loan Applications</h1>
                <p className="page-subtitle">Review and process borrower applications</p>
            </div>

            {error && <div className="alert alert-error">{error}</div>}

            {/* Applications List */}
            {applications.length === 0 ? (
                <div className="card" style={{ textAlign: 'center', padding: '48px' }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>📭</div>
                    <h3 style={{ marginBottom: '8px' }}>No Pending Applications</h3>
                    <p style={{ color: 'var(--text-secondary)' }}>
                        There are no loan applications waiting for review.
                    </p>
                </div>
            ) : (
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Pending Applications ({applications.length})</h3>
                    </div>
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>App ID</th>
                                    <th>Loan ID</th>
                                    <th>Borrower</th>
                                    <th>Amount</th>
                                    <th>Interest Rate</th>
                                    <th>Term</th>
                                    <th>Purpose</th>
                                    <th>Applied On</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {applications.map(app => (
                                    <tr key={app.id}>
                                        <td>
                                            <span style={{ fontWeight: '600' }}>#{app.id}</span>
                                        </td>
                                        <td>
                                            <span className="badge badge-info">Loan #{app.loanId}</span>
                                        </td>
                                        <td>
                                            <div>
                                                <div style={{ fontWeight: '500' }}>{app.borrowerName}</div>
                                            </div>
                                        </td>
                                        <td style={{ fontWeight: '600', color: 'var(--accent-primary)' }}>
                                            {formatCurrency(app.requestedAmount)}
                                        </td>
                                        <td>
                                            <span style={{ fontWeight: '600', color: 'var(--success)' }}>
                                                {app.loanInterestRate}%
                                            </span>
                                        </td>
                                        <td>{app.requestedTermMonths} months</td>
                                        <td>
                                            <span style={{ 
                                                maxWidth: '150px', 
                                                display: 'block', 
                                                overflow: 'hidden', 
                                                textOverflow: 'ellipsis',
                                                whiteSpace: 'nowrap'
                                            }}>
                                                {app.purpose || 'N/A'}
                                            </span>
                                        </td>
                                        <td>{formatDate(app.createdAt)}</td>
                                        <td>
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <button
                                                    className="btn btn-primary"
                                                    style={{ padding: '6px 12px', fontSize: '13px' }}
                                                    onClick={() => openApproveModal(app)}
                                                >
                                                    ✅ Approve
                                                </button>
                                                <button
                                                    className="btn btn-danger"
                                                    style={{ padding: '6px 12px', fontSize: '13px' }}
                                                    onClick={() => {
                                                        setSelectedApp(app);
                                                        setShowRejectModal(true);
                                                    }}
                                                >
                                                    ❌ Reject
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Approve Modal - Simplified */}
            {showApproveModal && selectedApp && (
                <div className="modal-overlay" onClick={() => setShowApproveModal(false)}>
                    <div className="modal-content" style={{ maxWidth: '500px' }} onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>✅ Approve Application</h2>
                            <button className="modal-close" onClick={() => setShowApproveModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ 
                                marginBottom: '20px', 
                                padding: '20px', 
                                background: 'var(--bg-glass)', 
                                borderRadius: 'var(--radius-md)',
                                border: '1px solid var(--border-color)'
                            }}>
                                <h4 style={{ marginBottom: '16px', color: 'var(--accent-primary)' }}>
                                    Loan Details (Loan #{selectedApp.loanId})
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                    <div>
                                        <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>Principal Amount</span>
                                        <p style={{ fontWeight: '600', fontSize: '18px', margin: '4px 0' }}>
                                            {formatCurrency(selectedApp.requestedAmount)}
                                        </p>
                                    </div>
                                    <div>
                                        <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>Interest Rate</span>
                                        <p style={{ fontWeight: '600', fontSize: '18px', margin: '4px 0', color: 'var(--success)' }}>
                                            {selectedApp.loanInterestRate}%
                                        </p>
                                    </div>
                                    <div>
                                        <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>Term</span>
                                        <p style={{ fontWeight: '600', fontSize: '18px', margin: '4px 0' }}>
                                            {selectedApp.requestedTermMonths} months
                                        </p>
                                    </div>
                                    <div>
                                        <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>Penalty Rate</span>
                                        <p style={{ fontWeight: '600', fontSize: '18px', margin: '4px 0', color: 'var(--danger)' }}>
                                            {selectedApp.loanPenaltyRate || 2}%
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div style={{ 
                                marginBottom: '20px', 
                                padding: '16px', 
                                background: 'rgba(0, 212, 170, 0.1)', 
                                borderRadius: 'var(--radius-md)'
                            }}>
                                <h4 style={{ marginBottom: '12px' }}>👤 Borrower Information</h4>
                                <p><strong>Name:</strong> {selectedApp.borrowerName}</p>
                                <p><strong>Purpose:</strong> {selectedApp.purpose || 'Not specified'}</p>
                                {selectedApp.creditScore && (
                                    <p><strong>Credit Score:</strong> 
                                        <span className={`badge ${selectedApp.creditScore >= 700 ? 'badge-success' : selectedApp.creditScore >= 600 ? 'badge-warning' : 'badge-danger'}`} style={{ marginLeft: '8px' }}>
                                            {selectedApp.creditScore}
                                        </span>
                                    </p>
                                )}
                            </div>

                            <div style={{ 
                                padding: '12px', 
                                background: 'rgba(255, 193, 7, 0.1)', 
                                borderRadius: 'var(--radius-md)',
                                marginBottom: '20px'
                            }}>
                                <p style={{ margin: 0, fontSize: '14px' }}>
                                    ⚠️ By approving, the loan will become <strong>ACTIVE</strong> and EMI schedule will be generated.
                                </p>
                            </div>

                            <div style={{ display: 'flex', gap: '12px' }}>
                                <button 
                                    className="btn btn-secondary" 
                                    style={{ flex: 1 }}
                                    onClick={() => setShowApproveModal(false)}
                                >
                                    Cancel
                                </button>
                                <button 
                                    className="btn btn-primary" 
                                    style={{ flex: 1 }}
                                    onClick={handleApprove}
                                    disabled={processing}
                                >
                                    {processing ? 'Processing...' : '✅ Approve & Activate Loan'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Reject Modal */}
            {showRejectModal && selectedApp && (
                <div className="modal-overlay" onClick={() => setShowRejectModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>❌ Reject Application #{selectedApp.id}</h2>
                            <button className="modal-close" onClick={() => setShowRejectModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ marginBottom: '20px', padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                                <p><strong>Borrower:</strong> {selectedApp.borrowerName}</p>
                                <p><strong>Requested Amount:</strong> {formatCurrency(selectedApp.requestedAmount)}</p>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Reason for Rejection *</label>
                                <textarea
                                    className="form-input"
                                    rows="4"
                                    value={rejectReason}
                                    onChange={(e) => setRejectReason(e.target.value)}
                                    placeholder="Please provide a reason for rejecting this application..."
                                />
                            </div>

                            <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                                <button 
                                    className="btn btn-secondary" 
                                    style={{ flex: 1 }}
                                    onClick={() => {
                                        setShowRejectModal(false);
                                        setRejectReason('');
                                    }}
                                >
                                    Cancel
                                </button>
                                <button 
                                    className="btn btn-danger" 
                                    style={{ flex: 1 }}
                                    onClick={handleReject}
                                    disabled={processing}
                                >
                                    {processing ? 'Processing...' : '❌ Reject Application'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LenderApplications;
