package com.loanmanagement.entity;

/**
 * Enumeration of payment statuses
 */
public enum PaymentStatus {
    PENDING, // Payment is scheduled but not yet due
    PENDING_APPROVAL, // Borrower marked as paid, waiting for lender approval
    PAID, // Payment has been made successfully (alias for COMPLETED)
    COMPLETED, // Payment has been made successfully
    LATE, // Payment was made after the due date
    MISSED, // Payment was not made by due date
    OVERDUE, // Payment is past due date and not yet paid
    PARTIAL, // Partial payment made
    REJECTED // Lender rejected the payment claim
}
