import { useState, useEffect } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import { borrowerAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WelcomeModal, TutorialModal, useOnboarding } from '../components/Onboarding';

const BorrowerDashboard = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [dashboard, setDashboard] = useState(null);
    const [loans, setLoans] = useState([]);
    const [applications, setApplications] = useState([]);
    const [payments, setPayments] = useState([]);
    const [offers, setOffers] = useState([]);
    const [creditScore, setCreditScore] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Credit Score Form State
    const [showManualEntry, setShowManualEntry] = useState(false);
    const [showCalculator, setShowCalculator] = useState(false);
    const [manualScore, setManualScore] = useState('');
    const [calculatorForm, setCalculatorForm] = useState({
        annualIncome: '',
        employmentStatus: 'EMPLOYED',
        existingLoans: '',
        latePayments: '',
        totalDebt: '',
        yearsOfCreditHistory: '',
        hasDefaulted: false
    });
    const [submitting, setSubmitting] = useState(false);

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
        fetchData();
    }, [location.pathname]);

    const fetchData = async () => {
        setLoading(true);
        setError('');
        try {
            if (location.pathname === '/borrower' || location.pathname === '/borrower/') {
                const response = await borrowerAPI.getDashboard();
                setDashboard(response.data.data);
            } else if (location.pathname === '/borrower/loans') {
                const response = await borrowerAPI.getLoans();
                setLoans(response.data.data || []);
            } else if (location.pathname === '/borrower/applications') {
                const response = await borrowerAPI.getApplications();
                setApplications(response.data.data || []);
            } else if (location.pathname === '/borrower/payments') {
                const response = await borrowerAPI.getPayments();
                setPayments(response.data.data || []);
            } else if (location.pathname === '/borrower/offers') {
                const response = await borrowerAPI.getLoanOffers();
                setOffers(response.data.data || []);
            } else if (location.pathname === '/borrower/credit-score') {
                const response = await borrowerAPI.getCreditScore();
                setCreditScore(response.data.data);
            }
        } catch (err) {
            setError('Failed to load data');
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

    const handleManualScoreSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const response = await borrowerAPI.updateCreditScore({
                creditScore: parseInt(manualScore),
                annualIncome: calculatorForm.annualIncome ? parseFloat(calculatorForm.annualIncome) : null,
                employmentStatus: calculatorForm.employmentStatus
            });
            setCreditScore(response.data.data);
            setShowManualEntry(false);
            setManualScore('');
        } catch (err) {
            setError('Failed to update credit score');
        } finally {
            setSubmitting(false);
        }
    };

    const handleCalculateScore = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const response = await borrowerAPI.calculateCreditScore({
                annualIncome: calculatorForm.annualIncome ? parseFloat(calculatorForm.annualIncome) : null,
                employmentStatus: calculatorForm.employmentStatus,
                existingLoans: calculatorForm.existingLoans ? parseInt(calculatorForm.existingLoans) : null,
                latePayments: calculatorForm.latePayments ? parseInt(calculatorForm.latePayments) : null,
                totalDebt: calculatorForm.totalDebt ? parseFloat(calculatorForm.totalDebt) : null,
                yearsOfCreditHistory: calculatorForm.yearsOfCreditHistory ? parseInt(calculatorForm.yearsOfCreditHistory) : null,
                hasDefaulted: calculatorForm.hasDefaulted
            });
            setCreditScore(response.data.data);
            setShowCalculator(false);
        } catch (err) {
            setError('Failed to calculate credit score');
        } finally {
            setSubmitting(false);
        }
    };

    const getCreditScoreColor = (score) => {
        if (!score) return '#6b7280';
        if (score >= 750) return '#10b981';
        if (score >= 700) return '#22c55e';
        if (score >= 650) return '#eab308';
        if (score >= 600) return '#f97316';
        return '#ef4444';
    };

    const getCreditScoreGradient = (score) => {
        if (!score) return 'linear-gradient(135deg, #6b7280, #374151)';
        if (score >= 750) return 'linear-gradient(135deg, #10b981, #059669)';
        if (score >= 700) return 'linear-gradient(135deg, #22c55e, #16a34a)';
        if (score >= 650) return 'linear-gradient(135deg, #eab308, #ca8a04)';
        if (score >= 600) return 'linear-gradient(135deg, #f97316, #ea580c)';
        return 'linear-gradient(135deg, #ef4444, #dc2626)';
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    // Dashboard View
    if (location.pathname === '/borrower' || location.pathname === '/borrower/') {
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
                <div className="fade-in">
                    <div className="page-header">
                        <h1 className="page-title">Borrower Dashboard</h1>
                        <p className="page-subtitle">Track your loans and manage payments</p>
                    </div>

                    <div className="stats-grid">
                        <div className="stat-card">
                            <div className="stat-label">Total Loans</div>
                            <div className="stat-value">{dashboard?.totalLoans || 0}</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-label">Active Loans</div>
                            <div className="stat-value">{dashboard?.activeLoans || 0}</div>
                            <div className="stat-change">In progress</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-label">Completed Loans</div>
                            <div className="stat-value">{dashboard?.completedLoans || 0}</div>
                            <div className="stat-change positive">Fully paid</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-label">Upcoming Payments</div>
                            <div className="stat-value">{dashboard?.upcomingPayments || 0}</div>
                            <div className="stat-change">Due soon</div>
                        </div>
                    </div>

                    <div className="stats-grid">
                        <div className="stat-card">
                            <div className="stat-label">Total Borrowed</div>
                            <div className="stat-value">{formatCurrency(dashboard?.totalBorrowedAmount)}</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-label">Remaining Balance</div>
                            <div className="stat-value">{formatCurrency(dashboard?.totalRemainingBalance)}</div>
                            <div className="stat-change">To be paid</div>
                        </div>
                    </div>

                    <div className="card" style={{ marginTop: '24px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Quick Actions</h3>
                        </div>
                        <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                            <Link to="/borrower/offers" className="btn btn-primary">🔍 Browse Loan Offers</Link>
                            <Link to="/borrower/credit-score" className="btn btn-secondary">📈 Check Credit Score</Link>
                            <Link to="/borrower/payments" className="btn btn-secondary">💵 Payment History</Link>
                            <Link to="/borrower/loans" className="btn btn-secondary">💳 My Loans</Link>
                        </div>
                    </div>
                </div>
                <button className="help-btn" onClick={resetTutorial} title="Show Tutorial">
                    ❓
                </button>
            </>
        );
    }

    // My Loans View
    if (location.pathname === '/borrower/loans') {
        return (
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">My Loans</h1>
                    <p className="page-subtitle">View and manage your active loans</p>
                </div>

                {loans.length === 0 ? (
                    <div className="card">
                        <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                            You don't have any loans yet. <Link to="/borrower/offers">Browse available offers</Link>
                        </p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Loan ID</th>
                                    <th>Amount</th>
                                    <th>Interest Rate</th>
                                    <th>Term</th>
                                    <th>Status</th>
                                    <th>Remaining</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loans.map((loan) => (
                                    <tr key={loan.id}>
                                        <td>#{loan.id}</td>
                                        <td>{formatCurrency(loan.principalAmount)}</td>
                                        <td>{loan.interestRate}%</td>
                                        <td>{loan.termMonths} months</td>
                                        <td>
                                            <span className={`badge badge-${loan.status?.toLowerCase()}`}>
                                                {loan.status}
                                            </span>
                                        </td>
                                        <td>{formatCurrency(loan.remainingBalance)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        );
    }

    // My Applications View
    if (location.pathname === '/borrower/applications') {
        return (
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">My Applications</h1>
                    <p className="page-subtitle">Track your loan application status</p>
                </div>

                {applications.length === 0 ? (
                    <div className="card">
                        <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                            No applications yet. <Link to="/borrower/offers">Apply for a loan</Link>
                        </p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Application ID</th>
                                    <th>Requested Amount</th>
                                    <th>Term</th>
                                    <th>Status</th>
                                    <th>Applied On</th>
                                </tr>
                            </thead>
                            <tbody>
                                {applications.map((app) => (
                                    <tr key={app.id}>
                                        <td>#{app.id}</td>
                                        <td>{formatCurrency(app.requestedAmount)}</td>
                                        <td>{app.requestedTermMonths} months</td>
                                        <td>
                                            <span className={`badge badge-${app.status?.toLowerCase()}`}>
                                                {app.status}
                                            </span>
                                        </td>
                                        <td>{formatDate(app.createdAt)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        );
    }

    // Handle mark payment as paid
    const handleMarkAsPaid = async (paymentId) => {
        const transactionRef = prompt('Enter transaction reference (optional):');
        try {
            setLoading(true);
            await borrowerAPI.markPaymentAsPaid(paymentId, transactionRef);
            alert('Payment marked as paid. Awaiting lender approval.');
            // Refresh payments
            const response = await borrowerAPI.getPayments();
            setPayments(response.data.data || []);
        } catch (err) {
            console.error('Error marking payment:', err);
            alert(err.response?.data?.message || 'Failed to mark payment as paid');
        } finally {
            setLoading(false);
        }
    };

    // Get status badge color
    const getStatusBadge = (status) => {
        const statusColors = {
            'PENDING': 'pending',
            'PENDING_APPROVAL': 'warning',
            'PAID': 'success',
            'COMPLETED': 'success',
            'LATE': 'warning',
            'OVERDUE': 'danger',
            'REJECTED': 'danger',
            'MISSED': 'danger'
        };
        return statusColors[status] || 'secondary';
    };

    // Get status display text
    const getStatusText = (status, payment) => {
        if (status === 'OVERDUE' && payment.lateFee > 0) {
            return `OVERDUE (+${formatCurrency(payment.lateFee)} penalty)`;
        }
        if (status === 'PENDING_APPROVAL') {
            return 'AWAITING APPROVAL';
        }
        return status;
    };

    // Payments View
    if (location.pathname === '/borrower/payments') {
        return (
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">Payment History</h1>
                    <p className="page-subtitle">Track your payment records</p>
                </div>

                {error && <div className="alert alert-error" style={{ marginBottom: '20px' }}>{error}</div>}

                {payments.length === 0 ? (
                    <div className="card">
                        <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                            No payment history yet.
                        </p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>EMI #</th>
                                    <th>Loan ID</th>
                                    <th>Amount</th>
                                    <th>Penalty</th>
                                    <th>Due Date</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {payments.map((payment) => (
                                    <tr key={payment.id}>
                                        <td>#{payment.paymentNumber || payment.id}</td>
                                        <td>#{payment.loanId}</td>
                                        <td>{formatCurrency(payment.amountDue)}</td>
                                        <td style={{ color: payment.lateFee > 0 ? '#ef4444' : 'inherit' }}>
                                            {payment.lateFee > 0 ? formatCurrency(payment.lateFee) : '-'}
                                        </td>
                                        <td>{formatDate(payment.dueDate)}</td>
                                        <td>
                                            <span className={`badge badge-${getStatusBadge(payment.status)}`}>
                                                {getStatusText(payment.status, payment)}
                                            </span>
                                        </td>
                                        <td>
                                            {(payment.status === 'PENDING' || payment.status === 'OVERDUE' || payment.status === 'REJECTED') && (
                                                <button 
                                                    className="btn btn-primary btn-sm"
                                                    onClick={() => handleMarkAsPaid(payment.id)}
                                                    disabled={loading}
                                                    style={{ padding: '6px 12px', fontSize: '12px' }}
                                                >
                                                    💳 Mark as Paid
                                                </button>
                                            )}
                                            {payment.status === 'PENDING_APPROVAL' && (
                                                <span style={{ color: 'var(--accent)', fontSize: '12px' }}>
                                                    ⏳ Waiting for approval
                                                </span>
                                            )}
                                            {(payment.status === 'PAID' || payment.status === 'COMPLETED') && (
                                                <span style={{ color: 'var(--secondary)', fontSize: '12px' }}>
                                                    ✅ Paid
                                                </span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        );
    }

    // Handle loan application
    const handleApplyForLoan = async (offer) => {
        try {
            setLoading(true);
            setError(null);
            await borrowerAPI.submitApplication({
                loanId: offer.id,
                purpose: offer.description || `Applying for Loan #${offer.id}`
            });
            alert('Application submitted successfully! Redirecting to your applications...');
            // Navigate to applications page - useEffect will fetch the data
            navigate('/borrower/applications');
        } catch (err) {
            console.error('Error applying for loan:', err);
            setError(err.response?.data?.message || 'Failed to submit application. You may have already applied for this loan.');
        } finally {
            setLoading(false);
        }
    };

    // Browse Offers View
    if (location.pathname === '/borrower/offers') {
        return (
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">Browse Loan Offers</h1>
                    <p className="page-subtitle">Find the best loan for your needs</p>
                </div>

                {error && <div className="alert alert-error" style={{ marginBottom: '20px' }}>{error}</div>}

                {offers.length === 0 ? (
                    <div className="card">
                        <p style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                            No loan offers available at the moment. Check back later!
                        </p>
                    </div>
                ) : (
                    <div className="stats-grid">
                        {offers.map((offer) => (
                            <div key={offer.id} className="card">
                                <div className="card-header">
                                    <h3 className="card-title">Loan #{offer.id}</h3>
                                    <span className={`badge badge-${offer.status?.toLowerCase()}`}>
                                        {offer.status}
                                    </span>
                                </div>
                                <div style={{ marginBottom: '16px' }}>
                                    <p><strong>Amount:</strong> {formatCurrency(offer.principalAmount)}</p>
                                    <p><strong>Interest Rate:</strong> {offer.interestRate}%</p>
                                    <p><strong>Term:</strong> {offer.termMonths} months</p>
                                    {offer.description && <p><strong>Description:</strong> {offer.description}</p>}
                                </div>
                                <button 
                                    className="btn btn-primary" 
                                    style={{ width: '100%' }}
                                    onClick={() => handleApplyForLoan(offer)}
                                    disabled={loading}
                                >
                                    {loading ? 'Applying...' : 'Apply Now'}
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        );
    }

    // Credit Score View
    if (location.pathname === '/borrower/credit-score') {
        return (
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">Credit Score Check</h1>
                    <p className="page-subtitle">View and manage your credit score</p>
                </div>

                {error && <div className="alert alert-error" style={{ marginBottom: '20px' }}>{error}</div>}

                {/* Credit Score Display */}
                <div className="card" style={{ marginBottom: '24px' }}>
                    <div style={{ 
                        display: 'flex', 
                        flexDirection: 'column', 
                        alignItems: 'center', 
                        padding: '30px',
                        background: getCreditScoreGradient(creditScore?.creditScore),
                        borderRadius: '12px',
                        color: 'white',
                        marginBottom: '24px'
                    }}>
                        <div style={{ fontSize: '14px', marginBottom: '8px', opacity: 0.9 }}>YOUR CREDIT SCORE</div>
                        <div style={{ 
                            fontSize: '72px', 
                            fontWeight: 'bold', 
                            lineHeight: 1,
                            textShadow: '2px 2px 4px rgba(0,0,0,0.2)'
                        }}>
                            {creditScore?.creditScore || '---'}
                        </div>
                        <div style={{ 
                            fontSize: '24px', 
                            marginTop: '8px',
                            fontWeight: '600'
                        }}>
                            {creditScore?.creditRating || 'Not Available'}
                        </div>
                        <div style={{ 
                            fontSize: '14px', 
                            marginTop: '12px',
                            opacity: 0.85,
                            display: 'flex',
                            gap: '16px'
                        }}>
                            <span>Range: 300 - 850</span>
                            {creditScore?.lastUpdated && (
                                <span>Updated: {formatDate(creditScore.lastUpdated)}</span>
                            )}
                        </div>
                    </div>

                    {/* Credit Score Details */}
                    {creditScore?.creditScore && (
                        <div className="stats-grid" style={{ marginBottom: '24px' }}>
                            <div className="stat-card">
                                <div className="stat-label">Risk Level</div>
                                <div className="stat-value" style={{ 
                                    color: creditScore?.riskLevel === 'LOW' ? '#10b981' : 
                                           creditScore?.riskLevel === 'MEDIUM' ? '#eab308' : '#ef4444'
                                }}>
                                    {creditScore?.riskLevel || 'N/A'}
                                </div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Max Loan Eligibility</div>
                                <div className="stat-value">{formatCurrency(creditScore?.maxLoanEligibility)}</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Suggested Interest Rate</div>
                                <div className="stat-value">{creditScore?.suggestedInterestRate || 'N/A'}%</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Employment Status</div>
                                <div className="stat-value" style={{ fontSize: '18px' }}>
                                    {creditScore?.employmentStatus?.replace('_', ' ') || 'Not Set'}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Recommendations */}
                    {creditScore?.recommendations && (
                        <div style={{ 
                            background: 'var(--bg-secondary)', 
                            padding: '16px', 
                            borderRadius: '8px',
                            marginBottom: '24px'
                        }}>
                            <h4 style={{ marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                💡 Recommendations
                            </h4>
                            <p style={{ color: 'var(--text-secondary)', margin: 0 }}>
                                {creditScore.recommendations}
                            </p>
                        </div>
                    )}

                    {/* Action Buttons */}
                    <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <button 
                            className="btn btn-primary"
                            onClick={() => { setShowManualEntry(true); setShowCalculator(false); }}
                        >
                            ✏️ Enter Score Manually
                        </button>
                        <button 
                            className="btn btn-secondary"
                            onClick={() => { setShowCalculator(true); setShowManualEntry(false); }}
                        >
                            🧮 Calculate My Score
                        </button>
                    </div>
                </div>

                {/* Manual Entry Form */}
                {showManualEntry && (
                    <div className="card" style={{ marginBottom: '24px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Enter Credit Score Manually</h3>
                            <button 
                                className="btn btn-secondary" 
                                style={{ padding: '4px 12px' }}
                                onClick={() => setShowManualEntry(false)}
                            >
                                ✕
                            </button>
                        </div>
                        <form onSubmit={handleManualScoreSubmit}>
                            <div className="form-group">
                                <label className="form-label">Credit Score (300-850)</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    value={manualScore}
                                    onChange={(e) => setManualScore(e.target.value)}
                                    min="300"
                                    max="850"
                                    required
                                    placeholder="Enter your credit score"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Annual Income (Optional)</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    value={calculatorForm.annualIncome}
                                    onChange={(e) => setCalculatorForm({...calculatorForm, annualIncome: e.target.value})}
                                    placeholder="Enter your annual income"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Employment Status</label>
                                <select
                                    className="form-input"
                                    value={calculatorForm.employmentStatus}
                                    onChange={(e) => setCalculatorForm({...calculatorForm, employmentStatus: e.target.value})}
                                >
                                    <option value="EMPLOYED">Employed (Full-time)</option>
                                    <option value="PART_TIME">Part-time</option>
                                    <option value="SELF_EMPLOYED">Self-Employed</option>
                                    <option value="BUSINESS_OWNER">Business Owner</option>
                                    <option value="RETIRED">Retired</option>
                                    <option value="UNEMPLOYED">Unemployed</option>
                                </select>
                            </div>
                            <button type="submit" className="btn btn-primary" disabled={submitting}>
                                {submitting ? 'Saving...' : 'Save Credit Score'}
                            </button>
                        </form>
                    </div>
                )}

                {/* Calculator Form */}
                {showCalculator && (
                    <div className="card" style={{ marginBottom: '24px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Calculate Your Credit Score</h3>
                            <button 
                                className="btn btn-secondary" 
                                style={{ padding: '4px 12px' }}
                                onClick={() => setShowCalculator(false)}
                            >
                                ✕
                            </button>
                        </div>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '20px' }}>
                            Fill in your financial details to estimate your credit score. This calculation is based on common credit scoring factors.
                        </p>
                        <form onSubmit={handleCalculateScore}>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px' }}>
                                <div className="form-group">
                                    <label className="form-label">Annual Income ($)</label>
                                    <input
                                        type="number"
                                        className="form-input"
                                        value={calculatorForm.annualIncome}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, annualIncome: e.target.value})}
                                        placeholder="e.g., 50000"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Employment Status</label>
                                    <select
                                        className="form-input"
                                        value={calculatorForm.employmentStatus}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, employmentStatus: e.target.value})}
                                    >
                                        <option value="EMPLOYED">Employed (Full-time)</option>
                                        <option value="PART_TIME">Part-time</option>
                                        <option value="SELF_EMPLOYED">Self-Employed</option>
                                        <option value="BUSINESS_OWNER">Business Owner</option>
                                        <option value="RETIRED">Retired</option>
                                        <option value="UNEMPLOYED">Unemployed</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Total Debt ($)</label>
                                    <input
                                        type="number"
                                        className="form-input"
                                        value={calculatorForm.totalDebt}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, totalDebt: e.target.value})}
                                        placeholder="e.g., 10000"
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Existing Loans</label>
                                    <input
                                        type="number"
                                        className="form-input"
                                        value={calculatorForm.existingLoans}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, existingLoans: e.target.value})}
                                        min="0"
                                        placeholder="Number of active loans"
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Late Payments (last 2 years)</label>
                                    <input
                                        type="number"
                                        className="form-input"
                                        value={calculatorForm.latePayments}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, latePayments: e.target.value})}
                                        min="0"
                                        placeholder="Number of late payments"
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Years of Credit History</label>
                                    <input
                                        type="number"
                                        className="form-input"
                                        value={calculatorForm.yearsOfCreditHistory}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, yearsOfCreditHistory: e.target.value})}
                                        min="0"
                                        placeholder="e.g., 5"
                                    />
                                </div>
                            </div>
                            <div className="form-group" style={{ marginTop: '16px' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        checked={calculatorForm.hasDefaulted}
                                        onChange={(e) => setCalculatorForm({...calculatorForm, hasDefaulted: e.target.checked})}
                                        style={{ width: '18px', height: '18px' }}
                                    />
                                    <span>I have defaulted on a loan in the past</span>
                                </label>
                            </div>
                            <button type="submit" className="btn btn-primary" disabled={submitting} style={{ marginTop: '16px' }}>
                                {submitting ? 'Calculating...' : '🧮 Calculate Credit Score'}
                            </button>
                        </form>
                    </div>
                )}

                {/* Credit Score Guide */}
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">📊 Credit Score Guide</h3>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '12px' }}>
                        <div style={{ padding: '16px', background: 'linear-gradient(135deg, #10b981, #059669)', borderRadius: '8px', color: 'white' }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>800-850</div>
                            <div style={{ fontSize: '14px', opacity: 0.9 }}>Exceptional</div>
                        </div>
                        <div style={{ padding: '16px', background: 'linear-gradient(135deg, #22c55e, #16a34a)', borderRadius: '8px', color: 'white' }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>740-799</div>
                            <div style={{ fontSize: '14px', opacity: 0.9 }}>Very Good</div>
                        </div>
                        <div style={{ padding: '16px', background: 'linear-gradient(135deg, #84cc16, #65a30d)', borderRadius: '8px', color: 'white' }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>670-739</div>
                            <div style={{ fontSize: '14px', opacity: 0.9 }}>Good</div>
                        </div>
                        <div style={{ padding: '16px', background: 'linear-gradient(135deg, #eab308, #ca8a04)', borderRadius: '8px', color: 'white' }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>580-669</div>
                            <div style={{ fontSize: '14px', opacity: 0.9 }}>Fair</div>
                        </div>
                        <div style={{ padding: '16px', background: 'linear-gradient(135deg, #ef4444, #dc2626)', borderRadius: '8px', color: 'white' }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>300-579</div>
                            <div style={{ fontSize: '14px', opacity: 0.9 }}>Poor</div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return null;
};

export default BorrowerDashboard;
