package com.loanmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditScoreResponse {
    private Long userId;
    private Integer creditScore;
    private String creditRating;
    private String riskLevel;
    private BigDecimal riskScore;
    private BigDecimal annualIncome;
    private String employmentStatus;
    private String recommendations;
    private BigDecimal maxLoanEligibility;
    private BigDecimal suggestedInterestRate;
    private LocalDateTime lastUpdated;

    public CreditScoreResponse() {
    }

    public static CreditScoreResponseBuilder builder() {
        return new CreditScoreResponseBuilder();
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getCreditRating() {
        return creditRating;
    }

    public void setCreditRating(String creditRating) {
        this.creditRating = creditRating;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
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

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public BigDecimal getMaxLoanEligibility() {
        return maxLoanEligibility;
    }

    public void setMaxLoanEligibility(BigDecimal maxLoanEligibility) {
        this.maxLoanEligibility = maxLoanEligibility;
    }

    public BigDecimal getSuggestedInterestRate() {
        return suggestedInterestRate;
    }

    public void setSuggestedInterestRate(BigDecimal suggestedInterestRate) {
        this.suggestedInterestRate = suggestedInterestRate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static class CreditScoreResponseBuilder {
        private Long userId;
        private Integer creditScore;
        private String creditRating;
        private String riskLevel;
        private BigDecimal riskScore;
        private BigDecimal annualIncome;
        private String employmentStatus;
        private String recommendations;
        private BigDecimal maxLoanEligibility;
        private BigDecimal suggestedInterestRate;
        private LocalDateTime lastUpdated;

        public CreditScoreResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public CreditScoreResponseBuilder creditScore(Integer creditScore) {
            this.creditScore = creditScore;
            return this;
        }

        public CreditScoreResponseBuilder creditRating(String creditRating) {
            this.creditRating = creditRating;
            return this;
        }

        public CreditScoreResponseBuilder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public CreditScoreResponseBuilder riskScore(BigDecimal riskScore) {
            this.riskScore = riskScore;
            return this;
        }

        public CreditScoreResponseBuilder annualIncome(BigDecimal annualIncome) {
            this.annualIncome = annualIncome;
            return this;
        }

        public CreditScoreResponseBuilder employmentStatus(String employmentStatus) {
            this.employmentStatus = employmentStatus;
            return this;
        }

        public CreditScoreResponseBuilder recommendations(String recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public CreditScoreResponseBuilder maxLoanEligibility(BigDecimal maxLoanEligibility) {
            this.maxLoanEligibility = maxLoanEligibility;
            return this;
        }

        public CreditScoreResponseBuilder suggestedInterestRate(BigDecimal suggestedInterestRate) {
            this.suggestedInterestRate = suggestedInterestRate;
            return this;
        }

        public CreditScoreResponseBuilder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public CreditScoreResponse build() {
            CreditScoreResponse response = new CreditScoreResponse();
            response.setUserId(this.userId);
            response.setCreditScore(this.creditScore);
            response.setCreditRating(this.creditRating);
            response.setRiskLevel(this.riskLevel);
            response.setRiskScore(this.riskScore);
            response.setAnnualIncome(this.annualIncome);
            response.setEmploymentStatus(this.employmentStatus);
            response.setRecommendations(this.recommendations);
            response.setMaxLoanEligibility(this.maxLoanEligibility);
            response.setSuggestedInterestRate(this.suggestedInterestRate);
            response.setLastUpdated(this.lastUpdated);
            return response;
        }
    }
}
