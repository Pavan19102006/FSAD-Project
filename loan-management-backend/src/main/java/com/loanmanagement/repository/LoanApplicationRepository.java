package com.loanmanagement.repository;

import com.loanmanagement.entity.ApplicationStatus;
import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByBorrower(User borrower);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    List<LoanApplication> findByBorrowerAndStatus(User borrower, ApplicationStatus status);
    
    List<LoanApplication> findByBorrowerAndLoan(User borrower, Loan loan);
    
    List<LoanApplication> findByLoan(Loan loan);

    long countByStatus(ApplicationStatus status);

    List<LoanApplication> findByReviewedBy(User reviewer);
}
