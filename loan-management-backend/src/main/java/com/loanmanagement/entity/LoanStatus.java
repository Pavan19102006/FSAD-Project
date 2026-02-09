package com.loanmanagement.entity;

/**
 * Enumeration of loan statuses
 */
public enum LoanStatus {
    PENDING, // Loan offer created, awaiting borrower
    ACTIVE, // Loan is active with ongoing payments
    COMPLETED, // All payments completed successfully
    DEFAULTED, // Borrower has defaulted on payments
    CANCELLED // Loan was cancelled before activation
}
