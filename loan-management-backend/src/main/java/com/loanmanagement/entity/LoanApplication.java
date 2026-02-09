package com.loanmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months", nullable = false)
    private Integer requestedTermMonths;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(name = "annual_income", precision = 15, scale = 2)
    private BigDecimal annualIncome;

    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LoanApplication() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getRequestedTermMonths() {
        return requestedTermMonths;
    }

    public void setRequestedTermMonths(Integer requestedTermMonths) {
        this.requestedTermMonths = requestedTermMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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

    // Builder pattern
    public static LoanApplicationBuilder builder() {
        return new LoanApplicationBuilder();
    }

    public static class LoanApplicationBuilder {
        private Long id;
        private User borrower;
        private Loan loan;
        private BigDecimal requestedAmount;
        private Integer requestedTermMonths;
        private String purpose;
        private BigDecimal annualIncome;
        private String employmentStatus;
        private Integer creditScore;
        private ApplicationStatus status = ApplicationStatus.PENDING;

        public LoanApplicationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanApplicationBuilder borrower(User borrower) {
            this.borrower = borrower;
            return this;
        }

        public LoanApplicationBuilder loan(Loan loan) {
            this.loan = loan;
            return this;
        }

        public LoanApplicationBuilder requestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public LoanApplicationBuilder requestedTermMonths(Integer requestedTermMonths) {
            this.requestedTermMonths = requestedTermMonths;
            return this;
        }

        public LoanApplicationBuilder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public LoanApplicationBuilder annualIncome(BigDecimal annualIncome) {
            this.annualIncome = annualIncome;
            return this;
        }

        public LoanApplicationBuilder employmentStatus(String employmentStatus) {
            this.employmentStatus = employmentStatus;
            return this;
        }

        public LoanApplicationBuilder creditScore(Integer creditScore) {
            this.creditScore = creditScore;
            return this;
        }

        public LoanApplicationBuilder status(ApplicationStatus status) {
            this.status = status;
            return this;
        }

        public LoanApplication build() {
            LoanApplication app = new LoanApplication();
            app.id = this.id;
            app.borrower = this.borrower;
            app.loan = this.loan;
            app.requestedAmount = this.requestedAmount;
            app.requestedTermMonths = this.requestedTermMonths;
            app.purpose = this.purpose;
            app.annualIncome = this.annualIncome;
            app.employmentStatus = this.employmentStatus;
            app.creditScore = this.creditScore;
            app.status = this.status;
            return app;
        }
    }
}
