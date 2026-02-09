package com.loanmanagement.service;

import com.loanmanagement.entity.AuditAction;
import com.loanmanagement.entity.AuditLog;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing audit logs.
 * Tracks all important actions in the system for compliance and debugging.
 */
@Service
@SuppressWarnings("null")
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For Java 8 date/time support
    }

    /**
     * Log an action with full details
     */
    @Async
    public void log(User user, AuditAction action, String entityType, Long entityId,
            Object oldValue, Object newValue, String description, HttpServletRequest request) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description);

            if (user != null) {
                builder.userId(user.getId())
                        .userEmail(user.getEmail())
                        .userRole(user.getRole().name());
            }

            if (oldValue != null) {
                builder.oldValue(toJson(oldValue));
            }

            if (newValue != null) {
                builder.newValue(toJson(newValue));
            }

            if (request != null) {
                builder.ipAddress(getClientIP(request))
                        .userAgent(request.getHeader("User-Agent"));
            }

            AuditLog log = builder.build();
            auditLogRepository.save(log);

            logger.debug("Audit log created: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log with minimal details
     */
    public void log(User user, AuditAction action, String entityType, Long entityId, String description) {
        log(user, action, entityType, entityId, null, null, description, null);
    }

    /**
     * Log a login event
     */
    public void logLogin(String email, boolean success, HttpServletRequest request) {
        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .action(success ? AuditAction.LOGIN : AuditAction.LOGIN_FAILED)
                .entityType("AUTH")
                .userEmail(email)
                .description(success ? "User logged in successfully" : "Login attempt failed");

        if (request != null) {
            builder.ipAddress(getClientIP(request))
                    .userAgent(request.getHeader("User-Agent"));
        }

        auditLogRepository.save(builder.build());
    }

    /**
     * Log a logout event
     */
    public void logLogout(User user, HttpServletRequest request) {
        log(user, AuditAction.LOGOUT, "AUTH", null, null, null, "User logged out", request);
    }

    /**
     * Log loan status change
     */
    public void logLoanStatusChange(User user, Long loanId, String oldStatus, String newStatus) {
        String description = String.format("Loan status changed from %s to %s", oldStatus, newStatus);
        log(user, AuditAction.STATUS_CHANGE, "LOAN", loanId, oldStatus, newStatus, description, null);
    }

    /**
     * Log payment
     */
    public void logPayment(User user, Long paymentId, Object paymentDetails) {
        log(user, AuditAction.PAYMENT, "PAYMENT", paymentId, null, paymentDetails,
                "Payment recorded", null);
    }

    /**
     * Log application approval/rejection
     */
    public void logApplicationReview(User reviewer, Long applicationId, boolean approved, String reason) {
        AuditAction action = approved ? AuditAction.APPROVE : AuditAction.REJECT;
        String description = approved ? "Application approved" : "Application rejected: " + reason;
        log(reviewer, action, "APPLICATION", applicationId, null, null, description, null);
    }

    /**
     * Get audit logs for a specific entity
     */
    public List<AuditLog> getEntityAuditTrail(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Get user's audit trail
     */
    public List<AuditLog> getUserAuditTrail(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Get recent audit logs with pagination
     */
    public Page<AuditLog> getRecentLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }

    /**
     * Get logs by date range
     */
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get login history for a user
     */
    public List<AuditLog> getLoginHistory(String email) {
        return auditLogRepository.findLoginAttemptsByEmail(email);
    }

    /**
     * Check for suspicious activity (multiple failed login attempts)
     */
    public boolean hasSuspiciousActivity(String email, int maxAttempts, int hoursToCheck) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursToCheck);
        long failedAttempts = auditLogRepository.countFailedLoginsSince(email, since);
        return failedAttempts >= maxAttempts;
    }

    /**
     * Get logs by action type
     */
    public List<AuditLog> getLogsByAction(AuditAction action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    // Helper methods
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }
}
