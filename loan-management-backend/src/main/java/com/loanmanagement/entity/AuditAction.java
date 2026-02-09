package com.loanmanagement.entity;

/**
 * Types of audit actions for tracking
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    APPROVE,
    REJECT,
    PAYMENT,
    STATUS_CHANGE,
    PASSWORD_CHANGE,
    EXPORT,
    VIEW
}
