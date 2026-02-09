package com.loanmanagement.repository;

import com.loanmanagement.entity.EMISchedule;
import com.loanmanagement.entity.EMIStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EMIScheduleRepository extends JpaRepository<EMISchedule, Long> {

    /**
     * Find all EMIs for a specific loan
     */
    List<EMISchedule> findByLoanIdOrderByEmiNumber(Long loanId);

    /**
     * Find EMIs by status for a loan
     */
    List<EMISchedule> findByLoanIdAndStatus(Long loanId, EMIStatus status);

    /**
     * Find next pending EMI for a loan
     */
    @Query("SELECT e FROM EMISchedule e WHERE e.loan.id = :loanId AND e.status = 'PENDING' ORDER BY e.emiNumber ASC LIMIT 1")
    EMISchedule findNextPendingEMI(@Param("loanId") Long loanId);

    /**
     * Find all overdue EMIs (past due date and not paid)
     */
    @Query("SELECT e FROM EMISchedule e WHERE e.dueDate < :currentDate AND e.status NOT IN ('PAID', 'WAIVED')")
    List<EMISchedule> findAllOverdueEMIs(@Param("currentDate") LocalDate currentDate);

    /**
     * Find overdue EMIs for a specific loan
     */
    @Query("SELECT e FROM EMISchedule e WHERE e.loan.id = :loanId AND e.dueDate < :currentDate AND e.status NOT IN ('PAID', 'WAIVED')")
    List<EMISchedule> findOverdueEMIsByLoan(@Param("loanId") Long loanId, @Param("currentDate") LocalDate currentDate);

    /**
     * Find EMIs due within next N days
     */
    @Query("SELECT e FROM EMISchedule e WHERE e.dueDate BETWEEN :startDate AND :endDate AND e.status = 'PENDING'")
    List<EMISchedule> findEMIsDueInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find EMIs due today
     */
    List<EMISchedule> findByDueDateAndStatus(LocalDate dueDate, EMIStatus status);

    /**
     * Count pending EMIs for a loan
     */
    long countByLoanIdAndStatus(Long loanId, EMIStatus status);

    /**
     * Get total outstanding amount for a loan
     */
    @Query("SELECT SUM(e.emiAmount - e.amountPaid + e.penaltyAmount) FROM EMISchedule e WHERE e.loan.id = :loanId AND e.status NOT IN ('PAID', 'WAIVED')")
    java.math.BigDecimal getTotalOutstandingByLoan(@Param("loanId") Long loanId);

    /**
     * Get total penalty amount for a loan
     */
    @Query("SELECT SUM(e.penaltyAmount) FROM EMISchedule e WHERE e.loan.id = :loanId")
    java.math.BigDecimal getTotalPenaltyByLoan(@Param("loanId") Long loanId);

    /**
     * Find EMIs by borrower
     */
    @Query("SELECT e FROM EMISchedule e WHERE e.loan.borrower.id = :borrowerId ORDER BY e.dueDate ASC")
    List<EMISchedule> findByBorrowerId(@Param("borrowerId") Long borrowerId);

    /**
     * Find upcoming EMIs for a borrower (next 30 days)
     */
    @Query("SELECT e FROM EMISchedule e WHERE e.loan.borrower.id = :borrowerId AND e.dueDate BETWEEN :startDate AND :endDate AND e.status = 'PENDING' ORDER BY e.dueDate ASC")
    List<EMISchedule> findUpcomingEMIsForBorrower(@Param("borrowerId") Long borrowerId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
