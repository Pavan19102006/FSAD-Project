package com.loanmanagement.controller;

import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.entity.User;
import com.loanmanagement.service.RiskScoringService;
import com.loanmanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for risk assessment
 */
@RestController
@RequestMapping("/api/risk")
@PreAuthorize("hasAnyRole('ADMIN', 'LENDER', 'ANALYST')")
public class RiskController {

    private final RiskScoringService riskScoringService;
    private final UserService userService;

    public RiskController(RiskScoringService riskScoringService, UserService userService) {
        this.riskScoringService = riskScoringService;
        this.userService = userService;
    }

    /**
     * Get risk assessment for a borrower
     */
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<ApiResponse<RiskScoringService.RiskAssessment>> getBorrowerRisk(
            @PathVariable Long borrowerId) {

        User borrower = userService.findById(borrowerId);
        RiskScoringService.RiskAssessment assessment = riskScoringService.calculateRiskScore(borrower);

        return ResponseEntity.ok(ApiResponse.<RiskScoringService.RiskAssessment>builder()
                .success(true)
                .message("Risk assessment calculated")
                .data(assessment)
                .build());
    }

    /**
     * Calculate/recalculate risk score for a borrower
     */
    @PostMapping("/calculate/{borrowerId}")
    public ResponseEntity<ApiResponse<RiskScoringService.RiskAssessment>> calculateRisk(
            @PathVariable Long borrowerId) {

        User borrower = userService.findById(borrowerId);
        RiskScoringService.RiskAssessment assessment = riskScoringService.calculateRiskScore(borrower);

        return ResponseEntity.ok(ApiResponse.<RiskScoringService.RiskAssessment>builder()
                .success(true)
                .message("Risk score recalculated")
                .data(assessment)
                .build());
    }
}
