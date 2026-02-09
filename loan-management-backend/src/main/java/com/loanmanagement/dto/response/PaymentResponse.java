package com.loanmanagement.dto.response;

import com.loanmanagement.entity.Payment;
import com.loanmanagement.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long loanId;
    private Integer paymentNumber;
    private BigDecimal amountDue;
    private BigDecimal principalPortion;
    private BigDecimal interestPortion;
    private BigDecimal amountPaid;
    private BigDecimal lateFee;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private PaymentStatus status;
    private boolean overdue;
    private LocalDateTime createdAt;

    public PaymentResponse() {
    }

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse r = new PaymentResponse();
        r.id = payment.getId();
        r.loanId = payment.getLoan() != null ? payment.getLoan().getId() : null;
        r.paymentNumber = payment.getPaymentNumber();
        r.amountDue = payment.getAmountDue();
        r.principalPortion = payment.getPrincipalPortion();
        r.interestPortion = payment.getInterestPortion();
        r.amountPaid = payment.getAmountPaid();
        r.lateFee = payment.getLateFee();
        r.dueDate = payment.getDueDate();
        r.paidDate = payment.getPaidDate();
        r.status = payment.getStatus();
        r.overdue = payment.isOverdue();
        r.createdAt = payment.getCreatedAt();
        return r;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Integer getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(Integer paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public BigDecimal getPrincipalPortion() {
        return principalPortion;
    }

    public void setPrincipalPortion(BigDecimal principalPortion) {
        this.principalPortion = principalPortion;
    }

    public BigDecimal getInterestPortion() {
        return interestPortion;
    }

    public void setInterestPortion(BigDecimal interestPortion) {
        this.interestPortion = interestPortion;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }

    public static class PaymentResponseBuilder {
        private Long id;
        private Long loanId;
        private Integer paymentNumber;
        private BigDecimal amountDue;
        private BigDecimal principalPortion;
        private BigDecimal interestPortion;
        private BigDecimal amountPaid;
        private BigDecimal lateFee;
        private LocalDate dueDate;
        private LocalDate paidDate;
        private PaymentStatus status;
        private boolean overdue;
        private LocalDateTime createdAt;

        public PaymentResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PaymentResponseBuilder loanId(Long loanId) {
            this.loanId = loanId;
            return this;
        }

        public PaymentResponseBuilder paymentNumber(Integer paymentNumber) {
            this.paymentNumber = paymentNumber;
            return this;
        }

        public PaymentResponseBuilder amountDue(BigDecimal amountDue) {
            this.amountDue = amountDue;
            return this;
        }

        public PaymentResponseBuilder principalPortion(BigDecimal principalPortion) {
            this.principalPortion = principalPortion;
            return this;
        }

        public PaymentResponseBuilder interestPortion(BigDecimal interestPortion) {
            this.interestPortion = interestPortion;
            return this;
        }

        public PaymentResponseBuilder amountPaid(BigDecimal amountPaid) {
            this.amountPaid = amountPaid;
            return this;
        }

        public PaymentResponseBuilder lateFee(BigDecimal lateFee) {
            this.lateFee = lateFee;
            return this;
        }

        public PaymentResponseBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public PaymentResponseBuilder paidDate(LocalDate paidDate) {
            this.paidDate = paidDate;
            return this;
        }

        public PaymentResponseBuilder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public PaymentResponseBuilder overdue(boolean overdue) {
            this.overdue = overdue;
            return this;
        }

        public PaymentResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PaymentResponse build() {
            PaymentResponse r = new PaymentResponse();
            r.id = this.id;
            r.loanId = this.loanId;
            r.paymentNumber = this.paymentNumber;
            r.amountDue = this.amountDue;
            r.principalPortion = this.principalPortion;
            r.interestPortion = this.interestPortion;
            r.amountPaid = this.amountPaid;
            r.lateFee = this.lateFee;
            r.dueDate = this.dueDate;
            r.paidDate = this.paidDate;
            r.status = this.status;
            r.overdue = this.overdue;
            r.createdAt = this.createdAt;
            return r;
        }
    }
}
