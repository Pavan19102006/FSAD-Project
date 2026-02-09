package com.loanmanagement.controller;

import com.loanmanagement.dto.request.CreateLoanRequest;
import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.LoanApplicationResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.dto.response.PaymentResponse;
import com.loanmanagement.entity.User;
import com.loanmanagement.service.LoanService;
import com.loanmanagement.service.PaymentService;
import com.loanmanagement.service.ReportService;
import com.loanmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lender")
@PreAuthorize("hasRole('LENDER')")
@Tag(name = "Lender", description = "Lender endpoints for loan management")
@SecurityRequirement(name = "bearerAuth")
public class LenderController {

    private final LoanService loanService;
    private final UserService userService;
    private final ReportService reportService;
    private final PaymentService paymentService;

    public LenderController(LoanService loanService, UserService userService, ReportService reportService, PaymentService paymentService) {
        this.loanService = loanService;
        this.userService = userService;
        this.reportService = reportService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get lender dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        User lender = userService.getCurrentUser();
        Map<String, Object> dashboard = reportService.getLenderDashboard(lender);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @PostMapping("/loans")
    @Operation(summary = "Create a new loan offer")
    public ResponseEntity<ApiResponse<LoanResponse>> createLoanOffer(@Valid @RequestBody CreateLoanRequest request) {
        User lender = userService.getCurrentUser();
        LoanResponse loan = loanService.createLoanOffer(lender, request);
        return ResponseEntity.ok(ApiResponse.success("Loan offer created successfully", loan));
    }

    @GetMapping("/loans")
    @Operation(summary = "Get all loans created by this lender")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans() {
        User lender = userService.getCurrentUser();
        List<LoanResponse> loans = loanService.getLoansByLender(lender);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/loans/{id}")
    @Operation(summary = "Get loan details")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable Long id) {
        LoanResponse loan = loanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/applications")
    @Operation(summary = "Get pending loan applications")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getPendingApplications() {
        List<LoanApplicationResponse> applications = loanService.getPendingApplications();
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PostMapping("/applications/{id}/approve")
    @Operation(summary = "Approve loan application")
    public ResponseEntity<ApiResponse<LoanResponse>> approveApplication(
            @PathVariable Long id) {
        User lender = userService.getCurrentUser();
        LoanResponse loan = loanService.approveApplication(id, lender);
        return ResponseEntity.ok(ApiResponse.success("Application approved and loan activated", loan));
    }

    @PostMapping("/applications/{id}/reject")
    @Operation(summary = "Reject loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> rejectApplication(
            @PathVariable Long id,
            @RequestParam String reason) {
        User lender = userService.getCurrentUser();
        LoanApplicationResponse application = loanService.rejectApplication(id, lender, reason);
        return ResponseEntity.ok(ApiResponse.success("Application rejected", application));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds to bank account")
    public ResponseEntity<ApiResponse<Map<String, Object>>> withdrawFunds(@RequestBody Map<String, Object> request) {
        userService.getCurrentUser(); // Verify user is authenticated
        Double amount = Double.valueOf(request.get("amount").toString());
        
        // In a real app, this would process the withdrawal to the lender's bank account
        // For now, we'll just return a success response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "PENDING");
        response.put("amount", amount);
        response.put("message", "Withdrawal request submitted successfully. Funds will be transferred within 2-3 business days.");
        response.put("transactionId", "WD" + System.currentTimeMillis());
        
        return ResponseEntity.ok(ApiResponse.success("Withdrawal initiated successfully", response));
    }

    @GetMapping("/payments/pending")
    @Operation(summary = "Get payments pending approval")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPendingPayments() {
        User lender = userService.getCurrentUser();
        List<PaymentResponse> payments = paymentService.getPendingApprovalPayments(lender);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/payments/{id}/approve")
    @Operation(summary = "Approve a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> approvePayment(@PathVariable Long id) {
        User lender = userService.getCurrentUser();
        PaymentResponse payment = paymentService.approvePayment(lender, id);
        return ResponseEntity.ok(ApiResponse.success("Payment approved successfully", payment));
    }

    @PostMapping("/payments/{id}/reject")
    @Operation(summary = "Reject a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> rejectPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        User lender = userService.getCurrentUser();
        PaymentResponse payment = paymentService.rejectPayment(lender, id, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment rejected", payment));
    }
}
