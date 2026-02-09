import { useState, useEffect } from 'react';
import { lenderAPI } from '../services/api';

const LenderPayments = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionLoading, setActionLoading] = useState(null);

    useEffect(() => {
        fetchPendingPayments();
    }, []);

    const fetchPendingPayments = async () => {
        try {
            setLoading(true);
            const response = await lenderAPI.getPendingPayments();
            setPayments(response.data.data || []);
        } catch (err) {
            setError('Failed to load pending payments');
            console.error(err);
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

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    };

    const handleApprove = async (paymentId) => {
        if (!confirm('Are you sure you want to approve this payment?')) return;
        
        try {
            setActionLoading(paymentId);
            await lenderAPI.approvePayment(paymentId);
            alert('Payment approved successfully!');
            fetchPendingPayments();
        } catch (err) {
            console.error('Error approving payment:', err);
            alert(err.response?.data?.message || 'Failed to approve payment');
        } finally {
            setActionLoading(null);
        }
    };

    const handleReject = async (paymentId) => {
        const reason = prompt('Enter reason for rejection (optional):');
        if (reason === null) return; // User cancelled
        
        try {
            setActionLoading(paymentId);
            await lenderAPI.rejectPayment(paymentId, reason);
            alert('Payment rejected.');
            fetchPendingPayments();
        } catch (err) {
            console.error('Error rejecting payment:', err);
            alert(err.response?.data?.message || 'Failed to reject payment');
        } finally {
            setActionLoading(null);
        }
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading...</p>
            </div>
        );
    }

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">Payment Approvals</h1>
                <p className="page-subtitle">Review and approve borrower payment claims</p>
            </div>

            {error && <div className="alert alert-error" style={{ marginBottom: '20px' }}>{error}</div>}

            {payments.length === 0 ? (
                <div className="card">
                    <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                        🎉 No pending payment approvals at this time.
                    </p>
                </div>
            ) : (
                <>
                    <div className="alert alert-info" style={{ marginBottom: '20px' }}>
                        <strong>📋 {payments.length} payment(s)</strong> awaiting your approval
                    </div>
                    
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>EMI #</th>
                                    <th>Loan ID</th>
                                    <th>Amount Due</th>
                                    <th>Due Date</th>
                                    <th>Transaction Ref</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {payments.map((payment) => (
                                    <tr key={payment.id}>
                                        <td>#{payment.paymentNumber || payment.id}</td>
                                        <td>#{payment.loanId}</td>
                                        <td>{formatCurrency(payment.amountDue)}</td>
                                        <td>
                                            {formatDate(payment.dueDate)}
                                            {new Date(payment.dueDate) < new Date() && (
                                                <span style={{ color: '#ef4444', fontSize: '11px', display: 'block' }}>
                                                    (Overdue)
                                                </span>
                                            )}
                                        </td>
                                        <td style={{ maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                            {payment.transactionReference || '-'}
                                        </td>
                                        <td>
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <button
                                                    className="btn btn-primary btn-sm"
                                                    onClick={() => handleApprove(payment.id)}
                                                    disabled={actionLoading === payment.id}
                                                    style={{ padding: '6px 12px', fontSize: '12px' }}
                                                >
                                                    {actionLoading === payment.id ? '...' : '✅ Approve'}
                                                </button>
                                                <button
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => handleReject(payment.id)}
                                                    disabled={actionLoading === payment.id}
                                                    style={{ padding: '6px 12px', fontSize: '12px' }}
                                                >
                                                    {actionLoading === payment.id ? '...' : '❌ Reject'}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </>
            )}

            <div className="card" style={{ marginTop: '24px', padding: '16px' }}>
                <h4 style={{ marginBottom: '12px' }}>ℹ️ Payment Approval Guidelines</h4>
                <ul style={{ color: 'var(--text-secondary)', paddingLeft: '20px', lineHeight: '1.8' }}>
                    <li><strong>Approve</strong> - If you have received the payment in your bank account</li>
                    <li><strong>Reject</strong> - If payment was not received or is incorrect</li>
                    <li>Rejected payments will be marked as pending again</li>
                    <li>Overdue rejected payments will incur the penalty rate you set when creating the loan</li>
                </ul>
            </div>
        </div>
    );
};

export default LenderPayments;
