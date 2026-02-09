package com.loanmanagement.entity;

/**
 * Enumeration of user roles in the loan management system
 */
public enum Role {
    ADMIN, // Platform administrator - oversee operations, manage users
    LENDER, // Create loan offers, track payments, manage borrowers
    BORROWER, // Apply for loans, track payments, manage loan details
    ANALYST // Financial analyst - analyze data, assess risks, generate reports
}
