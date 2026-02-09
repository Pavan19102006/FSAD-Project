package com.loanmanagement.controller;

import com.loanmanagement.dto.request.EMICalculationRequest;
import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.EMICalculationResponse;
import com.loanmanagement.dto.response.EMIScheduleResponse;
import com.loanmanagement.entity.EMISchedule;
import com.loanmanagement.service.EMIScheduleService;
import com.loanmanagement.service.InterestCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for EMI calculations and schedule management.
 * Public endpoint for EMI preview, secured endpoints for schedule management.
 */
@RestController
@RequestMapping("/api/emi")
public class EMIController {

    private final InterestCalculationService interestCalculationService;
    private final EMIScheduleService emiScheduleService;

    public EMIController(InterestCalculationService interestCalculationService,
            EMIScheduleService emiScheduleService) {
        this.interestCalculationService = interestCalculationService;
        this.emiScheduleService = emiScheduleService;
    }

    /**
     * Calculate EMI preview (public endpoint for borrowers to see before applying)
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<EMICalculationResponse>> calculateEMI(
            @RequestBody EMICalculationRequest request) {

        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal rate = request.getInterestRate();
        int termMonths = request.getTermMonths();
        boolean isReducingBalance = !"FLAT".equalsIgnoreCase(request.getEmiType());

        // Calculate EMI
        BigDecimal monthlyEMI;
        BigDecimal totalInterest;

        if (isReducingBalance) {
            monthlyEMI = interestCalculationService.calculateReducingBalanceEMI(principal, rate, termMonths);
            totalInterest = interestCalculationService.calculateTotalInterestReducingBalance(principal, rate,
                    termMonths);
        } else {
            monthlyEMI = interestCalculationService.calculateFlatRateEMI(principal, rate, termMonths);
            totalInterest = interestCalculationService.calculateSimpleInterest(principal, rate, termMonths);
        }

        BigDecimal totalPayable = principal.add(totalInterest);

        // Generate schedule breakdown
        List<EMICalculationResponse.EMIBreakdown> schedule = generateSchedulePreview(
                principal, rate, termMonths, monthlyEMI, isReducingBalance);

        // Build response
        EMICalculationResponse response = new EMICalculationResponse();
        response.setPrincipalAmount(principal);
        response.setInterestRate(rate);
        response.setTermMonths(termMonths);
        response.setEmiType(isReducingBalance ? "REDUCING_BALANCE" : "FLAT");
        response.setMonthlyEMI(monthlyEMI);
        response.setTotalInterest(totalInterest);
        response.setTotalPayable(totalPayable);
        response.setSchedule(schedule);

        return ResponseEntity.ok(ApiResponse.<EMICalculationResponse>builder()
                .success(true)
                .message("EMI calculated successfully")
                .data(response)
                .build());
    }

    /**
     * Get EMI schedule for a loan
     */
    @GetMapping("/schedule/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LENDER', 'BORROWER', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<EMIScheduleResponse>>> getEMISchedule(
            @PathVariable Long loanId) {

        List<EMISchedule> schedules = emiScheduleService.getEMISchedule(loanId);
        List<EMIScheduleResponse> response = schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<EMIScheduleResponse>>builder()
                .success(true)
                .message("EMI schedule retrieved")
                .data(response)
                .build());
    }

    /**
     * Get next pending EMI for a loan
     */
    @GetMapping("/next/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LENDER', 'BORROWER')")
    public ResponseEntity<ApiResponse<EMIScheduleResponse>> getNextPendingEMI(
            @PathVariable Long loanId) {

        EMISchedule emi = emiScheduleService.getNextPendingEMI(loanId);

        if (emi == null) {
            return ResponseEntity.ok(ApiResponse.<EMIScheduleResponse>builder()
                    .success(true)
                    .message("No pending EMIs found")
                    .data(null)
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.<EMIScheduleResponse>builder()
                .success(true)
                .message("Next EMI retrieved")
                .data(mapToResponse(emi))
                .build());
    }

    /**
     * Get all overdue EMIs (Admin/Analyst)
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<EMIScheduleResponse>>> getAllOverdueEMIs() {

        List<EMISchedule> overdueEMIs = emiScheduleService.getAllOverdueEMIs();
        List<EMIScheduleResponse> response = overdueEMIs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<EMIScheduleResponse>>builder()
                .success(true)
                .message("Overdue EMIs retrieved")
                .data(response)
                .build());
    }

    /**
     * Get EMIs due within next N days
     */
    @GetMapping("/due")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'LENDER')")
    public ResponseEntity<ApiResponse<List<EMIScheduleResponse>>> getEMIsDueSoon(
            @RequestParam(defaultValue = "7") int days) {

        List<EMISchedule> dueEMIs = emiScheduleService.getEMIsDueInDays(days);
        List<EMIScheduleResponse> response = dueEMIs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<EMIScheduleResponse>>builder()
                .success(true)
                .message("EMIs due in " + days + " days retrieved")
                .data(response)
                .build());
    }

    /**
     * Calculate late payment penalty
     */
    @GetMapping("/penalty")
    public ResponseEntity<ApiResponse<BigDecimal>> calculatePenalty(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "2.0") BigDecimal penaltyRate,
            @RequestParam int daysOverdue) {

        BigDecimal penalty = interestCalculationService.calculateLatePaymentPenalty(
                amount, penaltyRate, daysOverdue);

        return ResponseEntity.ok(ApiResponse.<BigDecimal>builder()
                .success(true)
                .message("Penalty calculated")
                .data(penalty)
                .build());
    }

    /**
     * Calculate prepayment savings
     */
    @GetMapping("/prepayment-savings")
    public ResponseEntity<ApiResponse<BigDecimal>> calculatePrepaymentSavings(
            @RequestParam BigDecimal remainingPrincipal,
            @RequestParam BigDecimal prepaymentAmount,
            @RequestParam BigDecimal interestRate,
            @RequestParam int remainingMonths) {

        BigDecimal savings = interestCalculationService.calculatePrepaymentSavings(
                remainingPrincipal, prepaymentAmount, interestRate, remainingMonths);

        return ResponseEntity.ok(ApiResponse.<BigDecimal>builder()
                .success(true)
                .message("Prepayment savings calculated")
                .data(savings)
                .build());
    }

    // Helper method to generate schedule preview
    private List<EMICalculationResponse.EMIBreakdown> generateSchedulePreview(
            BigDecimal principal, BigDecimal rate, int termMonths,
            BigDecimal monthlyEMI, boolean isReducingBalance) {

        List<EMICalculationResponse.EMIBreakdown> schedule = new ArrayList<>();
        BigDecimal outstanding = principal;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interest;
            BigDecimal principalPart;

            if (isReducingBalance) {
                BigDecimal[] breakdown = interestCalculationService.calculateEMIBreakdown(
                        outstanding, rate, monthlyEMI);
                interest = breakdown[0];
                principalPart = breakdown[1];
            } else {
                // Flat rate: equal distribution
                interest = interestCalculationService.calculateSimpleInterest(principal, rate, termMonths)
                        .divide(BigDecimal.valueOf(termMonths), 2, java.math.RoundingMode.HALF_UP);
                principalPart = principal.divide(BigDecimal.valueOf(termMonths), 2, java.math.RoundingMode.HALF_UP);
            }

            outstanding = outstanding.subtract(principalPart);
            if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
                outstanding = BigDecimal.ZERO;
            }

            schedule.add(new EMICalculationResponse.EMIBreakdown(
                    month, monthlyEMI, principalPart, interest, outstanding));
        }

        return schedule;
    }

    // Helper method to map entity to response
    private EMIScheduleResponse mapToResponse(EMISchedule emi) {
        EMIScheduleResponse response = new EMIScheduleResponse();
        response.setId(emi.getId());
        response.setLoanId(emi.getLoan().getId());
        response.setEmiNumber(emi.getEmiNumber());
        response.setDueDate(emi.getDueDate());
        response.setPrincipalComponent(emi.getPrincipalComponent());
        response.setInterestComponent(emi.getInterestComponent());
        response.setEmiAmount(emi.getEmiAmount());
        response.setOutstandingPrincipal(emi.getOutstandingPrincipal());
        response.setPenaltyAmount(emi.getPenaltyAmount());
        response.setAmountPaid(emi.getAmountPaid());
        response.setStatus(emi.getStatus().name());
        response.setPaidDate(emi.getPaidDate());
        response.setTotalDue(emi.getTotalDue());
        response.setRemainingAmount(emi.getRemainingAmount());
        response.setOverdue(emi.isOverdue());
        return response;
    }
}
