import { useState, useEffect } from 'react';
import { lenderAPI } from '../services/api';

const LenderWithdraw = () => {
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [withdrawAmount, setWithdrawAmount] = useState('');
    const [withdrawLoading, setWithdrawLoading] = useState(false);
    const [withdrawHistory, setWithdrawHistory] = useState([]);
    const [bankDetails, setBankDetails] = useState({
        accountNumber: '',
        ifscCode: '',
        accountHolderName: '',
        bankName: ''
    });

    useEffect(() => {
        fetchDashboard();
    }, []);

    const fetchDashboard = async () => {
        try {
            const response = await lenderAPI.getDashboard();
            setDashboard(response.data.data);
        } catch (err) {
            setError('Failed to load data');
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

    const availableBalance = (dashboard?.amountReclaimed || 0) + (dashboard?.totalInterestEarned || 0);

    const handleWithdraw = async () => {
        if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
            alert('Please enter a valid amount');
            return;
        }
        
        if (parseFloat(withdrawAmount) > availableBalance) {
            alert('Insufficient balance for withdrawal');
            return;
        }

        if (!bankDetails.accountNumber || !bankDetails.ifscCode || !bankDetails.accountHolderName) {
            alert('Please enter your bank details');
            return;
        }

        setWithdrawLoading(true);
        try {
            const response = await lenderAPI.withdrawFunds({ 
                amount: parseFloat(withdrawAmount),
                bankDetails: bankDetails
            });
            
            // Add to history
            setWithdrawHistory(prev => [{
                id: response.data.data?.transactionId || `WD${Date.now()}`,
                amount: parseFloat(withdrawAmount),
                status: 'PENDING',
                date: new Date().toISOString(),
                bankAccount: `****${bankDetails.accountNumber.slice(-4)}`
            }, ...prev]);

            alert('Withdrawal request submitted successfully! Funds will be transferred within 2-3 business days.');
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
                <p>Loading...</p>
            </div>
        );
    }

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">💸 Withdraw Funds</h1>
                <p className="page-subtitle">Transfer your earnings to your bank account</p>
            </div>

            {error && <div className="alert alert-error">{error}</div>}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                {/* Available Balance Card */}
                <div className="card" style={{ background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)', border: '1px solid var(--accent-primary)' }}>
                    <div className="card-header">
                        <h3 className="card-title">💳 Available Balance</h3>
                    </div>
                    <div style={{ textAlign: 'center', padding: '24px 0' }}>
                        <div style={{ fontSize: '48px', fontWeight: '700', color: 'var(--accent-primary)', marginBottom: '16px' }}>
                            {formatCurrency(availableBalance)}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'center', gap: '32px', color: 'var(--text-secondary)' }}>
                            <div>
                                <div style={{ fontSize: '12px' }}>Principal Reclaimed</div>
                                <div style={{ fontSize: '18px', fontWeight: '600', color: 'var(--secondary)' }}>
                                    {formatCurrency(dashboard?.amountReclaimed)}
                                </div>
                            </div>
                            <div>
                                <div style={{ fontSize: '12px' }}>Interest Earned</div>
                                <div style={{ fontSize: '18px', fontWeight: '600', color: 'var(--accent)' }}>
                                    {formatCurrency(dashboard?.totalInterestEarned)}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Investment Summary */}
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">📊 Investment Summary</h3>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                        <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                            <div style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>Total Invested</div>
                            <div style={{ fontSize: '20px', fontWeight: '600' }}>{formatCurrency(dashboard?.totalLentAmount)}</div>
                        </div>
                        <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                            <div style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>Active Loans</div>
                            <div style={{ fontSize: '20px', fontWeight: '600' }}>{dashboard?.activeLoans || 0}</div>
                        </div>
                        <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                            <div style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>Completed Loans</div>
                            <div style={{ fontSize: '20px', fontWeight: '600' }}>{dashboard?.completedLoans || 0}</div>
                        </div>
                        <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                            <div style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>Return Rate</div>
                            <div style={{ fontSize: '20px', fontWeight: '600', color: 'var(--secondary)' }}>~12%</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Withdraw Form */}
            <div className="card" style={{ marginTop: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">🏦 Withdraw to Bank Account</h3>
                </div>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                    {/* Bank Details */}
                    <div>
                        <h4 style={{ marginBottom: '16px', color: 'var(--text-secondary)' }}>Bank Details</h4>
                        <div className="form-group">
                            <label className="form-label">Account Holder Name *</label>
                            <input
                                type="text"
                                className="form-input"
                                value={bankDetails.accountHolderName}
                                onChange={(e) => setBankDetails({...bankDetails, accountHolderName: e.target.value})}
                                placeholder="Enter account holder name"
                            />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Bank Account Number *</label>
                            <input
                                type="text"
                                className="form-input"
                                value={bankDetails.accountNumber}
                                onChange={(e) => setBankDetails({...bankDetails, accountNumber: e.target.value})}
                                placeholder="Enter account number"
                            />
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">IFSC Code *</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={bankDetails.ifscCode}
                                    onChange={(e) => setBankDetails({...bankDetails, ifscCode: e.target.value.toUpperCase()})}
                                    placeholder="SBIN0001234"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Bank Name</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={bankDetails.bankName}
                                    onChange={(e) => setBankDetails({...bankDetails, bankName: e.target.value})}
                                    placeholder="State Bank of India"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Withdrawal Amount */}
                    <div>
                        <h4 style={{ marginBottom: '16px', color: 'var(--text-secondary)' }}>Withdrawal Amount</h4>
                        <div className="form-group">
                            <label className="form-label">Amount to Withdraw (₹) *</label>
                            <input
                                type="number"
                                className="form-input"
                                style={{ fontSize: '24px', padding: '16px' }}
                                value={withdrawAmount}
                                onChange={(e) => setWithdrawAmount(e.target.value)}
                                placeholder="0"
                                max={availableBalance}
                                min="100"
                            />
                            <div style={{ marginTop: '8px', display: 'flex', gap: '8px' }}>
                                {[25, 50, 75, 100].map(percent => (
                                    <button
                                        key={percent}
                                        type="button"
                                        className="btn btn-secondary"
                                        style={{ padding: '6px 12px', fontSize: '12px' }}
                                        onClick={() => setWithdrawAmount((availableBalance * percent / 100).toFixed(2))}
                                    >
                                        {percent}%
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)', marginTop: '16px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                <span>Withdrawal Amount</span>
                                <span>{formatCurrency(withdrawAmount || 0)}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', color: 'var(--text-secondary)' }}>
                                <span>Processing Fee</span>
                                <span>₹0.00</span>
                            </div>
                            <hr style={{ border: 'none', borderTop: '1px solid var(--border-color)', margin: '12px 0' }} />
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '600' }}>
                                <span>You'll Receive</span>
                                <span style={{ color: 'var(--accent-primary)' }}>{formatCurrency(withdrawAmount || 0)}</span>
                            </div>
                        </div>

                        <button
                            className="btn btn-primary"
                            style={{ width: '100%', marginTop: '16px', padding: '16px', fontSize: '16px' }}
                            onClick={handleWithdraw}
                            disabled={withdrawLoading || !withdrawAmount || parseFloat(withdrawAmount) <= 0}
                        >
                            {withdrawLoading ? 'Processing...' : '💸 Withdraw Funds'}
                        </button>

                        <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px', textAlign: 'center' }}>
                            Funds will be transferred within 2-3 business days
                        </p>
                    </div>
                </div>
            </div>

            {/* Withdrawal History */}
            {withdrawHistory.length > 0 && (
                <div className="card" style={{ marginTop: '24px' }}>
                    <div className="card-header">
                        <h3 className="card-title">📜 Recent Withdrawals</h3>
                    </div>
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Transaction ID</th>
                                    <th>Amount</th>
                                    <th>Bank Account</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {withdrawHistory.map(tx => (
                                    <tr key={tx.id}>
                                        <td style={{ fontFamily: 'monospace' }}>{tx.id}</td>
                                        <td style={{ fontWeight: '600' }}>{formatCurrency(tx.amount)}</td>
                                        <td>{tx.bankAccount}</td>
                                        <td>
                                            <span className={`badge ${tx.status === 'COMPLETED' ? 'badge-success' : 'badge-warning'}`}>
                                                {tx.status}
                                            </span>
                                        </td>
                                        <td>{new Date(tx.date).toLocaleDateString('en-IN')}</td>
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

export default LenderWithdraw;
