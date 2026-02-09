package com.loanmanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public class CreditScoreRequest {

    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    private Integer creditScore;

    private BigDecimal annualIncome;

    private String employmentStatus;

    private String address;

    // For credit score calculation
    private Integer existingLoans;
    private Integer latePayments;
    private BigDecimal totalDebt;
    private Integer yearsOfCreditHistory;
    private Boolean hasDefaulted;

    public CreditScoreRequest() {
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getExistingLoans() {
        return existingLoans;
    }

    public void setExistingLoans(Integer existingLoans) {
        this.existingLoans = existingLoans;
    }

    public Integer getLatePayments() {
        return latePayments;
    }

    public void setLatePayments(Integer latePayments) {
        this.latePayments = latePayments;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(BigDecimal totalDebt) {
        this.totalDebt = totalDebt;
    }

    public Integer getYearsOfCreditHistory() {
        return yearsOfCreditHistory;
    }

    public void setYearsOfCreditHistory(Integer yearsOfCreditHistory) {
        this.yearsOfCreditHistory = yearsOfCreditHistory;
    }

    public Boolean getHasDefaulted() {
        return hasDefaulted;
    }

    public void setHasDefaulted(Boolean hasDefaulted) {
        this.hasDefaulted = hasDefaulted;
    }
}
