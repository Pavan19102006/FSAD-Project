package com.loanmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking all system changes for audit purposes.
 * Records who did what, when, and what changed.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // LOAN, PAYMENT, USER, APPLICATION, etc.

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON representation of old state

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON representation of new state

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Builder pattern
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Long userId;
        private String userEmail;
        private String userRole;
        private AuditAction action;
        private String entityType;
        private Long entityId;
        private String oldValue;
        private String newValue;
        private String description;
        private String ipAddress;
        private String userAgent;

        public AuditLogBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuditLogBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public AuditLogBuilder userRole(String userRole) {
            this.userRole = userRole;
            return this;
        }

        public AuditLogBuilder action(AuditAction action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public AuditLogBuilder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public AuditLogBuilder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public AuditLogBuilder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public AuditLogBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuditLogBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.userId = this.userId;
            log.userEmail = this.userEmail;
            log.userRole = this.userRole;
            log.action = this.action;
            log.entityType = this.entityType;
            log.entityId = this.entityId;
            log.oldValue = this.oldValue;
            log.newValue = this.newValue;
            log.description = this.description;
            log.ipAddress = this.ipAddress;
            log.userAgent = this.userAgent;
            return log;
        }
    }
}
