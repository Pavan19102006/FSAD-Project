package com.loanmanagement.controller;

import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.entity.AuditLog;
import com.loanmanagement.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for viewing audit logs (Admin only)
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Get recent audit logs with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLog> logsPage = auditService.getRecentLogs(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("logs", logsPage.getContent().stream()
                .map(this::mapAuditLog)
                .collect(Collectors.toList()));
        response.put("currentPage", logsPage.getNumber());
        response.put("totalPages", logsPage.getTotalPages());
        response.put("totalItems", logsPage.getTotalElements());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Audit logs retrieved")
                .data(response)
                .build());
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AuditLog> logs = auditService.getLogsByDateRange(startDate, endDate);

        List<Map<String, Object>> response = logs.stream()
                .map(this::mapAuditLog)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Audit logs retrieved")
                .data(response)
                .build());
    }

    /**
     * Get audit trail for a specific entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEntityAuditTrail(
            @PathVariable String entityType,
            @PathVariable Long entityId) {

        List<AuditLog> logs = auditService.getEntityAuditTrail(entityType.toUpperCase(), entityId);

        List<Map<String, Object>> response = logs.stream()
                .map(this::mapAuditLog)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Entity audit trail retrieved")
                .data(response)
                .build());
    }

    /**
     * Get user activity log
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserAuditTrail(
            @PathVariable Long userId) {

        List<AuditLog> logs = auditService.getUserAuditTrail(userId);

        List<Map<String, Object>> response = logs.stream()
                .map(this::mapAuditLog)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("User audit trail retrieved")
                .data(response)
                .build());
    }

    /**
     * Get login history for an email
     */
    @GetMapping("/login-history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLoginHistory(
            @RequestParam String email) {

        List<AuditLog> logs = auditService.getLoginHistory(email);

        List<Map<String, Object>> response = logs.stream()
                .map(this::mapAuditLog)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Login history retrieved")
                .data(response)
                .build());
    }

    private Map<String, Object> mapAuditLog(AuditLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUserId());
        map.put("userEmail", log.getUserEmail());
        map.put("userRole", log.getUserRole());
        map.put("action", log.getAction().name());
        map.put("entityType", log.getEntityType());
        map.put("entityId", log.getEntityId());
        map.put("description", log.getDescription());
        map.put("timestamp", log.getTimestamp());
        map.put("ipAddress", log.getIpAddress());
        // Exclude old/new values for list views to reduce payload
        return map;
    }
}
