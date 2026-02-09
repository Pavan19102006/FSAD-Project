package com.loanmanagement.entity;

/**
 * Enumeration of loan application statuses
 */
public enum ApplicationStatus {
    PENDING, // Application submitted, awaiting review
    APPROVED, // Application approved by lender
    REJECTED, // Application rejected by lender
    WITHDRAWN // Application withdrawn by borrower
}
