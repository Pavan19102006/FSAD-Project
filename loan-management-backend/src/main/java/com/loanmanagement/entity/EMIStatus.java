package com.loanmanagement.entity;

/**
 * Status of an EMI payment in the schedule
 */
public enum EMIStatus {
    PENDING, // Not yet due
    DUE, // Payment is due
    PAID, // Fully paid
    PARTIAL, // Partially paid
    OVERDUE, // Past due date, not paid
    WAIVED // Penalty or amount waived
}
