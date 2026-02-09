package com.loanmanagement.repository;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.Transaction;
import com.loanmanagement.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByLoan(Loan loan);

    List<Transaction> findByLoanOrderByCreatedAtDesc(Loan loan);

    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumAmountByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.loan.id = :loanId ORDER BY t.createdAt DESC")
    List<Transaction> findByLoanIdOrderByCreatedAtDesc(@Param("loanId") Long loanId);

    @Query("SELECT t FROM Transaction t WHERE t.loan.id = :loanId")
    List<Transaction> findByLoanId(@Param("loanId") Long loanId);
}
