package com.loanmanagement.controller;

import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.dto.response.PaymentResponse;
import com.loanmanagement.service.LoanService;
import com.loanmanagement.service.PaymentService;
import com.loanmanagement.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyst")
@PreAuthorize("hasRole('ANALYST')")
@Tag(name = "Financial Analyst", description = "Analyst endpoints for reports and risk assessment")
@SecurityRequirement(name = "bearerAuth")
public class AnalystController {

    private final LoanService loanService;
    private final PaymentService paymentService;
    private final ReportService reportService;

    public AnalystController(LoanService loanService, PaymentService paymentService, ReportService reportService) {
        this.loanService = loanService;
        this.paymentService = paymentService;
        this.reportService = reportService;
    }

    @GetMapping("/reports/loans")
    @Operation(summary = "Get loan analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLoanAnalytics() {
        Map<String, Object> analytics = reportService.getLoanAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/reports/risk")
    @Operation(summary = "Get risk assessment report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRiskAssessment() {
        Map<String, Object> riskData = reportService.getRiskAssessment();
        return ResponseEntity.ok(ApiResponse.success(riskData));
    }

    @GetMapping("/reports/payments")
    @Operation(summary = "Get payment analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentAnalytics() {
        Map<String, Object> analytics = reportService.getPaymentAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/loans")
    @Operation(summary = "Get all loans for analysis")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getAllLoans() {
        List<LoanResponse> loans = loanService.getAllLoans();
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/loans/{id}")
    @Operation(summary = "Get loan details")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable Long id) {
        LoanResponse loan = loanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/loans/{id}/payments")
    @Operation(summary = "Get payment history for a loan")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getLoanPayments(@PathVariable Long id) {
        List<PaymentResponse> payments = paymentService.getPaymentSchedule(id);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/overdue-payments")
    @Operation(summary = "Get all overdue payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getOverduePayments() {
        List<PaymentResponse> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
