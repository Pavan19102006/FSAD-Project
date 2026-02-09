package com.loanmanagement.service;

import com.loanmanagement.entity.*;
import com.loanmanagement.repository.EMIScheduleRepository;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Scheduled tasks for loan management automation.
 */
@Service
@SuppressWarnings("unused")
public class LoanSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(LoanSchedulerService.class);

    private final EMIScheduleRepository emiScheduleRepository;
    private final EMIScheduleService emiScheduleService;
    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;
    private final InterestCalculationService interestCalculationService;
    private final PaymentService paymentService;

    public LoanSchedulerService(EMIScheduleRepository emiScheduleRepository,
            EMIScheduleService emiScheduleService,
            PaymentRepository paymentRepository,
            LoanRepository loanRepository,
            NotificationService notificationService,
            InterestCalculationService interestCalculationService,
            PaymentService paymentService) {
        this.emiScheduleRepository = emiScheduleRepository;
        this.emiScheduleService = emiScheduleService;
        this.paymentRepository = paymentRepository;
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
        this.interestCalculationService = interestCalculationService;
        this.paymentService = paymentService;
    }

    /**
     * Daily job at 6 AM: Check and mark overdue payments, apply penalties
     */
    @Scheduled(cron = "0 0 6 * * *") // Every day at 6:00 AM
    @Transactional
    public void checkOverduePayments() {
        logger.info("Running scheduled task: Check overdue payments");

        LocalDate today = LocalDate.now();
        List<EMISchedule> overdueEMIs = emiScheduleRepository.findAllOverdueEMIs(today);

        int processed = 0;
        for (EMISchedule emi : overdueEMIs) {
            try {
                // Mark as overdue and apply penalty
                emiScheduleService.markAsOverdueWithPenalty(emi.getId());

                // Send notification to borrower
                Loan loan = emi.getLoan();
                if (loan.getBorrower() != null) {
                    BigDecimal penalty = emi.getPenaltyAmount() != null ? emi.getPenaltyAmount() : BigDecimal.ZERO;
                    notificationService.sendOverdueNotification(
                            loan.getBorrower(),
                            loan.getId(),
                            emi.getEmiNumber(),
                            emi.getEmiAmount(),
                            penalty);
                }
                processed++;
            } catch (Exception e) {
                logger.error("Error processing overdue EMI {}: {}", emi.getId(), e.getMessage());
            }
        }

        logger.info("Overdue check completed. Processed {} EMIs", processed);
    }

    /**
     * Daily job at 9 AM: Send EMI due reminders (T-3 days)
     */
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9:00 AM
    @Transactional(readOnly = true)
    public void sendEMIReminders() {
        logger.info("Running scheduled task: Send EMI reminders");

        // Find EMIs due in next 3 days
        List<EMISchedule> upcomingEMIs = emiScheduleService.getEMIsDueInDays(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        int sent = 0;
        for (EMISchedule emi : upcomingEMIs) {
            try {
                Loan loan = emi.getLoan();
                if (loan.getBorrower() != null) {
                    notificationService.sendEMIReminder(
                            loan.getBorrower(),
                            loan.getId(),
                            emi.getEmiNumber(),
                            emi.getEmiAmount(),
                            emi.getDueDate().format(formatter));
                    sent++;
                }
            } catch (Exception e) {
                logger.error("Error sending reminder for EMI {}: {}", emi.getId(), e.getMessage());
            }
        }

        logger.info("EMI reminders sent: {}", sent);
    }

    /**
     * Daily job at 10 AM: Send EMI due today notifications
     */
    @Scheduled(cron = "0 0 10 * * *") // Every day at 10:00 AM
    @Transactional(readOnly = true)
    public void sendEMIDueTodayNotifications() {
        logger.info("Running scheduled task: Send EMI due today notifications");

        List<EMISchedule> dueTodayEMIs = emiScheduleRepository.findByDueDateAndStatus(
                LocalDate.now(), EMIStatus.PENDING);

        for (EMISchedule emi : dueTodayEMIs) {
            try {
                Loan loan = emi.getLoan();
                if (loan.getBorrower() != null) {
                    notificationService.sendEMIDueToday(
                            loan.getBorrower(),
                            loan.getId(),
                            emi.getEmiNumber(),
                            emi.getEmiAmount());
                }
            } catch (Exception e) {
                logger.error("Error sending due today notification for EMI {}: {}", emi.getId(), e.getMessage());
            }
        }
    }

    /**
     * Weekly job (Sunday 11 PM): Check for loans that should be marked as defaulted
     */
    @Scheduled(cron = "0 0 23 * * SUN") // Every Sunday at 11:00 PM
    @Transactional
    public void checkDefaultedLoans() {
        logger.info("Running scheduled task: Check for defaulted loans");

        // Loans with payments overdue for more than 90 days
        LocalDate cutoffDate = LocalDate.now().minusDays(90);
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);

        int defaulted = 0;
        for (Loan loan : activeLoans) {
            List<EMISchedule> overdueEMIs = emiScheduleService.getOverdueEMIsForLoan(loan.getId());

            // Check if any EMI is overdue more than 90 days
            boolean hasLongOverdue = overdueEMIs.stream()
                    .anyMatch(emi -> emi.getDueDate().isBefore(cutoffDate));

            if (hasLongOverdue) {
                loan.setStatus(LoanStatus.DEFAULTED);
                loanRepository.save(loan);
                defaulted++;

                logger.warn("Loan {} marked as DEFAULTED", loan.getId());
            }
        }

        logger.info("Default check completed. {} loans marked as defaulted", defaulted);
    }

    /**
     * Monthly job (1st of month at 1 AM): Cleanup old notifications
     */
    @Scheduled(cron = "0 0 1 1 * *") // 1st of every month at 1:00 AM
    @Transactional
    public void cleanupOldNotifications() {
        logger.info("Running scheduled task: Cleanup old notifications");

        int deleted = notificationService.cleanupOldNotifications();
        logger.info("Deleted {} old notifications", deleted);
    }

    /**
     * Daily job at 7 AM: Update overdue payments and apply penalties
     */
    @Scheduled(cron = "0 0 7 * * *") // Every day at 7:00 AM
    @Transactional
    public void updateOverduePayments() {
        logger.info("Running scheduled task: Update overdue payments with penalties");
        paymentService.updateOverduePaymentsWithPenalty();
        logger.info("Overdue payments update completed");
    }

    /**
     * Manual trigger for testing: Run all daily tasks
     */
    public void runDailyTasks() {
        logger.info("Manually running all daily tasks");
        checkOverduePayments();
        sendEMIReminders();
        sendEMIDueTodayNotifications();
        updateOverduePayments();
    }
}
