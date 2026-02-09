package com.loanmanagement.entity;

/**
 * Type of EMI calculation method
 */
public enum EMIType {
    FLAT, // Flat Rate: EMI = (P + (P × R × T)) / N - Same EMI throughout
    REDUCING_BALANCE // Reducing Balance: EMI = [P × R × (1+R)^N] / [(1+R)^N - 1]
}
