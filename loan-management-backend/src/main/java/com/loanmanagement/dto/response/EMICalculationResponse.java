package com.loanmanagement.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for EMI calculation preview
 */
public class EMICalculationResponse {

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String emiType;
    private BigDecimal monthlyEMI;
    private BigDecimal totalInterest;
    private BigDecimal totalPayable;
    private List<EMIBreakdown> schedule;

    public EMICalculationResponse() {
    }

    // Getters and Setters
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

    public BigDecimal getMonthlyEMI() {
        return monthlyEMI;
    }

    public void setMonthlyEMI(BigDecimal monthlyEMI) {
        this.monthlyEMI = monthlyEMI;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(BigDecimal totalPayable) {
        this.totalPayable = totalPayable;
    }

    public List<EMIBreakdown> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<EMIBreakdown> schedule) {
        this.schedule = schedule;
    }

    /**
     * Inner class for EMI breakdown in preview
     */
    public static class EMIBreakdown {
        private int month;
        private BigDecimal emiAmount;
        private BigDecimal principalComponent;
        private BigDecimal interestComponent;
        private BigDecimal outstandingBalance;

        public EMIBreakdown() {
        }

        public EMIBreakdown(int month, BigDecimal emiAmount, BigDecimal principalComponent,
                BigDecimal interestComponent, BigDecimal outstandingBalance) {
            this.month = month;
            this.emiAmount = emiAmount;
            this.principalComponent = principalComponent;
            this.interestComponent = interestComponent;
            this.outstandingBalance = outstandingBalance;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public BigDecimal getEmiAmount() {
            return emiAmount;
        }

        public void setEmiAmount(BigDecimal emiAmount) {
            this.emiAmount = emiAmount;
        }

        public BigDecimal getPrincipalComponent() {
            return principalComponent;
        }

        public void setPrincipalComponent(BigDecimal principalComponent) {
            this.principalComponent = principalComponent;
        }

        public BigDecimal getInterestComponent() {
            return interestComponent;
        }

        public void setInterestComponent(BigDecimal interestComponent) {
            this.interestComponent = interestComponent;
        }

        public BigDecimal getOutstandingBalance() {
            return outstandingBalance;
        }

        public void setOutstandingBalance(BigDecimal outstandingBalance) {
            this.outstandingBalance = outstandingBalance;
        }
    }
}
