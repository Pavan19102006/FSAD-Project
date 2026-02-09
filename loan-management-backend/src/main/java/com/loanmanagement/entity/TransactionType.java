package com.loanmanagement.entity;

/**
 * Enumeration of transaction types for audit trail
 */
public enum TransactionType {
    DISBURSEMENT, // Loan amount disbursed to borrower
    PAYMENT, // Regular payment from borrower
    FEE, // Service fee or processing fee
    PENALTY, // Late payment penalty
    REFUND, // Refund for overpayment
    ADJUSTMENT // Manual adjustment
}
