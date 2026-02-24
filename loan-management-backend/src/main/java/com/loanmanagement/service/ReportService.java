package com.loanmanagement.service;

import com.loanmanagement.dto.response.DashboardResponse;
import com.loanmanagement.entity.*;
import com.loanmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        // Status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (PaymentStatus status : PaymentStatus.values()) {
            statusDistribution.put(status.name(), (long) paymentRepository.findByStatus(status).size());
        }
        analytics.put("statusDistribution", statusDistribution);

        // Total counts the frontend needs
        List<Payment> allPayments = paymentRepository.findAll();
        long totalPayments = allPayments.size();
        long completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED).size();
        long latePayments = paymentRepository.findByStatus(PaymentStatus.LATE).size();
        long pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING).size();
        long missedPayments = paymentRepository.findByStatus(PaymentStatus.MISSED).size();
        long totalProcessed = completedPayments + latePayments;

        analytics.put("totalPayments", totalPayments);
        analytics.put("completedPayments", completedPayments);
        analytics.put("pendingPayments", pendingPayments);
        analytics.put("latePayments", latePayments);
        analytics.put("missedPayments", missedPayments);

        // Financial totals
        BigDecimal totalAmountCollected = allPayments.stream()
                .filter(p -> p.getAmountPaid() != null
                        && (p.getStatus() == PaymentStatus.COMPLETED || p.getStatus() == PaymentStatus.LATE))
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.put("totalAmountCollected", totalAmountCollected);

        BigDecimal totalAmountPending = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.OVERDUE)
                .map(Payment::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.put("totalAmountPending", totalAmountPending);

        if (totalPayments > 0) {
            BigDecimal totalAmountDue = allPayments.stream()
                    .map(Payment::getAmountDue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            analytics.put("averagePaymentAmount",
                    totalAmountDue.divide(BigDecimal.valueOf(totalPayments), 2, java.math.RoundingMode.HALF_UP));
        } else {
            analytics.put("averagePaymentAmount", BigDecimal.ZERO);
        }

        // On-time rate
        if (totalProcessed > 0) {
            double onTimeRate = (double) completedPayments / totalProcessed * 100;
            analytics.put("onTimePaymentRate", Math.round(onTimeRate * 100.0) / 100.0);
        } else {
            analytics.put("onTimePaymentRate", 100.0);
        }

        // Monthly transaction summary
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

    /**
     * Returns monthly aggregated data over the last 12 months for trend charts.
     */
    public List<Map<String, Object>> getMonthlyTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MMM yy");

        List<Loan> allLoans = loanRepository.findAll();
        List<Payment> allPayments = paymentRepository.findAll();

        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();

            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", ym.format(monthFormat));

            // New loans created this month (by startDate)
            long newLoans = allLoans.stream()
                    .filter(l -> l.getStartDate() != null &&
                            !l.getStartDate().isBefore(monthStart) &&
                            !l.getStartDate().isAfter(monthEnd))
                    .count();
            monthData.put("newLoans", newLoans);

            // Loan volume (principal) this month
            BigDecimal loanVolume = allLoans.stream()
                    .filter(l -> l.getStartDate() != null &&
                            !l.getStartDate().isBefore(monthStart) &&
                            !l.getStartDate().isAfter(monthEnd))
                    .map(Loan::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("loanVolume", loanVolume);

            // Payments collected this month (completed/late payments with paidDate)
            BigDecimal collected = allPayments.stream()
                    .filter(p -> p.getPaidDate() != null &&
                            !p.getPaidDate().isBefore(monthStart) &&
                            !p.getPaidDate().isAfter(monthEnd) &&
                            (p.getStatus() == PaymentStatus.COMPLETED || p.getStatus() == PaymentStatus.LATE))
                    .map(Payment::getAmountPaid)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("paymentsCollected", collected);

            // Interest earned this month
            BigDecimal interest = allPayments.stream()
                    .filter(p -> p.getPaidDate() != null &&
                            !p.getPaidDate().isBefore(monthStart) &&
                            !p.getPaidDate().isAfter(monthEnd) &&
                            (p.getStatus() == PaymentStatus.COMPLETED || p.getStatus() == PaymentStatus.LATE))
                    .map(Payment::getInterestPortion)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("interestEarned", interest);

            // Active loans count at end of month
            long activeCount = allLoans.stream()
                    .filter(l -> l.getStartDate() != null && l.getEndDate() != null &&
                            !l.getStartDate().isAfter(monthEnd) &&
                            !l.getEndDate().isBefore(monthStart) &&
                            (l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.COMPLETED ||
                                    l.getStatus() == LoanStatus.DEFAULTED))
                    .count();
            monthData.put("activeLoans", activeCount);

            // Cumulative portfolio value (sum of remaining balance of loans active at this
            // point)
            BigDecimal portfolioValue = allLoans.stream()
                    .filter(l -> l.getStartDate() != null &&
                            !l.getStartDate().isAfter(monthEnd) &&
                            (l.getStatus() == LoanStatus.ACTIVE ||
                                    (l.getEndDate() != null && !l.getEndDate().isBefore(monthStart))))
                    .map(Loan::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("portfolioValue", portfolioValue);

            trends.add(monthData);
        }

        return trends;
    }
}
