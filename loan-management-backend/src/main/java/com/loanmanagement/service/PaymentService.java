package com.loanmanagement.service;

import com.loanmanagement.dto.request.PaymentRequest;
import com.loanmanagement.dto.response.PaymentResponse;
import com.loanmanagement.entity.*;
import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.PaymentRepository;
import com.loanmanagement.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;

    public PaymentService(PaymentRepository paymentRepository, LoanRepository loanRepository,
            TransactionRepository transactionRepository) {
        this.paymentRepository = paymentRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<PaymentResponse> getPaymentSchedule(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        return paymentRepository.findByLoanOrderByPaymentNumberAsc(loan).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByBorrower(Long borrowerId) {
        return paymentRepository.findByBorrowerIdOrderByDueDateAsc(borrowerId).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse makePayment(User borrower, PaymentRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId()));

        Loan loan = payment.getLoan();

        if (!loan.getBorrower().getId().equals(borrower.getId())) {
            throw new BadRequestException("You are not authorized to make this payment");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment has already been made");
        }

        if (request.getAmount().compareTo(payment.getAmountDue()) < 0) {
            throw new BadRequestException("Payment amount is less than the required amount");
        }

        LocalDate today = LocalDate.now();
        boolean isLate = today.isAfter(payment.getDueDate());

        BigDecimal lateFee = BigDecimal.ZERO;
        if (isLate) {
            lateFee = payment.getAmountDue().multiply(BigDecimal.valueOf(0.05))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            payment.setLateFee(lateFee);
        }

        payment.setPaidDate(today);
        payment.setAmountPaid(request.getAmount());
        payment.setStatus(isLate ? PaymentStatus.LATE : PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);

        BigDecimal principalPaid = payment.getPrincipalPortion() != null ? payment.getPrincipalPortion()
                : BigDecimal.ZERO;
        loan.setRemainingBalance(loan.getRemainingBalance().subtract(principalPaid));

        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setRemainingBalance(BigDecimal.ZERO);
            loan.setStatus(LoanStatus.COMPLETED);
        }
        loanRepository.save(loan);

        recordTransaction(loan, TransactionType.PAYMENT, payment.getAmountPaid(),
                String.format("Payment #%d", payment.getPaymentNumber()));

        if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(loan, TransactionType.PENALTY, lateFee,
                    String.format("Late fee for payment #%d", payment.getPaymentNumber()));
        }

        return PaymentResponse.fromEntity(payment);
    }

    public List<PaymentResponse> getOverduePayments() {
        return paymentRepository.findOverduePayments(LocalDate.now()).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMissedPayments() {
        List<Payment> overduePayments = paymentRepository.findOverduePayments(LocalDate.now());

        for (Payment payment : overduePayments) {
            if (LocalDate.now().isAfter(payment.getDueDate().plusDays(30))) {
                payment.setStatus(PaymentStatus.MISSED);
                paymentRepository.save(payment);

                long missedPayments = paymentRepository.countMissedPaymentsByLoan(payment.getLoan());
                if (missedPayments >= 3) {
                    Loan loan = payment.getLoan();
                    loan.setStatus(LoanStatus.DEFAULTED);
                    loanRepository.save(loan);
                }
            }
        }
    }

    public BigDecimal getTotalPaidForLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        BigDecimal total = paymentRepository.sumCompletedPaymentsByLoan(loan);
        return total != null ? total : BigDecimal.ZERO;
    }

    private void recordTransaction(Loan loan, TransactionType type, BigDecimal amount, String description) {
        Transaction transaction = Transaction.builder()
                .loan(loan)
                .type(type)
                .amount(amount)
                .description(description)
                .build();

        transactionRepository.save(transaction);
    }

    /**
     * Borrower marks a payment as paid (sends for lender approval)
     */
    @Transactional
    public PaymentResponse markAsPaid(User borrower, Long paymentId, String transactionReference) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        Loan loan = payment.getLoan();

        if (!loan.getBorrower().getId().equals(borrower.getId())) {
            throw new BadRequestException("You are not authorized to mark this payment");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment has already been completed");
        }

        if (payment.getStatus() == PaymentStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Payment is already pending approval");
        }

        payment.setStatus(PaymentStatus.PENDING_APPROVAL);
        payment.setTransactionReference(transactionReference);
        payment.setNotes("Marked as paid by borrower, awaiting lender approval");
        payment = paymentRepository.save(payment);

        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Get all payments pending approval for a lender
     */
    public List<PaymentResponse> getPendingApprovalPayments(User lender) {
        return paymentRepository.findByLenderAndStatus(lender.getId(), PaymentStatus.PENDING_APPROVAL).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lender approves a payment
     */
    @Transactional
    public PaymentResponse approvePayment(User lender, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        Loan loan = payment.getLoan();

        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new BadRequestException("You are not authorized to approve this payment");
        }

        if (payment.getStatus() != PaymentStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Payment is not pending approval");
        }

        LocalDate today = LocalDate.now();
        boolean isLate = today.isAfter(payment.getDueDate());

        payment.setPaidDate(today);
        payment.setAmountPaid(payment.getAmountDue());
        payment.setStatus(isLate ? PaymentStatus.LATE : PaymentStatus.PAID);
        payment.setNotes("Payment approved by lender");
        payment = paymentRepository.save(payment);

        // Update loan balance
        BigDecimal principalPaid = payment.getPrincipalPortion() != null ? payment.getPrincipalPortion()
                : BigDecimal.ZERO;
        loan.setRemainingBalance(loan.getRemainingBalance().subtract(principalPaid));

        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setRemainingBalance(BigDecimal.ZERO);
            loan.setStatus(LoanStatus.COMPLETED);
        }
        loanRepository.save(loan);

        recordTransaction(loan, TransactionType.PAYMENT, payment.getAmountPaid(),
                String.format("Payment #%d approved", payment.getPaymentNumber()));

        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Lender rejects a payment
     */
    @Transactional
    public PaymentResponse rejectPayment(User lender, Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        Loan loan = payment.getLoan();

        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new BadRequestException("You are not authorized to reject this payment");
        }

        if (payment.getStatus() != PaymentStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Payment is not pending approval");
        }

        // Check if overdue
        LocalDate today = LocalDate.now();
        if (today.isAfter(payment.getDueDate())) {
            payment.setStatus(PaymentStatus.OVERDUE);
            // Apply penalty using loan's penalty rate (default to 2% if not set)
            BigDecimal penaltyRate = loan.getPenaltyRate() != null ? loan.getPenaltyRate() : BigDecimal.valueOf(2);
            BigDecimal penalty = payment.getAmountDue().multiply(penaltyRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            payment.setLateFee(payment.getLateFee() != null ? payment.getLateFee().add(penalty) : penalty);
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }
        
        payment.setNotes("Payment rejected by lender: " + (reason != null ? reason : "No reason provided"));
        payment.setTransactionReference(null);
        payment = paymentRepository.save(payment);

        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Update overdue payments and apply penalties (called by scheduler)
     */
    @Transactional
    public void updateOverduePaymentsWithPenalty() {
        LocalDate today = LocalDate.now();
        List<Payment> overduePayments = paymentRepository.findOverduePayments(today);

        for (Payment payment : overduePayments) {
            if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.REJECTED) {
                payment.setStatus(PaymentStatus.OVERDUE);
                
                Loan loan = payment.getLoan();
                // Apply penalty using loan's penalty rate (default to 2% if not set)
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(payment.getDueDate(), today);
                if (daysOverdue > 0 && (payment.getLateFee() == null || payment.getLateFee().compareTo(BigDecimal.ZERO) == 0)) {
                    BigDecimal penaltyRate = loan.getPenaltyRate() != null ? loan.getPenaltyRate() : BigDecimal.valueOf(2);
                    BigDecimal penalty = payment.getAmountDue().multiply(penaltyRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    payment.setLateFee(penalty);
                    payment.setNotes(String.format("Payment overdue. %.2f%% penalty applied.", penaltyRate));
                    
                    recordTransaction(loan, TransactionType.PENALTY, penalty,
                            String.format("Penalty (%.2f%%) for overdue payment #%d", penaltyRate, payment.getPaymentNumber()));
                }
                
                paymentRepository.save(payment);
            }
        }
    }
}
