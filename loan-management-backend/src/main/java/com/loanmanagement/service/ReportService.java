package com.loanmanagement.service;

import com.loanmanagement.dto.response.DashboardResponse;
import com.loanmanagement.entity.*;
import com.loanmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final LoanApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    public ReportService(UserRepository userRepository, LoanRepository loanRepository,
            LoanApplicationRepository applicationRepository, PaymentRepository paymentRepository,
            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.applicationRepository = applicationRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
    }

    public DashboardResponse getAdminDashboard() {
        return DashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalLoans(loanRepository.count())
                .activeLoans(loanRepository.findByStatus(LoanStatus.ACTIVE).size())
                .pendingApplications(applicationRepository.countByStatus(ApplicationStatus.PENDING))
                .totalLoanAmount(sumLoanAmounts())
                .totalPaidAmount(sumPaidAmounts())
                .overdueAmount(sumOverdueAmounts())
                .build();
    }

    public Map<String, Object> getLenderDashboard(User lender) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Loan> lenderLoans = loanRepository.findByLender(lender);

        dashboard.put("totalLoansCreated", lenderLoans.size());
        dashboard.put("activeLoans", lenderLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count());
        dashboard.put("completedLoans",
                lenderLoans.stream().filter(l -> l.getStatus() == LoanStatus.COMPLETED).count());
        dashboard.put("pendingOffers", lenderLoans.stream().filter(l -> l.getStatus() == LoanStatus.PENDING).count());

        // Amount Invested (Total principal amount lent out)
        BigDecimal totalLent = lenderLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.COMPLETED)
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("totalLentAmount", totalLent);

        // Total Amount Distributed (same as totalLent for 12%club)
        dashboard.put("totalDistributed", totalLent);

        // Amount Reclaimed (sum of principal portions from approved/paid payments)
        BigDecimal amountReclaimed = paymentRepository.sumPrincipalReclaimedByLender(lender.getId());
        dashboard.put("amountReclaimed", amountReclaimed != null ? amountReclaimed : BigDecimal.ZERO);

        // Total Interest Earned (sum of interest portions from approved/paid payments)
        BigDecimal interestEarned = paymentRepository.sumInterestEarnedByLender(lender.getId());
        dashboard.put("interestEarned", interestEarned != null ? interestEarned : BigDecimal.ZERO);
        dashboard.put("totalInterestEarned", interestEarned != null ? interestEarned : BigDecimal.ZERO);

        return dashboard;
    }

    public Map<String, Object> getBorrowerDashboard(User borrower) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Loan> borrowerLoans = loanRepository.findByBorrower(borrower);

        dashboard.put("totalLoans", borrowerLoans.size());
        dashboard.put("activeLoans", borrowerLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count());
        dashboard.put("completedLoans",
                borrowerLoans.stream().filter(l -> l.getStatus() == LoanStatus.COMPLETED).count());

        BigDecimal totalBorrowed = borrowerLoans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("totalBorrowedAmount", totalBorrowed);

        BigDecimal totalRemaining = borrowerLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(Loan::getRemainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("totalRemainingBalance", totalRemaining);

        List<Payment> upcomingPayments = paymentRepository.findByBorrowerIdOrderByDueDateAsc(borrower.getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .limit(5)
                .toList();
        dashboard.put("upcomingPayments", upcomingPayments.size());

        return dashboard;
    }

    public Map<String, Object> getLoanAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        Map<String, Long> statusDistribution = new HashMap<>();
        for (LoanStatus status : LoanStatus.values()) {
            statusDistribution.put(status.name(), (long) loanRepository.findByStatus(status).size());
        }
        analytics.put("statusDistribution", statusDistribution);

        List<Loan> allLoans = loanRepository.findAll();
        if (!allLoans.isEmpty()) {
            BigDecimal avgAmount = allLoans.stream()
                    .map(Loan::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allLoans.size()), 2, java.math.RoundingMode.HALF_UP);
            analytics.put("averageLoanAmount", avgAmount);

            BigDecimal avgInterestRate = allLoans.stream()
                    .map(Loan::getInterestRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allLoans.size()), 2, java.math.RoundingMode.HALF_UP);
            analytics.put("averageInterestRate", avgInterestRate);
        }

        long totalCompleted = loanRepository.findByStatus(LoanStatus.COMPLETED).size()
                + loanRepository.findByStatus(LoanStatus.DEFAULTED).size();
        if (totalCompleted > 0) {
            double defaultRate = (double) loanRepository.findByStatus(LoanStatus.DEFAULTED).size() / totalCompleted
                    * 100;
            analytics.put("defaultRate", Math.round(defaultRate * 100.0) / 100.0);
        } else {
            analytics.put("defaultRate", 0);
        }

        return analytics;
    }

    public Map<String, Object> getRiskAssessment() {
        Map<String, Object> riskData = new HashMap<>();

        List<Payment> overduePayments = paymentRepository.findOverduePayments(java.time.LocalDate.now());
        riskData.put("overduePaymentsCount", overduePayments.size());

        BigDecimal overdueAmount = overduePayments.stream()
                .map(Payment::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        riskData.put("totalOverdueAmount", overdueAmount);

        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        long loansAtRisk = 0;
        for (Loan loan : activeLoans) {
            if (paymentRepository.countMissedPaymentsByLoan(loan) > 0) {
                loansAtRisk++;
            }
        }
        riskData.put("loansAtRisk", loansAtRisk);

        if (!activeLoans.isEmpty()) {
            double riskScore = (double) loansAtRisk / activeLoans.size() * 100;
            riskData.put("portfolioRiskScore", Math.round(riskScore * 100.0) / 100.0);
        } else {
            riskData.put("portfolioRiskScore", 0);
        }

        return riskData;
    }

    public Map<String, Object> getPaymentAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        Map<String, Long> statusDistribution = new HashMap<>();
        for (PaymentStatus status : PaymentStatus.values()) {
            statusDistribution.put(status.name(), (long) paymentRepository.findByStatus(status).size());
        }
        analytics.put("statusDistribution", statusDistribution);

        long completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED).size();
        long latePayments = paymentRepository.findByStatus(PaymentStatus.LATE).size();
        long totalProcessed = completedPayments + latePayments;

        if (totalProcessed > 0) {
            double onTimeRate = (double) completedPayments / totalProcessed * 100;
            analytics.put("onTimePaymentRate", Math.round(onTimeRate * 100.0) / 100.0);
        } else {
            analytics.put("onTimePaymentRate", 100.0);
        }

        Map<String, BigDecimal> transactionSummary = new HashMap<>();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        for (TransactionType type : TransactionType.values()) {
            BigDecimal sum = transactionRepository.sumAmountByTypeAndDateRange(type, startOfMonth, now);
            transactionSummary.put(type.name(), sum != null ? sum : BigDecimal.ZERO);
        }
        analytics.put("monthlyTransactionSummary", transactionSummary);

        return analytics;
    }

    private BigDecimal sumLoanAmounts() {
        return loanRepository.findAll().stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumPaidAmounts() {
        return paymentRepository.findByStatus(PaymentStatus.COMPLETED).stream()
                .map(Payment::getAmountPaid)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumOverdueAmounts() {
        return paymentRepository.findOverduePayments(java.time.LocalDate.now()).stream()
                .map(Payment::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
