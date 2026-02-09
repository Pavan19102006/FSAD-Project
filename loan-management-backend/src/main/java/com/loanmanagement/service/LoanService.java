package com.loanmanagement.service;

import com.loanmanagement.dto.request.CreateLoanRequest;
import com.loanmanagement.dto.request.LoanApplicationRequest;
import com.loanmanagement.dto.response.LoanApplicationResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.entity.*;
import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.LoanApplicationRepository;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.PaymentRepository;
import com.loanmanagement.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final LoanApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final EMIScheduleService emiScheduleService;
    private final InterestCalculationService interestCalculationService;

    public LoanService(LoanRepository loanRepository, LoanApplicationRepository applicationRepository,
            PaymentRepository paymentRepository, TransactionRepository transactionRepository,
            EMIScheduleService emiScheduleService, InterestCalculationService interestCalculationService) {
        this.loanRepository = loanRepository;
        this.applicationRepository = applicationRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.emiScheduleService = emiScheduleService;
        this.interestCalculationService = interestCalculationService;
    }

    @Transactional
    public LoanResponse createLoanOffer(User lender, CreateLoanRequest request) {
        Loan loan = Loan.builder()
                .lender(lender)
                .principalAmount(request.getPrincipalAmount())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .description(request.getDescription() != null ? request.getDescription() : "Loan offer")
                .interestType(request.getInterestType() != null ? InterestType.valueOf(request.getInterestType()) : InterestType.COMPOUND)
                .emiType(request.getEmiType() != null ? EMIType.valueOf(request.getEmiType()) : EMIType.REDUCING_BALANCE)
                .penaltyRate(request.getPenaltyRate() != null ? request.getPenaltyRate() : new java.math.BigDecimal("2.00"))
                .status(LoanStatus.PENDING)
                .build();

        loan.setMonthlyPayment(loan.calculateMonthlyPayment());
        loan.setTotalInterest(loan.calculateTotalInterest());
        loan.setRemainingBalance(loan.getPrincipalAmount());

        loan = loanRepository.save(loan);
        return LoanResponse.fromEntity(loan);
    }

    public List<LoanResponse> getLoansByLender(User lender) {
        return loanRepository.findByLender(lender).stream()
                .map(LoanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getLoansByBorrower(User borrower) {
        return loanRepository.findByBorrower(borrower).stream()
                .map(LoanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getAvailableLoanOffers() {
        return loanRepository.findAvailableLoanOffers().stream()
                .map(LoanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
        return LoanResponse.fromEntity(loan);
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(LoanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanApplicationResponse submitApplication(User borrower, LoanApplicationRequest request) {
        // Find the loan offer the borrower is applying for
        Loan loanOffer = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan offer", "id", request.getLoanId()));
        
        // Check if loan offer is available (PENDING status and no borrower assigned)
        if (loanOffer.getStatus() != LoanStatus.PENDING || loanOffer.getBorrower() != null) {
            throw new BadRequestException("This loan offer is no longer available");
        }
        
        // Check if borrower already applied for this loan
        List<LoanApplication> existingApplications = applicationRepository.findByBorrowerAndLoan(borrower, loanOffer);
        if (!existingApplications.isEmpty()) {
            throw new BadRequestException("You have already applied for this loan offer");
        }
        
        LoanApplication application = LoanApplication.builder()
                .borrower(borrower)
                .loan(loanOffer)
                .requestedAmount(loanOffer.getPrincipalAmount())
                .requestedTermMonths(loanOffer.getTermMonths())
                .purpose(request.getPurpose())
                .annualIncome(request.getAnnualIncome())
                .employmentStatus(request.getEmploymentStatus())
                .creditScore(request.getCreditScore())
                .status(ApplicationStatus.PENDING)
                .build();

        application = applicationRepository.save(application);
        return LoanApplicationResponse.fromEntity(application);
    }

    public List<LoanApplicationResponse> getPendingApplications() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING).stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<LoanApplicationResponse> getApplicationsByBorrower(User borrower) {
        return applicationRepository.findByBorrower(borrower).stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponse approveApplication(Long applicationId, User lender) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", applicationId));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application is not pending");
        }

        // Get the existing loan offer that the borrower applied for
        Loan loan = application.getLoan();
        if (loan == null) {
            throw new BadRequestException("No loan offer associated with this application");
        }
        
        // Verify the lender owns this loan
        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new BadRequestException("You can only approve applications for your own loan offers");
        }
        
        // Assign borrower to the existing loan and activate it
        loan.setBorrower(application.getBorrower());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(loan.getTermMonths()));
        
        // Calculate EMI with the loan's existing terms
        BigDecimal emi = interestCalculationService.calculateReducingBalanceEMI(
                loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTermMonths());
        BigDecimal totalInterest = interestCalculationService.calculateTotalInterestReducingBalance(
                loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTermMonths());
        
        loan.setMonthlyPayment(emi);
        loan.setTotalInterest(totalInterest);
        loan.setRemainingBalance(loan.getPrincipalAmount());

        loan = loanRepository.save(loan);
        logger.info("Loan approved and activated with ID: {}", loan.getId());

        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedBy(lender);
        application.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(application);

        // Generate both payment schedule (legacy) and EMI schedule (new)
        generatePaymentSchedule(loan);
        emiScheduleService.generateEMISchedule(loan);

        recordTransaction(loan, TransactionType.DISBURSEMENT, loan.getPrincipalAmount(),
                "Loan disbursement to borrower");

        return LoanResponse.fromEntity(loan);
    }

    @Transactional
    public LoanApplicationResponse rejectApplication(Long applicationId, User lender, String reason) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", applicationId));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application is not pending");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setReviewedBy(lender);
        application.setReviewedAt(LocalDateTime.now());

        application = applicationRepository.save(application);
        return LoanApplicationResponse.fromEntity(application);
    }

    private void generatePaymentSchedule(Loan loan) {
        List<Payment> payments = new ArrayList<>();
        BigDecimal monthlyPayment = loan.getMonthlyPayment();
        BigDecimal remainingBalance = loan.getPrincipalAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, java.math.RoundingMode.HALF_UP);

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interestPortion = remainingBalance.multiply(monthlyRate)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal principalPortion = monthlyPayment.subtract(interestPortion);

            if (i == loan.getTermMonths()) {
                principalPortion = remainingBalance;
                interestPortion = monthlyPayment.subtract(principalPortion);
            }

            remainingBalance = remainingBalance.subtract(principalPortion);
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            Payment payment = Payment.builder()
                    .loan(loan)
                    .paymentNumber(i)
                    .amountDue(monthlyPayment)
                    .principalPortion(principalPortion)
                    .interestPortion(interestPortion)
                    .dueDate(loan.getStartDate().plusMonths(i))
                    .status(PaymentStatus.PENDING)
                    .build();

            payments.add(payment);
        }

        paymentRepository.saveAll(payments);
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

    public long countByStatus(LoanStatus status) {
        return loanRepository.findByStatus(status).size();
    }

    public BigDecimal sumPrincipalByStatus(LoanStatus status) {
        BigDecimal sum = loanRepository.sumPrincipalByStatus(status);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
