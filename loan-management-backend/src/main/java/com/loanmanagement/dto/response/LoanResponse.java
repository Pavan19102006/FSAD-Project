package com.loanmanagement.dto.response;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanResponse {
    private Long id;
    private Long lenderId;
    private String lenderName;
    private Long borrowerId;
    private String borrowerName;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal totalInterest;
    private BigDecimal remainingBalance;
    private LoanStatus status;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    public LoanResponse() {
    }

    public static LoanResponse fromEntity(Loan loan) {
        LoanResponse r = new LoanResponse();
        r.id = loan.getId();
        r.lenderId = loan.getLender() != null ? loan.getLender().getId() : null;
        r.lenderName = loan.getLender() != null ? loan.getLender().getFullName() : null;
        r.borrowerId = loan.getBorrower() != null ? loan.getBorrower().getId() : null;
        r.borrowerName = loan.getBorrower() != null ? loan.getBorrower().getFullName() : null;
        r.principalAmount = loan.getPrincipalAmount();
        r.interestRate = loan.getInterestRate();
        r.termMonths = loan.getTermMonths();
        r.monthlyPayment = loan.getMonthlyPayment();
        r.totalInterest = loan.getTotalInterest();
        r.remainingBalance = loan.getRemainingBalance();
        r.status = loan.getStatus();
        r.description = loan.getDescription();
        r.startDate = loan.getStartDate();
        r.endDate = loan.getEndDate();
        r.createdAt = loan.getCreatedAt();
        return r;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLenderId() {
        return lenderId;
    }

    public void setLenderId(Long lenderId) {
        this.lenderId = lenderId;
    }

    public String getLenderName() {
        return lenderName;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
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

    // Builder
    public static LoanResponseBuilder builder() {
        return new LoanResponseBuilder();
    }

    public static class LoanResponseBuilder {
        private Long id;
        private Long lenderId;
        private String lenderName;
        private Long borrowerId;
        private String borrowerName;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private Integer termMonths;
        private BigDecimal monthlyPayment;
        private BigDecimal totalInterest;
        private BigDecimal remainingBalance;
        private LoanStatus status;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;

        public LoanResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanResponseBuilder lenderId(Long lenderId) {
            this.lenderId = lenderId;
            return this;
        }

        public LoanResponseBuilder lenderName(String lenderName) {
            this.lenderName = lenderName;
            return this;
        }

        public LoanResponseBuilder borrowerId(Long borrowerId) {
            this.borrowerId = borrowerId;
            return this;
        }

        public LoanResponseBuilder borrowerName(String borrowerName) {
            this.borrowerName = borrowerName;
            return this;
        }

        public LoanResponseBuilder principalAmount(BigDecimal principalAmount) {
            this.principalAmount = principalAmount;
            return this;
        }

        public LoanResponseBuilder interestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        public LoanResponseBuilder termMonths(Integer termMonths) {
            this.termMonths = termMonths;
            return this;
        }

        public LoanResponseBuilder monthlyPayment(BigDecimal monthlyPayment) {
            this.monthlyPayment = monthlyPayment;
            return this;
        }

        public LoanResponseBuilder totalInterest(BigDecimal totalInterest) {
            this.totalInterest = totalInterest;
            return this;
        }

        public LoanResponseBuilder remainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
            return this;
        }

        public LoanResponseBuilder status(LoanStatus status) {
            this.status = status;
            return this;
        }

        public LoanResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LoanResponseBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public LoanResponseBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public LoanResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LoanResponse build() {
            LoanResponse r = new LoanResponse();
            r.id = this.id;
            r.lenderId = this.lenderId;
            r.lenderName = this.lenderName;
            r.borrowerId = this.borrowerId;
            r.borrowerName = this.borrowerName;
            r.principalAmount = this.principalAmount;
            r.interestRate = this.interestRate;
            r.termMonths = this.termMonths;
            r.monthlyPayment = this.monthlyPayment;
            r.totalInterest = this.totalInterest;
            r.remainingBalance = this.remainingBalance;
            r.status = this.status;
            r.description = this.description;
            r.startDate = this.startDate;
            r.endDate = this.endDate;
            r.createdAt = this.createdAt;
            return r;
        }
    }
}
