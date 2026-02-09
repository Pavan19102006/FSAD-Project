package com.loanmanagement.service;

import com.loanmanagement.entity.*;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.PaymentRepository;
import com.loanmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating borrower risk scores.
 * Risk Score: 0-100 (higher = more risk)
 * 0-30: LOW, 31-60: MEDIUM, 61-80: HIGH, 81-100: CRITICAL
 */
@Service
@SuppressWarnings("unused")
public class RiskScoringService {

    private static final Logger logger = LoggerFactory.getLogger(RiskScoringService.class);

    // Weight factors for risk calculation (total = 100%)
    private static final double PAYMENT_HISTORY_WEIGHT = 0.40; // 40%
    private static final double LOAN_AMOUNT_WEIGHT = 0.20; // 20%
    private static final double TENURE_WEIGHT = 0.15; // 15%
    private static final double EXISTING_LOANS_WEIGHT = 0.15; // 15%
    private static final double DEFAULT_HISTORY_WEIGHT = 0.10; // 10%

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;

    public RiskScoringService(UserRepository userRepository,
            LoanRepository loanRepository,
            PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Calculate risk score for a borrower
     */
    @Transactional(readOnly = true)
    public RiskAssessment calculateRiskScore(User borrower) {
        logger.info("Calculating risk score for borrower: {}", borrower.getId());

        Map<String, Double> componentScores = new HashMap<>();

        // 1. Payment History Score (0-100, lower is better for on-time payments)
        double paymentScore = calculatePaymentHistoryScore(borrower);
        componentScores.put("paymentHistory", paymentScore);

        // 2. Loan Amount Score (higher amounts = higher risk)
        double loanAmountScore = calculateLoanAmountScore(borrower);
        componentScores.put("loanAmount", loanAmountScore);

        // 3. Tenure Score (longer tenure = higher risk)
        double tenureScore = calculateTenureScore(borrower);
        componentScores.put("tenure", tenureScore);

        // 4. Existing Loans Score (more active loans = higher risk)
        double existingLoansScore = calculateExistingLoansScore(borrower);
        componentScores.put("existingLoans", existingLoansScore);

        // 5. Default History Score (any defaults = very high risk)
        double defaultScore = calculateDefaultHistoryScore(borrower);
        componentScores.put("defaultHistory", defaultScore);

        // Calculate weighted total
        double totalScore = (paymentScore * PAYMENT_HISTORY_WEIGHT) +
                (loanAmountScore * LOAN_AMOUNT_WEIGHT) +
                (tenureScore * TENURE_WEIGHT) +
                (existingLoansScore * EXISTING_LOANS_WEIGHT) +
                (defaultScore * DEFAULT_HISTORY_WEIGHT);

        // Ensure score is between 0 and 100
        totalScore = Math.max(0, Math.min(100, totalScore));

        // Determine risk level
        RiskLevel riskLevel = determineRiskLevel(totalScore);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setBorrowerId(borrower.getId());
        assessment.setRiskScore(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP));
        assessment.setRiskLevel(riskLevel);
        assessment.setComponentScores(componentScores);
        assessment.setRecommendation(generateRecommendation(riskLevel, componentScores));

        logger.info("Risk score for borrower {}: {} ({})", borrower.getId(), totalScore, riskLevel);
        return assessment;
    }

    /**
     * Calculate payment history score
     * More on-time payments = lower score (less risk)
     */
    private double calculatePaymentHistoryScore(User borrower) {
        List<Payment> payments = paymentRepository.findByBorrower(borrower);

        if (payments.isEmpty()) {
            return 50.0; // Neutral score for new borrowers
        }

        long totalPayments = payments.size();
        long onTimePayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .filter(p -> p.getPaidDate() != null && !p.getPaidDate().isAfter(p.getDueDate()))
                .count();
        long overduePayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.OVERDUE ||
                        (p.getStatus() == PaymentStatus.PAID &&
                                p.getPaidDate() != null && p.getPaidDate().isAfter(p.getDueDate())))
                .count();

        // On-time ratio (higher = lower risk)
        double onTimeRatio = (double) onTimePayments / totalPayments;

        // Invert: 100% on-time = 0 risk, 0% on-time = 100 risk
        return (1 - onTimeRatio) * 100;
    }

    /**
     * Calculate loan amount score
     * Higher total borrowed = higher risk
     */
    private double calculateLoanAmountScore(User borrower) {
        List<Loan> loans = loanRepository.findByBorrower(borrower);

        if (loans.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalBorrowed = loans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Scoring: <10K = low risk, 10K-50K = medium, 50K-100K = high, >100K = very
        // high
        double amount = totalBorrowed.doubleValue();
        if (amount < 10000)
            return 20.0;
        if (amount < 50000)
            return 40.0;
        if (amount < 100000)
            return 60.0;
        if (amount < 250000)
            return 80.0;
        return 100.0;
    }

    /**
     * Calculate tenure score
     * Longer total remaining tenure = higher risk
     */
    private double calculateTenureScore(User borrower) {
        List<Loan> activeLoans = loanRepository.findByBorrowerAndStatus(borrower, LoanStatus.ACTIVE);

        if (activeLoans.isEmpty()) {
            return 0.0;
        }

        int totalRemainingMonths = activeLoans.stream()
                .mapToInt(loan -> {
                    if (loan.getEndDate() == null)
                        return 0;
                    long months = java.time.temporal.ChronoUnit.MONTHS.between(
                            java.time.LocalDate.now(), loan.getEndDate());
                    return Math.max(0, (int) months);
                })
                .sum();

        // Scoring: <12 months = low, 12-36 = medium, 36-60 = high, >60 = very high
        if (totalRemainingMonths < 12)
            return 20.0;
        if (totalRemainingMonths < 36)
            return 40.0;
        if (totalRemainingMonths < 60)
            return 60.0;
        if (totalRemainingMonths < 120)
            return 80.0;
        return 100.0;
    }

    /**
     * Calculate existing loans score
     * More active loans = higher risk
     */
    private double calculateExistingLoansScore(User borrower) {
        long activeLoans = loanRepository.findByBorrowerAndStatus(borrower, LoanStatus.ACTIVE).size();

        // Scoring: 0-1 = low, 2 = medium, 3 = high, 4+ = very high
        if (activeLoans <= 1)
            return 20.0;
        if (activeLoans == 2)
            return 40.0;
        if (activeLoans == 3)
            return 70.0;
        return 100.0;
    }

    /**
     * Calculate default history score
     * Any defaults = very high risk
     */
    private double calculateDefaultHistoryScore(User borrower) {
        long defaultedLoans = loanRepository.findByBorrowerAndStatus(borrower, LoanStatus.DEFAULTED).size();

        if (defaultedLoans == 0)
            return 0.0;
        if (defaultedLoans == 1)
            return 80.0;
        return 100.0; // Multiple defaults = maximum risk
    }

    /**
     * Determine risk level from score
     */
    private RiskLevel determineRiskLevel(double score) {
        if (score <= 30)
            return RiskLevel.LOW;
        if (score <= 60)
            return RiskLevel.MEDIUM;
        if (score <= 80)
            return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    /**
     * Generate recommendation based on risk assessment
     */
    private String generateRecommendation(RiskLevel level, Map<String, Double> components) {
        StringBuilder sb = new StringBuilder();

        switch (level) {
            case LOW:
                sb.append("Low risk borrower. Eligible for premium loan terms.");
                break;
            case MEDIUM:
                sb.append("Moderate risk. Standard loan terms apply. ");
                break;
            case HIGH:
                sb.append("High risk borrower. Consider requiring collateral or guarantor. ");
                break;
            case CRITICAL:
                sb.append("Critical risk. Loan approval not recommended. ");
                break;
        }

        // Add specific concerns
        if (components.get("paymentHistory") > 60) {
            sb.append("Payment history shows concerns. ");
        }
        if (components.get("defaultHistory") > 0) {
            sb.append("Has previous default(s). ");
        }
        if (components.get("existingLoans") > 60) {
            sb.append("Has multiple active loans. ");
        }

        return sb.toString();
    }

    /**
     * Risk Assessment Result DTO
     */
    public static class RiskAssessment {
        private Long borrowerId;
        private BigDecimal riskScore;
        private RiskLevel riskLevel;
        private Map<String, Double> componentScores;
        private String recommendation;

        // Getters and Setters
        public Long getBorrowerId() {
            return borrowerId;
        }

        public void setBorrowerId(Long borrowerId) {
            this.borrowerId = borrowerId;
        }

        public BigDecimal getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(BigDecimal riskScore) {
            this.riskScore = riskScore;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }

        public Map<String, Double> getComponentScores() {
            return componentScores;
        }

        public void setComponentScores(Map<String, Double> componentScores) {
            this.componentScores = componentScores;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }

    /**
     * Risk Level Enum
     */
    public enum RiskLevel {
        LOW, // 0-30: 🟢
        MEDIUM, // 31-60: 🟡
        HIGH, // 61-80: 🟠
        CRITICAL // 81-100: 🔴
    }
}
