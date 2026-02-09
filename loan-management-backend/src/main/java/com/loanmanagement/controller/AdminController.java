package com.loanmanagement.controller;

import com.loanmanagement.dto.request.RegisterRequest;
import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.DashboardResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.entity.Role;
import com.loanmanagement.service.ReportService;
import com.loanmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin endpoints for platform management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final ReportService reportService;

    public AdminController(UserService userService, ReportService reportService) {
        this.userService = userService;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse dashboard = reportService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Toggle user enabled/disabled status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        UserResponse user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated", user));
    }

    @GetMapping("/users/{id}/credit-score")
    @Operation(summary = "Get user's credit score and risk info")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getUserCreditScore(@PathVariable Long id) {
        com.loanmanagement.entity.User user = userService.findById(id);
        java.util.Map<String, Object> creditInfo = new java.util.HashMap<>();
        creditInfo.put("userId", user.getId());
        creditInfo.put("name", user.getFullName());
        creditInfo.put("email", user.getEmail());
        creditInfo.put("creditScore", user.getCreditScore());
        creditInfo.put("riskScore", user.getRiskScore());
        creditInfo.put("riskLevel", user.getRiskLevel());
        creditInfo.put("annualIncome", user.getAnnualIncome());
        creditInfo.put("employmentStatus", user.getEmploymentStatus());
        return ResponseEntity.ok(ApiResponse.success("Credit score retrieved", creditInfo));
    }

    @PutMapping("/users/{id}/credit-score")
    @Operation(summary = "Update user's credit score manually")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateCreditScore(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> request) {
        com.loanmanagement.entity.User user = userService.findById(id);

        if (request.get("creditScore") != null) {
            user.setCreditScore(((Number) request.get("creditScore")).intValue());
        }
        if (request.get("riskLevel") != null) {
            user.setRiskLevel((String) request.get("riskLevel"));
        }
        if (request.get("annualIncome") != null) {
            user.setAnnualIncome(new java.math.BigDecimal(request.get("annualIncome").toString()));
        }
        if (request.get("employmentStatus") != null) {
            user.setEmploymentStatus((String) request.get("employmentStatus"));
        }

        userService.saveUser(user);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", user.getId());
        result.put("creditScore", user.getCreditScore());
        result.put("riskLevel", user.getRiskLevel());
        result.put("message", "Credit score updated successfully");
        return ResponseEntity.ok(ApiResponse.success("Credit score updated", result));
    }

    @GetMapping("/users/stats")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userService.countByRole(Role.ADMIN) + userService.countByRole(Role.LENDER)
                + userService.countByRole(Role.BORROWER) + userService.countByRole(Role.ANALYST));
        stats.put("totalAdmins", userService.countByRole(Role.ADMIN));
        stats.put("totalLenders", userService.countByRole(Role.LENDER));
        stats.put("totalBorrowers", userService.countByRole(Role.BORROWER));
        stats.put("totalAnalysts", userService.countByRole(Role.ANALYST));
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved", stats));
    }
}
