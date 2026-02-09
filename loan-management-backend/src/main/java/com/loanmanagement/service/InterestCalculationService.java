package com.loanmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Service for all interest and EMI calculations.
 * Supports Simple Interest, Compound Interest, and EMI calculations
 * for both Flat and Reducing Balance methods.
 */
@Service
public class InterestCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(InterestCalculationService.class);
    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    private static final int SCALE = 2;

    // Default penalty rate for late payments (2%)
    private static final BigDecimal DEFAULT_PENALTY_RATE = new BigDecimal("2.00");

    /**
     * Calculate Simple Interest
     * Formula: SI = (P × R × T) / 100
     * 
     * @param principal    Principal amount
     * @param annualRate   Annual interest rate (percentage)
     * @param tenureMonths Loan tenure in months
     * @return Simple interest amount
     */
    public BigDecimal calculateSimpleInterest(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        logger.debug("Calculating Simple Interest: Principal={}, Rate={}%, Tenure={} months",
                principal, annualRate, tenureMonths);

        BigDecimal tenureYears = BigDecimal.valueOf(tenureMonths).divide(BigDecimal.valueOf(12), MATH_CONTEXT);
        BigDecimal interest = principal
                .multiply(annualRate)
                .multiply(tenureYears)
                .divide(BigDecimal.valueOf(100), MATH_CONTEXT);

        return interest.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Compound Interest
     * Formula: CI = P × (1 + R/n)^(n×T) - P
     * Where n = compounding frequency (12 for monthly)
     * 
     * @param principal            Principal amount
     * @param annualRate           Annual interest rate (percentage)
     * @param tenureMonths         Loan tenure in months
     * @param compoundingFrequency Number of times interest compounds per year
     *                             (default: 12)
     * @return Compound interest amount
     */
    public BigDecimal calculateCompoundInterest(BigDecimal principal, BigDecimal annualRate,
            int tenureMonths, int compoundingFrequency) {
        logger.debug("Calculating Compound Interest: Principal={}, Rate={}%, Tenure={} months, Frequency={}",
                principal, annualRate, tenureMonths, compoundingFrequency);

        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), MATH_CONTEXT);
        BigDecimal n = BigDecimal.valueOf(compoundingFrequency);
        BigDecimal t = BigDecimal.valueOf(tenureMonths).divide(BigDecimal.valueOf(12), MATH_CONTEXT);

        // (1 + R/n)
        BigDecimal base = BigDecimal.ONE.add(rate.divide(n, MATH_CONTEXT));

        // (n × T)
        BigDecimal exponent = n.multiply(t);

        // (1 + R/n)^(n×T)
        double baseDouble = base.doubleValue();
        double expDouble = exponent.doubleValue();
        BigDecimal compoundFactor = BigDecimal.valueOf(Math.pow(baseDouble, expDouble));

        // CI = P × compoundFactor - P
        BigDecimal totalAmount = principal.multiply(compoundFactor);
        BigDecimal interest = totalAmount.subtract(principal);

        return interest.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Compound Interest with monthly compounding (default)
     */
    public BigDecimal calculateCompoundInterest(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        return calculateCompoundInterest(principal, annualRate, tenureMonths, 12);
    }

    /**
     * Calculate EMI using Reducing Balance Method
     * Formula: EMI = [P × R × (1+R)^N] / [(1+R)^N - 1]
     * Where R = monthly interest rate, N = number of months
     * 
     * @param principal    Principal amount
     * @param annualRate   Annual interest rate (percentage)
     * @param tenureMonths Loan tenure in months
     * @return Monthly EMI amount
     */
    public BigDecimal calculateReducingBalanceEMI(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        logger.debug("Calculating Reducing Balance EMI: Principal={}, Rate={}%, Tenure={} months",
                principal, annualRate, tenureMonths);

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            // If no interest, simple division
            return principal.divide(BigDecimal.valueOf(tenureMonths), MATH_CONTEXT)
                    .setScale(SCALE, RoundingMode.HALF_UP);
        }

        // Monthly interest rate
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), MATH_CONTEXT)
                .divide(BigDecimal.valueOf(12), MATH_CONTEXT);

        // (1 + R)^N
        double monthlyRateDouble = monthlyRate.doubleValue();
        double compoundFactor = Math.pow(1 + monthlyRateDouble, tenureMonths);
        BigDecimal compoundFactorBD = BigDecimal.valueOf(compoundFactor);

        // P × R × (1+R)^N
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(compoundFactorBD);

        // (1+R)^N - 1
        BigDecimal denominator = compoundFactorBD.subtract(BigDecimal.ONE);

        // EMI = numerator / denominator
        BigDecimal emi = numerator.divide(denominator, MATH_CONTEXT);

        return emi.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate EMI using Flat Rate Method
     * Formula: EMI = (P + (P × R × T)) / N
     * Where T = tenure in years, N = number of months
     * 
     * @param principal    Principal amount
     * @param annualRate   Annual interest rate (percentage)
     * @param tenureMonths Loan tenure in months
     * @return Monthly EMI amount
     */
    public BigDecimal calculateFlatRateEMI(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        logger.debug("Calculating Flat Rate EMI: Principal={}, Rate={}%, Tenure={} months",
                principal, annualRate, tenureMonths);

        // Total interest using simple interest formula
        BigDecimal totalInterest = calculateSimpleInterest(principal, annualRate, tenureMonths);

        // Total amount = Principal + Interest
        BigDecimal totalAmount = principal.add(totalInterest);

        // EMI = Total Amount / Number of months
        BigDecimal emi = totalAmount.divide(BigDecimal.valueOf(tenureMonths), MATH_CONTEXT);

        return emi.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total interest for Reducing Balance EMI
     * Total Interest = (EMI × N) - P
     * 
     * @param principal    Principal amount
     * @param annualRate   Annual interest rate (percentage)
     * @param tenureMonths Loan tenure in months
     * @return Total interest amount
     */
    public BigDecimal calculateTotalInterestReducingBalance(BigDecimal principal, BigDecimal annualRate,
            int tenureMonths) {
        BigDecimal emi = calculateReducingBalanceEMI(principal, annualRate, tenureMonths);
        BigDecimal totalPayment = emi.multiply(BigDecimal.valueOf(tenureMonths));
        return totalPayment.subtract(principal).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate late payment penalty
     * Formula: Penalty = (Overdue Amount × Penalty Rate × Days Overdue) / (365 ×
     * 100)
     * 
     * @param overdueAmount Amount that is overdue
     * @param penaltyRate   Annual penalty rate (percentage), uses default if null
     * @param daysOverdue   Number of days payment is overdue
     * @return Penalty amount
     */
    public BigDecimal calculateLatePaymentPenalty(BigDecimal overdueAmount, BigDecimal penaltyRate,
            int daysOverdue) {
        logger.debug("Calculating Late Payment Penalty: Amount={}, Rate={}%, Days={}",
                overdueAmount, penaltyRate, daysOverdue);

        BigDecimal rate = penaltyRate != null ? penaltyRate : DEFAULT_PENALTY_RATE;

        BigDecimal penalty = overdueAmount
                .multiply(rate)
                .multiply(BigDecimal.valueOf(daysOverdue))
                .divide(BigDecimal.valueOf(365 * 100), MATH_CONTEXT);

        return penalty.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate prepayment savings (interest saved by early payment)
     * 
     * @param remainingPrincipal Remaining principal before prepayment
     * @param prepaymentAmount   Amount being prepaid
     * @param annualRate         Annual interest rate
     * @param remainingMonths    Remaining months in loan tenure
     * @return Interest amount saved due to prepayment
     */
    public BigDecimal calculatePrepaymentSavings(BigDecimal remainingPrincipal, BigDecimal prepaymentAmount,
            BigDecimal annualRate, int remainingMonths) {
        logger.debug("Calculating Prepayment Savings: Principal={}, Prepayment={}, Rate={}%, Months={}",
                remainingPrincipal, prepaymentAmount, annualRate, remainingMonths);

        // Calculate interest on the prepayment amount for remaining tenure
        BigDecimal interestSaved = calculateSimpleInterest(prepaymentAmount, annualRate, remainingMonths);

        return interestSaved.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate principal and interest components for a specific EMI payment
     * in Reducing Balance method
     * 
     * @param outstandingPrincipal Principal remaining before this payment
     * @param annualRate           Annual interest rate
     * @param emiAmount            Total EMI amount
     * @return Array: [interestComponent, principalComponent]
     */
    public BigDecimal[] calculateEMIBreakdown(BigDecimal outstandingPrincipal, BigDecimal annualRate,
            BigDecimal emiAmount) {
        // Monthly interest rate
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), MATH_CONTEXT)
                .divide(BigDecimal.valueOf(12), MATH_CONTEXT);

        // Interest component = Outstanding Principal × Monthly Rate
        BigDecimal interestComponent = outstandingPrincipal.multiply(monthlyRate)
                .setScale(SCALE, RoundingMode.HALF_UP);

        // Principal component = EMI - Interest
        BigDecimal principalComponent = emiAmount.subtract(interestComponent)
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new BigDecimal[] { interestComponent, principalComponent };
    }

    /**
     * Calculate total payable amount including interest
     * 
     * @param principal         Principal amount
     * @param annualRate        Annual interest rate
     * @param tenureMonths      Loan tenure in months
     * @param isReducingBalance true for reducing balance, false for flat rate
     * @return Total amount to be paid
     */
    public BigDecimal calculateTotalPayable(BigDecimal principal, BigDecimal annualRate,
            int tenureMonths, boolean isReducingBalance) {
        BigDecimal emi;
        if (isReducingBalance) {
            emi = calculateReducingBalanceEMI(principal, annualRate, tenureMonths);
        } else {
            emi = calculateFlatRateEMI(principal, annualRate, tenureMonths);
        }

        return emi.multiply(BigDecimal.valueOf(tenureMonths)).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
