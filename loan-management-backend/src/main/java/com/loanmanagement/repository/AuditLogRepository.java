package com.loanmanagement.repository;

import com.loanmanagement.entity.AuditAction;
import com.loanmanagement.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user ID
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find audit logs by entity type and ID
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    /**
     * Find audit logs within date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent audit logs with pagination
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);

    /**
     * Find login attempts for a user
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userEmail = :email AND a.action IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED') ORDER BY a.timestamp DESC")
    List<AuditLog> findLoginAttemptsByEmail(@Param("email") String email);

    /**
     * Count failed login attempts in last N hours
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userEmail = :email AND a.action = 'LOGIN_FAILED' AND a.timestamp > :since")
    long countFailedLoginsSince(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Find suspicious activities (multiple failed logins, unusual access patterns)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' AND a.timestamp > :since GROUP BY a.ipAddress HAVING COUNT(a) > :threshold")
    List<AuditLog> findSuspiciousActivities(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
}
