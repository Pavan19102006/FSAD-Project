package com.loanmanagement.dto.request;

import java.math.BigDecimal;

/**
 * Request DTO for EMI calculation preview
 */
public class EMICalculationRequest {

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String emiType; // FLAT or REDUCING_BALANCE
    private String interestType; // SIMPLE or COMPOUND

    public EMICalculationRequest() {
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public String getInterestType() {
        return interestType;
    }

    public void setInterestType(String interestType) {
        this.interestType = interestType;
    }
}
