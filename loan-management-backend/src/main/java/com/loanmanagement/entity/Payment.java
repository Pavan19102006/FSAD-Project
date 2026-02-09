package com.loanmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "payment_number", nullable = false)
    private Integer paymentNumber;

    @Column(name = "amount_due", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "principal_portion", precision = 15, scale = 2)
    private BigDecimal principalPortion;

    @Column(name = "interest_portion", precision = 15, scale = 2)
    private BigDecimal interestPortion;

    @Column(name = "amount_paid", precision = 15, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "late_fee", precision = 15, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "prepayment_amount", precision = 15, scale = 2)
    private BigDecimal prepaymentAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // BANK_TRANSFER, UPI, CARD, CASH, etc.

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment() {
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

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getPrepaymentAmount() {
        return prepaymentAmount;
    }

    public void setPrepaymentAmount(BigDecimal prepaymentAmount) {
        this.prepaymentAmount = prepaymentAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isOverdue() {
        return status == PaymentStatus.PENDING && dueDate.isBefore(LocalDate.now());
    }

    // Builder pattern
    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private Loan loan;
        private Integer paymentNumber;
        private BigDecimal amountDue;
        private BigDecimal principalPortion;
        private BigDecimal interestPortion;
        private BigDecimal amountPaid;
        private BigDecimal lateFee;
        private LocalDate dueDate;
        private LocalDate paidDate;
        private PaymentStatus status = PaymentStatus.PENDING;

        public PaymentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PaymentBuilder loan(Loan loan) {
            this.loan = loan;
            return this;
        }

        public PaymentBuilder paymentNumber(Integer paymentNumber) {
            this.paymentNumber = paymentNumber;
            return this;
        }

        public PaymentBuilder amountDue(BigDecimal amountDue) {
            this.amountDue = amountDue;
            return this;
        }

        public PaymentBuilder principalPortion(BigDecimal principalPortion) {
            this.principalPortion = principalPortion;
            return this;
        }

        public PaymentBuilder interestPortion(BigDecimal interestPortion) {
            this.interestPortion = interestPortion;
            return this;
        }

        public PaymentBuilder amountPaid(BigDecimal amountPaid) {
            this.amountPaid = amountPaid;
            return this;
        }

        public PaymentBuilder lateFee(BigDecimal lateFee) {
            this.lateFee = lateFee;
            return this;
        }

        public PaymentBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public PaymentBuilder paidDate(LocalDate paidDate) {
            this.paidDate = paidDate;
            return this;
        }

        public PaymentBuilder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Payment build() {
            Payment p = new Payment();
            p.id = this.id;
            p.loan = this.loan;
            p.paymentNumber = this.paymentNumber;
            p.amountDue = this.amountDue;
            p.principalPortion = this.principalPortion;
            p.interestPortion = this.interestPortion;
            p.amountPaid = this.amountPaid;
            p.lateFee = this.lateFee;
            p.dueDate = this.dueDate;
            p.paidDate = this.paidDate;
            p.status = this.status;
            return p;
        }
    }
}
