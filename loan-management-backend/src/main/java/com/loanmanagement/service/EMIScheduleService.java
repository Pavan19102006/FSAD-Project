package com.loanmanagement.service;

import com.loanmanagement.entity.*;
import com.loanmanagement.repository.EMIScheduleRepository;
import com.loanmanagement.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating and managing EMI schedules.
 */
@Service
@SuppressWarnings("null")
public class EMIScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(EMIScheduleService.class);

    private final EMIScheduleRepository emiScheduleRepository;
    private final LoanRepository loanRepository;
    private final InterestCalculationService interestCalculationService;

    public EMIScheduleService(EMIScheduleRepository emiScheduleRepository,
            LoanRepository loanRepository,
            InterestCalculationService interestCalculationService) {
        this.emiScheduleRepository = emiScheduleRepository;
        this.loanRepository = loanRepository;
        this.interestCalculationService = interestCalculationService;
    }

    /**
     * Generate EMI schedule for a loan when it's approved.
     * Creates all EMI installments with principal/interest breakdown.
     * 
     * @param loan The approved loan
     * @return List of generated EMI schedules
     */
    @Transactional
    public List<EMISchedule> generateEMISchedule(Loan loan) {
        logger.info("Generating EMI schedule for loan ID: {}", loan.getId());

        List<EMISchedule> schedules = new ArrayList<>();

        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getInterestRate();
        int termMonths = loan.getTermMonths();
        EMIType emiType = loan.getEmiType() != null ? loan.getEmiType() : EMIType.REDUCING_BALANCE;

        // Calculate EMI amount based on type
        BigDecimal emiAmount;
        if (emiType == EMIType.REDUCING_BALANCE) {
            emiAmount = interestCalculationService.calculateReducingBalanceEMI(principal, annualRate, termMonths);
        } else {
            emiAmount = interestCalculationService.calculateFlatRateEMI(principal, annualRate, termMonths);
        }

        // Update loan with calculated EMI
        loan.setMonthlyPayment(emiAmount);

        // Calculate total interest
        BigDecimal totalInterest;
        if (emiType == EMIType.REDUCING_BALANCE) {
            totalInterest = interestCalculationService.calculateTotalInterestReducingBalance(principal, annualRate,
                    termMonths);
        } else {
            totalInterest = interestCalculationService.calculateSimpleInterest(principal, annualRate, termMonths);
        }
        loan.setTotalInterest(totalInterest);

        // Start date is today or loan's start date
        LocalDate startDate = loan.getStartDate() != null ? loan.getStartDate() : LocalDate.now();
        loan.setStartDate(startDate);

        // Calculate end date
        LocalDate endDate = startDate.plusMonths(termMonths);
        loan.setEndDate(endDate);

        // Generate schedule based on EMI type
        BigDecimal outstandingPrincipal = principal;

        for (int i = 1; i <= termMonths; i++) {
            EMISchedule schedule = new EMISchedule();
            schedule.setLoan(loan);
            schedule.setEmiNumber(i);
            schedule.setDueDate(startDate.plusMonths(i));
            schedule.setEmiAmount(emiAmount);
            schedule.setStatus(EMIStatus.PENDING);

            if (emiType == EMIType.REDUCING_BALANCE) {
                // Reducing balance: Interest decreases, principal increases each month
                BigDecimal[] breakdown = interestCalculationService.calculateEMIBreakdown(
                        outstandingPrincipal, annualRate, emiAmount);

                schedule.setInterestComponent(breakdown[0]);
                schedule.setPrincipalComponent(breakdown[1]);

                outstandingPrincipal = outstandingPrincipal.subtract(breakdown[1]);

                // Handle rounding in last EMI
                if (i == termMonths && outstandingPrincipal.compareTo(BigDecimal.ZERO) > 0) {
                    schedule.setPrincipalComponent(schedule.getPrincipalComponent().add(outstandingPrincipal));
                    outstandingPrincipal = BigDecimal.ZERO;
                }
            } else {
                // Flat rate: Fixed principal and interest components
                BigDecimal monthlyInterest = totalInterest.divide(BigDecimal.valueOf(termMonths), 2,
                        RoundingMode.HALF_UP);
                BigDecimal monthlyPrincipal = principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

                schedule.setInterestComponent(monthlyInterest);
                schedule.setPrincipalComponent(monthlyPrincipal);

                outstandingPrincipal = outstandingPrincipal.subtract(monthlyPrincipal);
            }

            schedule.setOutstandingPrincipal(outstandingPrincipal.max(BigDecimal.ZERO));
            schedules.add(schedule);
        }

        // Save all schedules
        emiScheduleRepository.saveAll(schedules);
        loanRepository.save(loan);

        logger.info("Generated {} EMI schedules for loan ID: {}", schedules.size(), loan.getId());
        return schedules;
    }

    /**
     * Get EMI schedule for a loan
     */
    public List<EMISchedule> getEMISchedule(Long loanId) {
        return emiScheduleRepository.findByLoanIdOrderByEmiNumber(loanId);
    }

    /**
     * Get next pending EMI for a loan
     */
    public EMISchedule getNextPendingEMI(Long loanId) {
        return emiScheduleRepository.findNextPendingEMI(loanId);
    }

    /**
     * Get all overdue EMIs across all loans
     */
    public List<EMISchedule> getAllOverdueEMIs() {
        return emiScheduleRepository.findAllOverdueEMIs(LocalDate.now());
    }

    /**
     * Get overdue EMIs for a specific loan
     */
    public List<EMISchedule> getOverdueEMIsForLoan(Long loanId) {
        return emiScheduleRepository.findOverdueEMIsByLoan(loanId, LocalDate.now());
    }

    /**
     * Get EMIs due within the next N days
     */
    public List<EMISchedule> getEMIsDueInDays(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return emiScheduleRepository.findEMIsDueInRange(startDate, endDate);
    }

    /**
     * Get upcoming EMIs for a borrower
     */
    public List<EMISchedule> getUpcomingEMIsForBorrower(Long borrowerId, int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return emiScheduleRepository.findUpcomingEMIsForBorrower(borrowerId, startDate, endDate);
    }

    /**
     * Mark EMI as overdue and apply penalty
     */
    @Transactional
    public EMISchedule markAsOverdueWithPenalty(Long emiId) {
        EMISchedule emi = emiScheduleRepository.findById(emiId)
                .orElseThrow(() -> new RuntimeException("EMI not found: " + emiId));

        if (emi.getStatus() == EMIStatus.PAID || emi.getStatus() == EMIStatus.WAIVED) {
            return emi; // Already handled
        }

        Loan loan = emi.getLoan();
        int daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(emi.getDueDate(), LocalDate.now());

        if (daysOverdue > 0) {
            BigDecimal penalty = interestCalculationService.calculateLatePaymentPenalty(
                    emi.getEmiAmount(), loan.getPenaltyRate(), daysOverdue);

            emi.setPenaltyAmount(penalty);
            emi.setStatus(EMIStatus.OVERDUE);

            // Update loan's total penalty
            BigDecimal totalPenalty = loan.getTotalPenaltyAccrued() != null ? loan.getTotalPenaltyAccrued()
                    : BigDecimal.ZERO;
            loan.setTotalPenaltyAccrued(totalPenalty.add(penalty));

            loanRepository.save(loan);
            logger.info("Applied penalty {} to EMI {} (days overdue: {})", penalty, emiId, daysOverdue);
        }

        return emiScheduleRepository.save(emi);
    }

    /**
     * Record payment for an EMI
     */
    @Transactional
    public EMISchedule recordPayment(Long emiId, BigDecimal amount) {
        EMISchedule emi = emiScheduleRepository.findById(emiId)
                .orElseThrow(() -> new RuntimeException("EMI not found: " + emiId));

        BigDecimal currentPaid = emi.getAmountPaid() != null ? emi.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(amount);
        emi.setAmountPaid(newPaid);

        BigDecimal totalDue = emi.getTotalDue();

        if (newPaid.compareTo(totalDue) >= 0) {
            emi.setStatus(EMIStatus.PAID);
            emi.setPaidDate(LocalDate.now());

            // Update loan remaining balance
            Loan loan = emi.getLoan();
            BigDecimal remaining = loan.getRemainingBalance().subtract(emi.getPrincipalComponent());
            loan.setRemainingBalance(remaining.max(BigDecimal.ZERO));
            loanRepository.save(loan);

            logger.info("EMI {} fully paid", emiId);
        } else {
            emi.setStatus(EMIStatus.PARTIAL);
            logger.info("Partial payment recorded for EMI {}: {} of {}", emiId, newPaid, totalDue);
        }

        return emiScheduleRepository.save(emi);
    }

    /**
     * Get total outstanding amount for a loan
     */
    public BigDecimal getTotalOutstanding(Long loanId) {
        BigDecimal outstanding = emiScheduleRepository.getTotalOutstandingByLoan(loanId);
        return outstanding != null ? outstanding : BigDecimal.ZERO;
    }

    /**
     * Get total penalties for a loan
     */
    public BigDecimal getTotalPenalties(Long loanId) {
        BigDecimal penalties = emiScheduleRepository.getTotalPenaltyByLoan(loanId);
        return penalties != null ? penalties : BigDecimal.ZERO;
    }
}
