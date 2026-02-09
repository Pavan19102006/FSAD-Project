package com.loanmanagement.entity;

/**
 * Type of interest calculation method
 */
public enum InterestType {
    SIMPLE, // Simple Interest: SI = P × R × T / 100
    COMPOUND // Compound Interest: CI = P × (1 + R/n)^(n×T) - P
}
