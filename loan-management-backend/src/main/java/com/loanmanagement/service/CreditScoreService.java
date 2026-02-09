package com.loanmanagement.service;

import com.loanmanagement.dto.request.CreditScoreRequest;
import com.loanmanagement.dto.response.CreditScoreResponse;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CreditScoreService {

    private final UserRepository userRepository;

    public CreditScoreService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CreditScoreResponse getCreditScore(User user) {
        return buildCreditScoreResponse(user);
    }

    @Transactional
    public CreditScoreResponse updateCreditScore(User user, CreditScoreRequest request) {
        if (request.getCreditScore() != null) {
            user.setCreditScore(request.getCreditScore());
        }
        if (request.getAnnualIncome() != null) {
            user.setAnnualIncome(request.getAnnualIncome());
        }
        if (request.getEmploymentStatus() != null) {
            user.setEmploymentStatus(request.getEmploymentStatus());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        // Calculate risk based on credit score
        calculateAndSetRisk(user);

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return buildCreditScoreResponse(user);
    }

    @Transactional
    public CreditScoreResponse calculateCreditScore(User user, CreditScoreRequest request) {
        int calculatedScore = calculateScore(request);
        user.setCreditScore(calculatedScore);

        if (request.getAnnualIncome() != null) {
            user.setAnnualIncome(request.getAnnualIncome());
        }
        if (request.getEmploymentStatus() != null) {
            user.setEmploymentStatus(request.getEmploymentStatus());
        }

        calculateAndSetRisk(user);

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return buildCreditScoreResponse(user);
    }

    private int calculateScore(CreditScoreRequest request) {
        // Base score starts at 550
        int baseScore = 550;

        // Payment history impact (35% weight) - max 150 points
        int paymentScore = 150;
        if (request.getLatePayments() != null) {
            paymentScore -= request.getLatePayments() * 15; // Each late payment reduces by 15
        }
        if (request.getHasDefaulted() != null && request.getHasDefaulted()) {
            paymentScore -= 100; // Default has major impact
        }
        paymentScore = Math.max(0, paymentScore);

        // Credit utilization/debt (30% weight) - max 120 points
        int debtScore = 120;
        if (request.getTotalDebt() != null && request.getAnnualIncome() != null 
                && request.getAnnualIncome().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal debtToIncome = request.getTotalDebt()
                    .divide(request.getAnnualIncome(), 2, RoundingMode.HALF_UP);
            if (debtToIncome.compareTo(new BigDecimal("0.5")) > 0) {
                debtScore -= 60;
            } else if (debtToIncome.compareTo(new BigDecimal("0.3")) > 0) {
                debtScore -= 30;
            }
        }

        // Length of credit history (15% weight) - max 60 points
        int historyScore = 0;
        if (request.getYearsOfCreditHistory() != null) {
            historyScore = Math.min(60, request.getYearsOfCreditHistory() * 8);
        }

        // Credit mix - existing loans (10% weight) - max 40 points
        int mixScore = 20;
        if (request.getExistingLoans() != null) {
            if (request.getExistingLoans() >= 1 && request.getExistingLoans() <= 3) {
                mixScore = 40; // Good credit mix
            } else if (request.getExistingLoans() > 5) {
                mixScore = 10; // Too many loans
            }
        }

        // Employment status bonus (10% weight) - max 30 points
        int employmentScore = 15;
        if (request.getEmploymentStatus() != null) {
            switch (request.getEmploymentStatus().toUpperCase()) {
                case "EMPLOYED":
                case "FULL_TIME":
                    employmentScore = 30;
                    break;
                case "SELF_EMPLOYED":
                case "BUSINESS_OWNER":
                    employmentScore = 25;
                    break;
                case "PART_TIME":
                case "CONTRACT":
                    employmentScore = 20;
                    break;
                case "RETIRED":
                    employmentScore = 25;
                    break;
                case "UNEMPLOYED":
                    employmentScore = 5;
                    break;
            }
        }

        int totalScore = baseScore + paymentScore + debtScore + historyScore + mixScore + employmentScore;

        // Clamp between 300 and 850
        return Math.min(850, Math.max(300, totalScore));
    }

    private void calculateAndSetRisk(User user) {
        if (user.getCreditScore() == null) {
            user.setRiskLevel("UNKNOWN");
            user.setRiskScore(BigDecimal.ZERO);
            return;
        }

        int score = user.getCreditScore();
        BigDecimal riskScore;
        String riskLevel;

        if (score >= 750) {
            riskLevel = "LOW";
            riskScore = new BigDecimal("10.00");
        } else if (score >= 700) {
            riskLevel = "LOW";
            riskScore = new BigDecimal("20.00");
        } else if (score >= 650) {
            riskLevel = "MEDIUM";
            riskScore = new BigDecimal("40.00");
        } else if (score >= 600) {
            riskLevel = "MEDIUM";
            riskScore = new BigDecimal("55.00");
        } else if (score >= 550) {
            riskLevel = "HIGH";
            riskScore = new BigDecimal("70.00");
        } else {
            riskLevel = "VERY_HIGH";
            riskScore = new BigDecimal("90.00");
        }

        user.setRiskLevel(riskLevel);
        user.setRiskScore(riskScore);
    }

    private CreditScoreResponse buildCreditScoreResponse(User user) {
        Integer creditScore = user.getCreditScore();
        String creditRating = getCreditRating(creditScore);
        String recommendations = getRecommendations(creditScore);
        BigDecimal maxLoanEligibility = calculateMaxLoanEligibility(user);
        BigDecimal suggestedInterestRate = calculateSuggestedInterestRate(creditScore);

        return CreditScoreResponse.builder()
                .userId(user.getId())
                .creditScore(creditScore)
                .creditRating(creditRating)
                .riskLevel(user.getRiskLevel())
                .riskScore(user.getRiskScore())
                .annualIncome(user.getAnnualIncome())
                .employmentStatus(user.getEmploymentStatus())
                .recommendations(recommendations)
                .maxLoanEligibility(maxLoanEligibility)
                .suggestedInterestRate(suggestedInterestRate)
                .lastUpdated(user.getUpdatedAt())
                .build();
    }

    private String getCreditRating(Integer score) {
        if (score == null) return "Not Available";
        if (score >= 800) return "Exceptional";
        if (score >= 740) return "Very Good";
        if (score >= 670) return "Good";
        if (score >= 580) return "Fair";
        return "Poor";
    }

    private String getRecommendations(Integer score) {
        if (score == null) {
            return "Please update your credit score to get personalized recommendations.";
        }
        if (score >= 750) {
            return "Excellent credit! You qualify for the best loan rates. Consider taking advantage of premium loan offers.";
        }
        if (score >= 700) {
            return "Good credit score! You're eligible for competitive rates. Maintain your payment history to improve further.";
        }
        if (score >= 650) {
            return "Decent credit. Pay bills on time and reduce debt to improve your score. You may qualify for moderate interest rates.";
        }
        if (score >= 600) {
            return "Fair credit. Focus on paying down existing debt and avoid late payments. Consider a secured credit card to build history.";
        }
        return "Your credit needs improvement. Pay all bills on time, reduce debt, and avoid new credit applications. Consider credit counseling.";
    }

    private BigDecimal calculateMaxLoanEligibility(User user) {
        if (user.getAnnualIncome() == null || user.getCreditScore() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal annualIncome = user.getAnnualIncome();
        int score = user.getCreditScore();

        // Base multiplier based on credit score
        BigDecimal multiplier;
        if (score >= 750) {
            multiplier = new BigDecimal("5.0");
        } else if (score >= 700) {
            multiplier = new BigDecimal("4.0");
        } else if (score >= 650) {
            multiplier = new BigDecimal("3.0");
        } else if (score >= 600) {
            multiplier = new BigDecimal("2.0");
        } else {
            multiplier = new BigDecimal("1.0");
        }

        return annualIncome.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSuggestedInterestRate(Integer score) {
        if (score == null) {
            return new BigDecimal("15.00");
        }
        if (score >= 750) {
            return new BigDecimal("8.50");
        }
        if (score >= 700) {
            return new BigDecimal("10.00");
        }
        if (score >= 650) {
            return new BigDecimal("12.50");
        }
        if (score >= 600) {
            return new BigDecimal("15.00");
        }
        return new BigDecimal("18.00");
    }
}
