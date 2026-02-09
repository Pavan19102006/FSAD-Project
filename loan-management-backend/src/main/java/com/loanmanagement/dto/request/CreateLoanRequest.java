package com.loanmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateLoanRequest {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "100.00", message = "Minimum loan amount is 100")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    private BigDecimal interestRate;

    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    private Integer termMonths;

    private String description;
    
    private String interestType;
    
    private String emiType;
    
    private BigDecimal penaltyRate;

    public CreateLoanRequest() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getInterestType() {
        return interestType;
    }
    
    public void setInterestType(String interestType) {
        this.interestType = interestType;
    }
    
    public String getEmiType() {
        return emiType;
    }
    
    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }
    
    public BigDecimal getPenaltyRate() {
        return penaltyRate;
    }
    
    public void setPenaltyRate(BigDecimal penaltyRate) {
        this.penaltyRate = penaltyRate;
    }
}
