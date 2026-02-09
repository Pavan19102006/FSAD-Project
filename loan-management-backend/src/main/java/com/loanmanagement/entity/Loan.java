package com.loanmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id")
    private User borrower;

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "monthly_payment", precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "total_interest", precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false)
    private InterestType interestType = InterestType.COMPOUND;

    @Enumerated(EnumType.STRING)
    @Column(name = "emi_type", nullable = false)
    private EMIType emiType = EMIType.REDUCING_BALANCE;

    @Column(name = "penalty_rate", precision = 5, scale = 2)
    private BigDecimal penaltyRate = new BigDecimal("2.00");

    @Column(name = "total_penalty_accrued", precision = 15, scale = 2)
    private BigDecimal totalPenaltyAccrued = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Loan() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (remainingBalance == null && principalAmount != null) {
            remainingBalance = principalAmount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getLender() {
        return lender;
    }

    public void setLender(User lender) {
        this.lender = lender;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public InterestType getInterestType() {
        return interestType;
    }

    public void setInterestType(InterestType interestType) {
        this.interestType = interestType;
    }

    public EMIType getEmiType() {
        return emiType;
    }

    public void setEmiType(EMIType emiType) {
        this.emiType = emiType;
    }

    public BigDecimal getPenaltyRate() {
        return penaltyRate;
    }

    public void setPenaltyRate(BigDecimal penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    public BigDecimal getTotalPenaltyAccrued() {
        return totalPenaltyAccrued;
    }

    public void setTotalPenaltyAccrued(BigDecimal totalPenaltyAccrued) {
        this.totalPenaltyAccrued = totalPenaltyAccrued;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Calculate monthly payment using amortization formula
    public BigDecimal calculateMonthlyPayment() {
        if (principalAmount == null || interestRate == null || termMonths == null)
            return BigDecimal.ZERO;
        double r = interestRate.doubleValue() / 100 / 12;
        int n = termMonths;
        double p = principalAmount.doubleValue();
        if (r == 0)
            return principalAmount.divide(BigDecimal.valueOf(n), 2, java.math.RoundingMode.HALF_UP);
        double payment = p * (r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(payment).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalInterest() {
        BigDecimal monthly = calculateMonthlyPayment();
        BigDecimal total = monthly.multiply(BigDecimal.valueOf(termMonths));
        return total.subtract(principalAmount).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // Builder pattern
    public static LoanBuilder builder() {
        return new LoanBuilder();
    }

    public static class LoanBuilder {
        private Long id;
        private User lender;
        private User borrower;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private Integer termMonths;
        private BigDecimal monthlyPayment;
        private BigDecimal totalInterest;
        private BigDecimal remainingBalance;
        private LoanStatus status = LoanStatus.PENDING;
        private InterestType interestType = InterestType.COMPOUND;
        private EMIType emiType = EMIType.REDUCING_BALANCE;
        private BigDecimal penaltyRate = new BigDecimal("2.00");
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;

        public LoanBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanBuilder lender(User lender) {
            this.lender = lender;
            return this;
        }

        public LoanBuilder borrower(User borrower) {
            this.borrower = borrower;
            return this;
        }

        public LoanBuilder principalAmount(BigDecimal principalAmount) {
            this.principalAmount = principalAmount;
            return this;
        }

        public LoanBuilder interestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        public LoanBuilder termMonths(Integer termMonths) {
            this.termMonths = termMonths;
            return this;
        }

        public LoanBuilder monthlyPayment(BigDecimal monthlyPayment) {
            this.monthlyPayment = monthlyPayment;
            return this;
        }

        public LoanBuilder totalInterest(BigDecimal totalInterest) {
            this.totalInterest = totalInterest;
            return this;
        }

        public LoanBuilder remainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
            return this;
        }

        public LoanBuilder status(LoanStatus status) {
            this.status = status;
            return this;
        }

        public LoanBuilder interestType(InterestType interestType) {
            this.interestType = interestType;
            return this;
        }

        public LoanBuilder emiType(EMIType emiType) {
            this.emiType = emiType;
            return this;
        }

        public LoanBuilder penaltyRate(BigDecimal penaltyRate) {
            this.penaltyRate = penaltyRate;
            return this;
        }

        public LoanBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LoanBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public LoanBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Loan build() {
            Loan loan = new Loan();
            loan.id = this.id;
            loan.lender = this.lender;
            loan.borrower = this.borrower;
            loan.principalAmount = this.principalAmount;
            loan.interestRate = this.interestRate;
            loan.termMonths = this.termMonths;
            loan.monthlyPayment = this.monthlyPayment;
            loan.totalInterest = this.totalInterest;
            loan.remainingBalance = this.remainingBalance;
            loan.status = this.status;
            loan.interestType = this.interestType;
            loan.emiType = this.emiType;
            loan.penaltyRate = this.penaltyRate;
            loan.description = this.description;
            loan.startDate = this.startDate;
            loan.endDate = this.endDate;
            return loan;
        }
    }
}
