package com.loanmanagement.repository;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.Payment;
import com.loanmanagement.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByLoan(Loan loan);

    List<Payment> findByLoanOrderByPaymentNumberAsc(Loan loan);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByDueDateBeforeAndStatus(LocalDate date, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.loan.borrower.id = :borrowerId ORDER BY p.dueDate ASC")
    List<Payment> findByBorrowerIdOrderByDueDateAsc(@Param("borrowerId") Long borrowerId);

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan = :loan AND p.status = 'COMPLETED'")
    BigDecimal sumCompletedPaymentsByLoan(@Param("loan") Loan loan);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.loan = :loan AND p.status = 'MISSED'")
    long countMissedPaymentsByLoan(@Param("loan") Loan loan);

    @Query("SELECT p FROM Payment p WHERE p.dueDate <= :date AND p.status = 'PENDING'")
    List<Payment> findOverduePayments(@Param("date") LocalDate date);

    @Query("SELECT p FROM Payment p WHERE p.loan.borrower = :borrower")
    List<Payment> findByBorrower(@Param("borrower") com.loanmanagement.entity.User borrower);

    @Query("SELECT p FROM Payment p WHERE p.loan.lender.id = :lenderId AND p.status = :status ORDER BY p.dueDate ASC")
    List<Payment> findByLenderAndStatus(@Param("lenderId") Long lenderId, @Param("status") PaymentStatus status);

    // Sum of principal portions from paid/approved payments for lender's loans
    @Query("SELECT COALESCE(SUM(p.principalPortion), 0) FROM Payment p WHERE p.loan.lender.id = :lenderId AND (p.status = 'PAID' OR p.status = 'COMPLETED' OR p.status = 'LATE')")
    BigDecimal sumPrincipalReclaimedByLender(@Param("lenderId") Long lenderId);

    // Sum of interest portions from paid/approved payments for lender's loans
    @Query("SELECT COALESCE(SUM(p.interestPortion), 0) FROM Payment p WHERE p.loan.lender.id = :lenderId AND (p.status = 'PAID' OR p.status = 'COMPLETED' OR p.status = 'LATE')")
    BigDecimal sumInterestEarnedByLender(@Param("lenderId") Long lenderId);
}
