import { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const OtpVerification = () => {
    const [otp, setOtp] = useState(['', '', '', '', '', '']);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [resendTimer, setResendTimer] = useState(60);
    const [resendLoading, setResendLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const inputRefs = useRef([]);
    const { verifyOtp, resendOtp } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const email = location.state?.email;
    const maskedEmail = location.state?.maskedEmail;
    const isRegistration = location.state?.isRegistration;

    useEffect(() => {
        if (!email) {
            navigate('/login');
            return;
        }
        inputRefs.current[0]?.focus();
    }, [email, navigate]);

    // Resend countdown timer
    useEffect(() => {
        if (resendTimer > 0) {
            const timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
            return () => clearTimeout(timer);
        }
    }, [resendTimer]);

    const handleChange = (index, value) => {
        if (!/^\d*$/.test(value)) return;

        const newOtp = [...otp];
        newOtp[index] = value.slice(-1);
        setOtp(newOtp);
        setError('');

        if (value && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }

        // Auto-submit when all digits entered
        if (value && index === 5) {
            const fullOtp = newOtp.join('');
            if (fullOtp.length === 6) {
                handleSubmit(fullOtp);
            }
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !otp[index] && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
        if (pasted.length === 6) {
            const newOtp = pasted.split('');
            setOtp(newOtp);
            inputRefs.current[5]?.focus();
            handleSubmit(pasted);
        }
    };

    const handleSubmit = async (otpCode) => {
        const code = otpCode || otp.join('');
        if (code.length !== 6) {
            setError('Please enter all 6 digits');
            return;
        }

        setError('');
        setLoading(true);

        try {
            const user = await verifyOtp(email, code);

            switch (user.role) {
                case 'ADMIN': navigate('/admin'); break;
                case 'LENDER': navigate('/lender'); break;
                case 'BORROWER': navigate('/borrower'); break;
                case 'ANALYST': navigate('/analyst'); break;
                default: navigate('/');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid OTP. Please try again.');
            setOtp(['', '', '', '', '', '']);
            inputRefs.current[0]?.focus();
        } finally {
            setLoading(false);
        }
    };

    const handleResend = async () => {
        setResendLoading(true);
        setError('');
        setSuccess('');

        try {
            await resendOtp(email);
            setResendTimer(60);
            setSuccess('A new OTP has been sent to your email');
            setOtp(['', '', '', '', '', '']);
            inputRefs.current[0]?.focus();
            setTimeout(() => setSuccess(''), 4000);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to resend OTP');
        } finally {
            setResendLoading(false);
        }
    };

    if (!email) return null;

    return (
        <div className="auth-container">
            <div className="auth-card otp-card fade-in">
                <div className="auth-logo">
                    <div className="otp-icon-wrap">
                        <div className="otp-icon-bg">
                            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                            </svg>
                        </div>
                    </div>
                    <h1 className="auth-title">
                        {isRegistration ? 'Verify Your Email' : 'Verify Your Identity'}
                    </h1>
                    <p className="auth-subtitle">
                        {isRegistration
                            ? <>We've sent a 6-digit code to verify your email<br /><strong className="otp-email-display">{maskedEmail || email}</strong></>
                            : <>We've sent a 6-digit code to<br /><strong className="otp-email-display">{maskedEmail || email}</strong></>
                        }
                    </p>
                </div>

                {error && (
                    <div className="alert alert-error">
                        <span>⚠️</span>
                        <span>{error}</span>
                    </div>
                )}

                {success && (
                    <div className="alert alert-success">
                        <span>✅</span>
                        <span>{success}</span>
                    </div>
                )}

                <div className="otp-input-container" onPaste={handlePaste}>
                    {otp.map((digit, index) => (
                        <input
                            key={index}
                            ref={(el) => (inputRefs.current[index] = el)}
                            type="text"
                            inputMode="numeric"
                            maxLength={1}
                            value={digit}
                            onChange={(e) => handleChange(index, e.target.value)}
                            onKeyDown={(e) => handleKeyDown(index, e)}
                            className={`otp-digit-input ${digit ? 'filled' : ''} ${error ? 'error' : ''}`}
                            autoComplete="one-time-code"
                            disabled={loading}
                        />
                    ))}
                </div>

                <button
                    className="btn btn-primary otp-verify-btn"
                    onClick={() => handleSubmit()}
                    disabled={loading || otp.join('').length !== 6}
                >
                    {loading ? (
                        <span className="otp-loading">
                            <span className="otp-spinner"></span>
                            Verifying...
                        </span>
                    ) : isRegistration ? 'Verify & Complete Registration' : 'Verify & Sign In'}
                </button>

                <div className="otp-resend-section">
                    <p className="otp-resend-text">Didn't receive the code?</p>
                    {resendTimer > 0 ? (
                        <p className="otp-timer">
                            Resend in <span className="otp-timer-count">{resendTimer}s</span>
                        </p>
                    ) : (
                        <button
                            className="otp-resend-btn"
                            onClick={handleResend}
                            disabled={resendLoading}
                        >
                            {resendLoading ? 'Sending...' : 'Resend Code'}
                        </button>
                    )}
                </div>

                <div className="otp-info-box">
                    <div className="otp-info-icon">💡</div>
                    <div>
                        <p className="otp-info-title">
                            {isRegistration ? 'Verify to complete signup' : 'Check your spam folder'}
                        </p>
                        <p className="otp-info-text">
                            {isRegistration
                                ? 'Enter the code sent to your email to complete your registration. The code expires in 5 minutes.'
                                : "If you don't see the email, check your spam/junk folder. The code expires in 5 minutes."
                            }
                        </p>
                    </div>
                </div>

                <button
                    className="otp-back-btn"
                    onClick={() => navigate(isRegistration ? '/register' : '/login')}
                >
                    ← Back to {isRegistration ? 'Registration' : 'Login'}
                </button>
            </div>
        </div>
    );
};

export default OtpVerification;
