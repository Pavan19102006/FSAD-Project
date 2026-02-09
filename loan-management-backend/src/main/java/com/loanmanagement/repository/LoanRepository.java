package com.loanmanagement.repository;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.LoanStatus;
import com.loanmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByLender(User lender);

    List<Loan> findByBorrower(User borrower);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByLenderAndStatus(User lender, LoanStatus status);

    List<Loan> findByBorrowerAndStatus(User borrower, LoanStatus status);

    @Query("SELECT SUM(l.principalAmount) FROM Loan l WHERE l.status = :status")
    BigDecimal sumPrincipalByStatus(@Param("status") LoanStatus status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.lender = :lender AND l.status = :status")
    long countByLenderAndStatus(@Param("lender") User lender, @Param("status") LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.status = 'PENDING' AND l.borrower IS NULL")
    List<Loan> findAvailableLoanOffers();

    @Query("SELECT SUM(l.totalInterest) FROM Loan l WHERE l.lender = :lender AND l.status = 'COMPLETED'")
    BigDecimal sumEarnedInterestByLender(@Param("lender") User lender);
}
