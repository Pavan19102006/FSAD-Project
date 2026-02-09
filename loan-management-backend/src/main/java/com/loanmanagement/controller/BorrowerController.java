package com.loanmanagement.controller;

import com.loanmanagement.dto.request.CreditScoreRequest;
import com.loanmanagement.dto.request.LoanApplicationRequest;
import com.loanmanagement.dto.request.PaymentRequest;
import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.CreditScoreResponse;
import com.loanmanagement.dto.response.LoanApplicationResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.dto.response.PaymentResponse;
import com.loanmanagement.entity.User;
import com.loanmanagement.service.CreditScoreService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrower")
@PreAuthorize("hasRole('BORROWER')")
@Tag(name = "Borrower", description = "Borrower endpoints for loan applications and payments")
@SecurityRequirement(name = "bearerAuth")
public class BorrowerController {

    private final LoanService loanService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final ReportService reportService;
    private final CreditScoreService creditScoreService;

    public BorrowerController(LoanService loanService, PaymentService paymentService,
            UserService userService, ReportService reportService, CreditScoreService creditScoreService) {
        this.loanService = loanService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.reportService = reportService;
        this.creditScoreService = creditScoreService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get borrower dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        User borrower = userService.getCurrentUser();
        Map<String, Object> dashboard = reportService.getBorrowerDashboard(borrower);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/loan-offers")
    @Operation(summary = "Get available loan offers")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getAvailableLoanOffers() {
        List<LoanResponse> offers = loanService.getAvailableLoanOffers();
        return ResponseEntity.ok(ApiResponse.success(offers));
    }

    @PostMapping("/applications")
    @Operation(summary = "Submit a loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> submitApplication(
            @Valid @RequestBody LoanApplicationRequest request) {
        User borrower = userService.getCurrentUser();
        LoanApplicationResponse application = loanService.submitApplication(borrower, request);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", application));
    }

    @GetMapping("/applications")
    @Operation(summary = "Get my loan applications")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getMyApplications() {
        User borrower = userService.getCurrentUser();
        List<LoanApplicationResponse> applications = loanService.getApplicationsByBorrower(borrower);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/loans")
    @Operation(summary = "Get my loans")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans() {
        User borrower = userService.getCurrentUser();
        List<LoanResponse> loans = loanService.getLoansByBorrower(borrower);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/loans/{id}")
    @Operation(summary = "Get loan details")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable Long id) {
        LoanResponse loan = loanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/loans/{id}/schedule")
    @Operation(summary = "Get payment schedule for a loan")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentSchedule(@PathVariable Long id) {
        List<PaymentResponse> schedule = paymentService.getPaymentSchedule(id);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all my payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments() {
        User borrower = userService.getCurrentUser();
        List<PaymentResponse> payments = paymentService.getPaymentsByBorrower(borrower.getId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/payments")
    @Operation(summary = "Make a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(@Valid @RequestBody PaymentRequest request) {
        User borrower = userService.getCurrentUser();
        PaymentResponse payment = paymentService.makePayment(borrower, request);
        return ResponseEntity.ok(ApiResponse.success("Payment successful", payment));
    }

    @PostMapping("/payments/{id}/mark-paid")
    @Operation(summary = "Mark payment as paid (sends for lender approval)")
    public ResponseEntity<ApiResponse<PaymentResponse>> markPaymentAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionReference) {
        User borrower = userService.getCurrentUser();
        PaymentResponse payment = paymentService.markAsPaid(borrower, id, transactionReference);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as paid. Awaiting lender approval.", payment));
    }

    @GetMapping("/credit-score")
    @Operation(summary = "Get my credit score")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> getMyCreditScore() {
        User borrower = userService.getCurrentUser();
        CreditScoreResponse creditScore = creditScoreService.getCreditScore(borrower);
        return ResponseEntity.ok(ApiResponse.success(creditScore));
    }

    @PutMapping("/credit-score")
    @Operation(summary = "Update my credit score manually")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> updateMyCreditScore(
            @Valid @RequestBody CreditScoreRequest request) {
        User borrower = userService.getCurrentUser();
        CreditScoreResponse creditScore = creditScoreService.updateCreditScore(borrower, request);
        return ResponseEntity.ok(ApiResponse.success("Credit score updated successfully", creditScore));
    }

    @PostMapping("/credit-score/calculate")
    @Operation(summary = "Calculate credit score based on financial information")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> calculateMyCreditScore(
            @Valid @RequestBody CreditScoreRequest request) {
        User borrower = userService.getCurrentUser();
        CreditScoreResponse creditScore = creditScoreService.calculateCreditScore(borrower, request);
        return ResponseEntity.ok(ApiResponse.success("Credit score calculated successfully", creditScore));
    }
}
