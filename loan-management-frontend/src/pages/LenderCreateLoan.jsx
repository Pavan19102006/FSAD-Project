import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { lenderAPI } from '../services/api';

const LenderCreateLoan = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        principalAmount: '',
        interestRate: '12',
        termMonths: '12',
        interestType: 'COMPOUND',
        emiType: 'REDUCING_BALANCE',
        description: '',
        penaltyRate: '2'
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const calculateEMI = () => {
        const principal = parseFloat(formData.principalAmount) || 0;
        const rate = parseFloat(formData.interestRate) / 100 / 12;
        const months = parseInt(formData.termMonths) || 1;

        if (formData.emiType === 'FLAT') {
            const totalInterest = principal * (parseFloat(formData.interestRate) / 100) * (months / 12);
            return (principal + totalInterest) / months;
        } else {
            // Reducing balance EMI calculation
            if (rate === 0) return principal / months;
            return principal * rate * Math.pow(1 + rate, months) / (Math.pow(1 + rate, months) - 1);
        }
    };

    const calculateTotalInterest = () => {
        const principal = parseFloat(formData.principalAmount) || 0;
        const months = parseInt(formData.termMonths) || 1;
        const emi = calculateEMI();
        return (emi * months) - principal;
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
        }).format(amount || 0);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!formData.principalAmount || parseFloat(formData.principalAmount) <= 0) {
            setError('Please enter a valid principal amount');
            return;
        }

        setLoading(true);
        try {
            await lenderAPI.createLoan({
                principalAmount: parseFloat(formData.principalAmount),
                interestRate: parseFloat(formData.interestRate),
                termMonths: parseInt(formData.termMonths),
                interestType: formData.interestType,
                emiType: formData.emiType,
                description: formData.description,
                penaltyRate: parseFloat(formData.penaltyRate)
            });
            alert('Loan offer created successfully!');
            navigate('/lender/loans');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create loan offer');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">➕ Create New Loan Offer</h1>
                <p className="page-subtitle">Set up a new loan offer for borrowers</p>
            </div>

            {error && <div className="alert alert-error" style={{ marginBottom: '24px' }}>{error}</div>}

            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
                {/* Loan Form */}
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Loan Details</h3>
                    </div>
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">Principal Amount (₹) *</label>
                            <input
                                type="number"
                                name="principalAmount"
                                className="form-input"
                                value={formData.principalAmount}
                                onChange={handleChange}
                                placeholder="Enter loan amount"
                                min="1000"
                                required
                            />
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">Interest Rate (% per annum) *</label>
                                <input
                                    type="number"
                                    name="interestRate"
                                    className="form-input"
                                    value={formData.interestRate}
                                    onChange={handleChange}
                                    placeholder="12"
                                    min="1"
                                    max="36"
                                    step="0.1"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Loan Term (Months) *</label>
                                <input
                                    type="number"
                                    name="termMonths"
                                    className="form-input"
                                    value={formData.termMonths}
                                    onChange={handleChange}
                                    placeholder="12"
                                    min="1"
                                    max="360"
                                    required
                                />
                            </div>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">Interest Type</label>
                                <select
                                    name="interestType"
                                    className="form-input"
                                    value={formData.interestType}
                                    onChange={handleChange}
                                >
                                    <option value="COMPOUND">Compound Interest</option>
                                    <option value="SIMPLE">Simple Interest</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label className="form-label">EMI Type</label>
                                <select
                                    name="emiType"
                                    className="form-input"
                                    value={formData.emiType}
                                    onChange={handleChange}
                                >
                                    <option value="REDUCING_BALANCE">Reducing Balance</option>
                                    <option value="FLAT">Flat Rate</option>
                                </select>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Penalty Rate (% per month for late payments)</label>
                            <input
                                type="number"
                                name="penaltyRate"
                                className="form-input"
                                value={formData.penaltyRate}
                                onChange={handleChange}
                                placeholder="2"
                                min="0"
                                max="10"
                                step="0.1"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Description / Terms</label>
                            <textarea
                                name="description"
                                className="form-input"
                                rows="4"
                                value={formData.description}
                                onChange={handleChange}
                                placeholder="Enter any additional terms, conditions, or description for this loan offer..."
                            />
                        </div>

                        <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() => navigate('/lender/loans')}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="btn btn-primary"
                                disabled={loading}
                                style={{ flex: 1 }}
                            >
                                {loading ? 'Creating...' : '✅ Create Loan Offer'}
                            </button>
                        </div>
                    </form>
                </div>

                {/* Loan Summary */}
                <div>
                    <div className="card" style={{ position: 'sticky', top: '24px' }}>
                        <div className="card-header">
                            <h3 className="card-title">📊 Loan Summary</h3>
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                                <div style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '4px' }}>Principal Amount</div>
                                <div style={{ fontSize: '24px', fontWeight: '700', color: 'var(--accent-primary)' }}>
                                    {formatCurrency(formData.principalAmount || 0)}
                                </div>
                            </div>

                            <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                                <div style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '4px' }}>Monthly EMI</div>
                                <div style={{ fontSize: '24px', fontWeight: '700', color: 'var(--secondary)' }}>
                                    {formatCurrency(calculateEMI())}
                                </div>
                            </div>

                            <div style={{ padding: '16px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                                <div style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '4px' }}>Total Interest Earned</div>
                                <div style={{ fontSize: '24px', fontWeight: '700', color: 'var(--accent)' }}>
                                    {formatCurrency(calculateTotalInterest())}
                                </div>
                            </div>

                            <div style={{ padding: '16px', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', borderRadius: 'var(--radius-md)' }}>
                                <div style={{ color: 'rgba(255,255,255,0.8)', fontSize: '13px', marginBottom: '4px' }}>Total Amount to Receive</div>
                                <div style={{ fontSize: '24px', fontWeight: '700', color: '#fff' }}>
                                    {formatCurrency((parseFloat(formData.principalAmount) || 0) + calculateTotalInterest())}
                                </div>
                            </div>

                            <div style={{ fontSize: '13px', color: 'var(--text-secondary)', padding: '12px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)' }}>
                                <p><strong>Interest Rate:</strong> {formData.interestRate}% per annum</p>
                                <p><strong>Loan Term:</strong> {formData.termMonths} months</p>
                                <p><strong>EMI Type:</strong> {formData.emiType === 'REDUCING_BALANCE' ? 'Reducing Balance' : 'Flat Rate'}</p>
                                <p><strong>Late Payment Penalty:</strong> {formData.penaltyRate}% per month</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LenderCreateLoan;
