import { useState } from 'react';

const WelcomeModal = ({ user, onClose, onStartTutorial }) => {
    return (
        <div className="modal-overlay">
            <div className="modal welcome-modal">
                <div className="welcome-hero">
                    <div className="welcome-icon">🎉</div>
                    <h1>Welcome to LoanPro!</h1>
                    <p className="welcome-subtitle">
                        Hello <strong>{user?.firstName}</strong>, we're excited to have you here!
                    </p>
                </div>

                <div className="welcome-features">
                    <h3>What you can do:</h3>
                    <div className="feature-grid">
                        {user?.role === 'BORROWER' && (
                            <>
                                <div className="feature-item">
                                    <span className="feature-icon">🔍</span>
                                    <span>Browse loan offers</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">📝</span>
                                    <span>Apply for loans</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">💳</span>
                                    <span>Track your loans</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">💵</span>
                                    <span>Make payments</span>
                                </div>
                            </>
                        )}
                        {user?.role === 'LENDER' && (
                            <>
                                <div className="feature-item">
                                    <span className="feature-icon">➕</span>
                                    <span>Create loan offers</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">📋</span>
                                    <span>Review applications</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">💰</span>
                                    <span>Manage your loans</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">📊</span>
                                    <span>Track earnings</span>
                                </div>
                            </>
                        )}
                        {user?.role === 'ADMIN' && (
                            <>
                                <div className="feature-item">
                                    <span className="feature-icon">👥</span>
                                    <span>Manage users</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">📈</span>
                                    <span>View analytics</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">🛡️</span>
                                    <span>Platform security</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">⚙️</span>
                                    <span>System settings</span>
                                </div>
                            </>
                        )}
                        {user?.role === 'ANALYST' && (
                            <>
                                <div className="feature-item">
                                    <span className="feature-icon">📊</span>
                                    <span>View loan analytics</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">⚠️</span>
                                    <span>Risk assessment</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">💸</span>
                                    <span>Payment reports</span>
                                </div>
                                <div className="feature-item">
                                    <span className="feature-icon">📋</span>
                                    <span>Generate reports</span>
                                </div>
                            </>
                        )}
                    </div>
                </div>

                <div className="welcome-actions">
                    <button className="btn btn-primary" onClick={onStartTutorial}>
                        🎓 Start Tutorial
                    </button>
                    <button className="btn btn-secondary" onClick={onClose}>
                        Skip for now
                    </button>
                </div>
            </div>
        </div>
    );
};

const TutorialModal = ({ user, currentStep, totalSteps, onNext, onPrev, onClose }) => {
    const getTutorialSteps = () => {
        switch (user?.role) {
            case 'BORROWER':
                return [
                    {
                        title: 'Dashboard Overview',
                        description: 'This is your main dashboard. Here you can see a summary of your loans, active balances, and upcoming payments.',
                        icon: '📊',
                        highlight: 'The stats cards show your total loans, active loans, and remaining balance at a glance.'
                    },
                    {
                        title: 'Browse Loan Offers',
                        description: 'Click on "Browse Offers" in the sidebar to see available loan offers from lenders.',
                        icon: '🔍',
                        highlight: 'You can filter by amount, interest rate, and term length to find the best offer for you.'
                    },
                    {
                        title: 'Apply for a Loan',
                        description: 'Found a loan you like? Click "Apply Now" to submit your application. The lender will review and approve or reject it.',
                        icon: '📝',
                        highlight: 'Make sure to fill in accurate information to improve your chances of approval.'
                    },
                    {
                        title: 'Track Your Loans',
                        description: 'Go to "My Loans" to see all your active and completed loans with detailed information.',
                        icon: '💳',
                        highlight: 'You can see payment schedules, remaining balances, and loan terms here.'
                    },
                    {
                        title: 'Make Payments',
                        description: 'Click on "Payments" to view your payment history and make new payments on your loans.',
                        icon: '💵',
                        highlight: 'Pay on time to avoid late fees and maintain a good credit standing!'
                    }
                ];
            case 'LENDER':
                return [
                    {
                        title: 'Lender Dashboard',
                        description: 'Welcome to your lender dashboard! Here you can see your lending portfolio summary.',
                        icon: '📊',
                        highlight: 'Track total loans, active loans, and earnings at a glance.'
                    },
                    {
                        title: 'Create Loan Offers',
                        description: 'Click "Create Loan" to set up new loan offers for borrowers.',
                        icon: '➕',
                        highlight: 'Set the amount, interest rate, and term length for your loan offer.'
                    },
                    {
                        title: 'Review Applications',
                        description: 'Go to "Applications" to see pending loan applications from borrowers.',
                        icon: '📋',
                        highlight: 'Review borrower details and approve or reject applications.'
                    },
                    {
                        title: 'Manage Your Loans',
                        description: 'Track all your active loans and their payment status in "My Loans".',
                        icon: '💰',
                        highlight: 'Monitor repayments and see detailed loan information.'
                    }
                ];
            case 'ADMIN':
                return [
                    {
                        title: 'Admin Dashboard',
                        description: 'Welcome! This dashboard gives you a complete overview of the platform.',
                        icon: '📊',
                        highlight: 'See total users, active loans, and platform-wide statistics.'
                    },
                    {
                        title: 'User Management',
                        description: 'Go to "Users" to view and manage all platform users.',
                        icon: '👥',
                        highlight: 'Enable/disable accounts, update roles, and monitor user activity.'
                    },
                    {
                        title: 'Platform Analytics',
                        description: 'Click "Analytics" to see detailed platform performance metrics.',
                        icon: '📈',
                        highlight: 'Track loan volumes, default rates, and user growth over time.'
                    }
                ];
            case 'ANALYST':
                return [
                    {
                        title: 'Analytics Dashboard',
                        description: 'This is your analytics command center. View comprehensive loan and payment data.',
                        icon: '📊',
                        highlight: 'Access real-time metrics and performance indicators.'
                    },
                    {
                        title: 'Risk Assessment',
                        description: 'Go to "Risk Assessment" to analyze loan risk factors.',
                        icon: '⚠️',
                        highlight: 'Identify high-risk loans and default patterns.'
                    },
                    {
                        title: 'Payment Reports',
                        description: 'Click "Payment Reports" to view payment trends and analytics.',
                        icon: '💸',
                        highlight: 'Track on-time payments, late payments, and collection rates.'
                    },
                    {
                        title: 'Loan Overview',
                        description: 'View all platform loans in "All Loans" for detailed analysis.',
                        icon: '📋',
                        highlight: 'Filter, sort, and export loan data for reporting.'
                    }
                ];
            default:
                return [];
        }
    };

    const steps = getTutorialSteps();
    const step = steps[currentStep];

    if (!step) return null;

    return (
        <div className="modal-overlay">
            <div className="modal tutorial-modal">
                <div className="tutorial-header">
                    <div className="tutorial-progress">
                        <span>Step {currentStep + 1} of {totalSteps}</span>
                        <div className="progress-bar">
                            <div
                                className="progress-fill"
                                style={{ width: `${((currentStep + 1) / totalSteps) * 100}%` }}
                            ></div>
                        </div>
                    </div>
                    <button className="close-btn" onClick={onClose}>×</button>
                </div>

                <div className="tutorial-content">
                    <div className="tutorial-icon">{step.icon}</div>
                    <h2>{step.title}</h2>
                    <p className="tutorial-description">{step.description}</p>
                    <div className="tutorial-highlight">
                        <span className="highlight-icon">💡</span>
                        <span>{step.highlight}</span>
                    </div>
                </div>

                <div className="tutorial-actions">
                    {currentStep > 0 && (
                        <button className="btn btn-secondary" onClick={onPrev}>
                            ← Previous
                        </button>
                    )}
                    {currentStep < totalSteps - 1 ? (
                        <button className="btn btn-primary" onClick={onNext}>
                            Next →
                        </button>
                    ) : (
                        <button className="btn btn-primary" onClick={onClose}>
                            🎉 Get Started!
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export const useOnboarding = (user) => {
    const [showWelcome, setShowWelcome] = useState(false);
    const [showTutorial, setShowTutorial] = useState(false);
    const [tutorialStep, setTutorialStep] = useState(0);

    const checkFirstTimeUser = () => {
        if (!user) return;

        const onboardingKey = `loanpro_onboarding_${user.id}`;
        const hasSeenOnboarding = localStorage.getItem(onboardingKey);

        if (!hasSeenOnboarding) {
            setShowWelcome(true);
        }
    };

    const completeOnboarding = () => {
        if (!user) return;

        const onboardingKey = `loanpro_onboarding_${user.id}`;
        localStorage.setItem(onboardingKey, 'completed');
        setShowWelcome(false);
        setShowTutorial(false);
    };

    const startTutorial = () => {
        setShowWelcome(false);
        setShowTutorial(true);
        setTutorialStep(0);
    };

    const resetTutorial = () => {
        if (!user) return;

        const onboardingKey = `loanpro_onboarding_${user.id}`;
        localStorage.removeItem(onboardingKey);
        setShowWelcome(true);
    };

    const getTotalSteps = () => {
        switch (user?.role) {
            case 'BORROWER': return 5;
            case 'LENDER': return 4;
            case 'ADMIN': return 3;
            case 'ANALYST': return 4;
            default: return 0;
        }
    };

    return {
        showWelcome,
        showTutorial,
        tutorialStep,
        totalSteps: getTotalSteps(),
        checkFirstTimeUser,
        completeOnboarding,
        startTutorial,
        resetTutorial,
        setTutorialStep,
        closeWelcome: () => {
            completeOnboarding();
        },
        closeTutorial: () => {
            completeOnboarding();
        },
        nextStep: () => {
            if (tutorialStep < getTotalSteps() - 1) {
                setTutorialStep(s => s + 1);
            }
        },
        prevStep: () => {
            if (tutorialStep > 0) {
                setTutorialStep(s => s - 1);
            }
        }
    };
};

export { WelcomeModal, TutorialModal };
