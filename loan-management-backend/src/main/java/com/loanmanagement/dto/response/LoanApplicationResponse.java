package com.loanmanagement.dto.response;

import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplicationResponse {
    private Long id;
    private Long borrowerId;
    private String borrowerName;
    private Long loanId;
    private BigDecimal requestedAmount;
    private Integer requestedTermMonths;
    private String purpose;
    private BigDecimal annualIncome;
    private String employmentStatus;
    private Integer creditScore;
    private ApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    
    // Loan offer details
    private BigDecimal loanInterestRate;
    private BigDecimal loanPenaltyRate;
    private String loanDescription;

    public LoanApplicationResponse() {
    }

    public static LoanApplicationResponse fromEntity(LoanApplication app) {
        LoanApplicationResponse r = new LoanApplicationResponse();
        r.id = app.getId();
        r.borrowerId = app.getBorrower() != null ? app.getBorrower().getId() : null;
        r.borrowerName = app.getBorrower() != null ? app.getBorrower().getFullName() : null;
        r.loanId = app.getLoan() != null ? app.getLoan().getId() : null;
        r.requestedAmount = app.getRequestedAmount();
        r.requestedTermMonths = app.getRequestedTermMonths();
        r.purpose = app.getPurpose();
        r.annualIncome = app.getAnnualIncome();
        r.employmentStatus = app.getEmploymentStatus();
        r.creditScore = app.getCreditScore();
        r.status = app.getStatus();
        r.rejectionReason = app.getRejectionReason();
        r.reviewedAt = app.getReviewedAt();
        r.createdAt = app.getCreatedAt();
        
        // Include loan offer details
        if (app.getLoan() != null) {
            r.loanInterestRate = app.getLoan().getInterestRate();
            r.loanPenaltyRate = app.getLoan().getPenaltyRate();
            r.loanDescription = app.getLoan().getDescription();
        }
        return r;
    }
    
    public BigDecimal getLoanInterestRate() {
        return loanInterestRate;
    }
    
    public void setLoanInterestRate(BigDecimal loanInterestRate) {
        this.loanInterestRate = loanInterestRate;
    }
    
    public BigDecimal getLoanPenaltyRate() {
        return loanPenaltyRate;
    }
    
    public void setLoanPenaltyRate(BigDecimal loanPenaltyRate) {
        this.loanPenaltyRate = loanPenaltyRate;
    }
    
    public String getLoanDescription() {
        return loanDescription;
    }
    
    public void setLoanDescription(String loanDescription) {
        this.loanDescription = loanDescription;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
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

    // Builder
    public static LoanApplicationResponseBuilder builder() {
        return new LoanApplicationResponseBuilder();
    }

    public static class LoanApplicationResponseBuilder {
        private Long id;
        private Long borrowerId;
        private String borrowerName;
        private Long loanId;
        private BigDecimal requestedAmount;
        private Integer requestedTermMonths;
        private String purpose;
        private BigDecimal annualIncome;
        private String employmentStatus;
        private Integer creditScore;
        private ApplicationStatus status;
        private String rejectionReason;
        private LocalDateTime reviewedAt;
        private LocalDateTime createdAt;

        public LoanApplicationResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanApplicationResponseBuilder borrowerId(Long borrowerId) {
            this.borrowerId = borrowerId;
            return this;
        }

        public LoanApplicationResponseBuilder borrowerName(String borrowerName) {
            this.borrowerName = borrowerName;
            return this;
        }

        public LoanApplicationResponseBuilder loanId(Long loanId) {
            this.loanId = loanId;
            return this;
        }

        public LoanApplicationResponseBuilder requestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public LoanApplicationResponseBuilder requestedTermMonths(Integer requestedTermMonths) {
            this.requestedTermMonths = requestedTermMonths;
            return this;
        }

        public LoanApplicationResponseBuilder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public LoanApplicationResponseBuilder annualIncome(BigDecimal annualIncome) {
            this.annualIncome = annualIncome;
            return this;
        }

        public LoanApplicationResponseBuilder employmentStatus(String employmentStatus) {
            this.employmentStatus = employmentStatus;
            return this;
        }

        public LoanApplicationResponseBuilder creditScore(Integer creditScore) {
            this.creditScore = creditScore;
            return this;
        }

        public LoanApplicationResponseBuilder status(ApplicationStatus status) {
            this.status = status;
            return this;
        }

        public LoanApplicationResponseBuilder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public LoanApplicationResponseBuilder reviewedAt(LocalDateTime reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public LoanApplicationResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LoanApplicationResponse build() {
            LoanApplicationResponse r = new LoanApplicationResponse();
            r.id = this.id;
            r.borrowerId = this.borrowerId;
            r.borrowerName = this.borrowerName;
            r.loanId = this.loanId;
            r.requestedAmount = this.requestedAmount;
            r.requestedTermMonths = this.requestedTermMonths;
            r.purpose = this.purpose;
            r.annualIncome = this.annualIncome;
            r.employmentStatus = this.employmentStatus;
            r.creditScore = this.creditScore;
            r.status = this.status;
            r.rejectionReason = this.rejectionReason;
            r.reviewedAt = this.reviewedAt;
            r.createdAt = this.createdAt;
            return r;
        }
    }
}
