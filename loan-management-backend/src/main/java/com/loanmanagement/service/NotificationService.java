package com.loanmanagement.service;

import com.loanmanagement.entity.*;
import com.loanmanagement.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user notifications.
 */
@Service
@SuppressWarnings("null")
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Create a notification for a user
     */
    public Notification createNotification(User user, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Create notification with entity reference
     */
    public Notification createNotification(User user, NotificationType type, String title,
            String message, String entityType, Long entityId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Notify about EMI reminder (3 days before due date)
     */
    public void sendEMIReminder(User borrower, Long loanId, int emiNumber, BigDecimal amount, String dueDate) {
        String title = "EMI Payment Reminder";
        String message = String.format(
                "Your EMI #%d of %s is due on %s. Please ensure timely payment to avoid late fees.",
                emiNumber, formatCurrency(amount), dueDate);

        createNotification(borrower, NotificationType.EMI_REMINDER, title, message, "LOAN", loanId);
        logger.info("EMI reminder sent to user {} for loan {}", borrower.getId(), loanId);
    }

    /**
     * Notify about EMI due today
     */
    public void sendEMIDueToday(User borrower, Long loanId, int emiNumber, BigDecimal amount) {
        String title = "EMI Payment Due Today";
        String message = String.format(
                "Your EMI #%d of %s is due today. Please make the payment to avoid late fees.",
                emiNumber, formatCurrency(amount));

        createNotification(borrower, NotificationType.EMI_DUE_TODAY, title, message, "LOAN", loanId);
    }

    /**
     * Notify about overdue payment
     */
    public void sendOverdueNotification(User borrower, Long loanId, int emiNumber,
            BigDecimal amount, BigDecimal penalty) {
        String title = "Payment Overdue";
        String message = String.format(
                "Your EMI #%d of %s is overdue. A late fee of %s has been applied. " +
                        "Total due: %s. Please pay immediately to avoid further penalties.",
                emiNumber, formatCurrency(amount), formatCurrency(penalty),
                formatCurrency(amount.add(penalty)));

        createNotification(borrower, NotificationType.PAYMENT_OVERDUE, title, message, "LOAN", loanId);
    }

    /**
     * Notify about payment received
     */
    public void sendPaymentConfirmation(User borrower, Long loanId, Long paymentId, BigDecimal amount) {
        String title = "Payment Received";
        String message = String.format(
                "We've received your payment of %s. Thank you for your timely payment!",
                formatCurrency(amount));

        createNotification(borrower, NotificationType.PAYMENT_RECEIVED, title, message, "PAYMENT", paymentId);
    }

    /**
     * Notify lender about loan application
     */
    public void notifyLoanApproved(User borrower, Long loanId, BigDecimal amount) {
        String title = "Loan Approved! 🎉";
        String message = String.format(
                "Great news! Your loan application for %s has been approved. " +
                        "The amount will be disbursed to your account shortly.",
                formatCurrency(amount));

        createNotification(borrower, NotificationType.LOAN_APPROVED, title, message, "LOAN", loanId);
    }

    /**
     * Notify about loan rejection
     */
    public void notifyLoanRejected(User borrower, Long applicationId, String reason) {
        String title = "Loan Application Update";
        String message = String.format(
                "We regret to inform you that your loan application could not be approved. " +
                        "Reason: %s. Please feel free to apply again with updated information.",
                reason != null ? reason : "Does not meet eligibility criteria");

        createNotification(borrower, NotificationType.LOAN_REJECTED, title, message, "APPLICATION", applicationId);
    }

    /**
     * Notify about loan completion
     */
    public void notifyLoanCompleted(User borrower, Long loanId) {
        String title = "Congratulations! Loan Fully Paid 🎉";
        String message = "Congratulations! You have successfully repaid your loan in full. " +
                "Thank you for being a valued customer!";

        createNotification(borrower, NotificationType.LOAN_COMPLETED, title, message, "LOAN", loanId);
    }

    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get unread notifications
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Count unread notifications
     */
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Cleanup old read notifications (older than 30 days)
     */
    @Transactional
    public int cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return notificationRepository.deleteOldReadNotifications(cutoff);
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("$%.2f", amount);
    }
}
