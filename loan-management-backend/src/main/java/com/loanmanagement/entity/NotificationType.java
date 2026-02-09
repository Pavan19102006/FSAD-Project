package com.loanmanagement.entity;

/**
 * Types of notifications
 */
public enum NotificationType {
    EMI_REMINDER, // EMI due reminder (3 days before)
    EMI_DUE_TODAY, // EMI due today
    PAYMENT_OVERDUE, // Payment is overdue
    PAYMENT_RECEIVED, // Payment confirmation
    LOAN_APPROVED, // Loan approved
    LOAN_REJECTED, // Loan rejected
    APPLICATION_RECEIVED, // Application received
    LOAN_DISBURSED, // Loan disbursed
    LOAN_COMPLETED, // Loan fully paid
    PENALTY_APPLIED, // Late penalty applied
    PREPAYMENT_RECEIVED, // Prepayment recorded
    SYSTEM_ALERT, // General system alert
    ACCOUNT_UPDATE // Account information updated
}
