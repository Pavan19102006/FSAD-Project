package com.loanmanagement.config;

import com.loanmanagement.entity.*;
import com.loanmanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
            LoanRepository loanRepository,
            LoanApplicationRepository loanApplicationRepository,
            PaymentRepository paymentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ==== DEMO USERS ====
        // Admin
        createUserIfNotExists("admin@12club.com", "admin123", "System", "Administrator",
                "+1234567890", Role.ADMIN, null, null, null);

        // Lenders (3)
        User lender1 = createUserIfNotExists("lender@12club.com", "lender123", "John", "Lender",
                "+1234567891", Role.LENDER, new BigDecimal("250000"), "BUSINESS_OWNER", null);
        User lender2 = createUserIfNotExists("lender2@12club.com", "lender123", "Sarah", "Capital",
                "+1234567894", Role.LENDER, new BigDecimal("320000"), "BUSINESS_OWNER", null);
        User lender3 = createUserIfNotExists("lender3@12club.com", "lender123", "Michael", "Funds",
                "+1234567895", Role.LENDER, new BigDecimal("180000"), "SELF_EMPLOYED", null);

        // Borrowers (4)
        User borrower1 = createUserIfNotExists("borrower@12club.com", "borrower123", "Jane", "Borrower",
                "+1234567892", Role.BORROWER, new BigDecimal("65000"), "EMPLOYED", 720);
        User borrower2 = createUserIfNotExists("borrower2@12club.com", "borrower123", "David", "Wilson",
                "+1234567896", Role.BORROWER, new BigDecimal("85000"), "EMPLOYED", 680);
        User borrower3 = createUserIfNotExists("borrower3@12club.com", "borrower123", "Priya", "Sharma",
                "+1234567897", Role.BORROWER, new BigDecimal("52000"), "SELF_EMPLOYED", 750);
        User borrower4 = createUserIfNotExists("borrower4@12club.com", "borrower123", "Carlos", "Rivera",
                "+1234567898", Role.BORROWER, new BigDecimal("48000"), "EMPLOYED", 640);

        // Analysts (2)
        createUserIfNotExists("analyst@12club.com", "analyst123", "Alex", "Analyst",
                "+1234567893", Role.ANALYST, null, null, null);
        createUserIfNotExists("analyst2@12club.com", "analyst123", "Neha", "Insights",
                "+1234567899", Role.ANALYST, null, null, null);

        // Seed demo data
        if (loanRepository.count() == 0 && lender1 != null && borrower1 != null) {
            seedDemoData(lender1, lender2, lender3, borrower1, borrower2, borrower3, borrower4);
        }

        log.info("Data initialization complete. Total users: {}", userRepository.count());
    }

    private User createUserIfNotExists(String email, String password, String firstName, String lastName,
            String phone, Role role, BigDecimal income, String employment, Integer creditScore) {
        if (userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).orElse(null);
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phone)
                .role(role)
                .enabled(true)
                .build();
        if (income != null)
            user.setAnnualIncome(income);
        if (employment != null)
            user.setEmploymentStatus(employment);
        if (creditScore != null)
            user.setCreditScore(creditScore);
        user = userRepository.save(user);
        log.info("Created {} user: {} / {}", role, email, password);
        return user;
    }

    private void seedDemoData(User lender1, User lender2, User lender3,
            User borrower1, User borrower2, User borrower3, User borrower4) {
        log.info("Seeding rich demo data for demonstration...");

        // ============================================================
        // LOAN 1: Active Home Renovation ($25,000 @ 10% for 12mo) - lender1 → borrower1
        // ============================================================
        Loan loan1 = Loan.builder()
                .lender(lender1).borrower(borrower1)
                .principalAmount(new BigDecimal("25000.00"))
                .interestRate(new BigDecimal("10.00"))
                .termMonths(12)
                .status(LoanStatus.ACTIVE)
                .description("Home renovation loan - kitchen and bathroom remodel")
                .startDate(LocalDate.now().minusMonths(4))
                .endDate(LocalDate.now().plusMonths(8))
                .build();
        loan1.setMonthlyPayment(loan1.calculateMonthlyPayment());
        loan1.setTotalInterest(loan1.calculateTotalInterest());
        loan1.setRemainingBalance(new BigDecimal("17500.00"));
        loan1 = loanRepository.save(loan1);
        createPaymentsForActiveLoan(loan1, 4);
        log.info("Loan #1: Active Home Renovation ($25,000)");

        // ============================================================
        // LOAN 2: Completed Education Loan ($10,000 @ 8% for 6mo) - lender1 → borrower1
        // ============================================================
        Loan loan2 = Loan.builder()
                .lender(lender1).borrower(borrower1)
                .principalAmount(new BigDecimal("10000.00"))
                .interestRate(new BigDecimal("8.00"))
                .termMonths(6)
                .status(LoanStatus.COMPLETED)
                .description("Education loan for online certification course")
                .startDate(LocalDate.now().minusMonths(10))
                .endDate(LocalDate.now().minusMonths(4))
                .build();
        loan2.setMonthlyPayment(loan2.calculateMonthlyPayment());
        loan2.setTotalInterest(loan2.calculateTotalInterest());
        loan2.setRemainingBalance(BigDecimal.ZERO);
        loan2 = loanRepository.save(loan2);
        createCompletedPayments(loan2);
        log.info("Loan #2: Completed Education ($10,000)");

        // ============================================================
        // LOAN 3: Active Business Loan ($50,000 @ 12% for 24mo) - lender2 → borrower2
        // ============================================================
        Loan loan3 = Loan.builder()
                .lender(lender2).borrower(borrower2)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("12.00"))
                .termMonths(24)
                .status(LoanStatus.ACTIVE)
                .description("Business expansion loan - second retail location")
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(18))
                .build();
        loan3.setMonthlyPayment(loan3.calculateMonthlyPayment());
        loan3.setTotalInterest(loan3.calculateTotalInterest());
        loan3.setRemainingBalance(new BigDecimal("39000.00"));
        loan3 = loanRepository.save(loan3);
        createPaymentsForActiveLoan(loan3, 6);
        log.info("Loan #3: Active Business ($50,000)");

        // ============================================================
        // LOAN 4: Completed Car Loan ($18,000 @ 7.5% for 12mo) - lender2 → borrower3
        // ============================================================
        Loan loan4 = Loan.builder()
                .lender(lender2).borrower(borrower3)
                .principalAmount(new BigDecimal("18000.00"))
                .interestRate(new BigDecimal("7.50"))
                .termMonths(12)
                .status(LoanStatus.COMPLETED)
                .description("Used car purchase loan")
                .startDate(LocalDate.now().minusMonths(14))
                .endDate(LocalDate.now().minusMonths(2))
                .build();
        loan4.setMonthlyPayment(loan4.calculateMonthlyPayment());
        loan4.setTotalInterest(loan4.calculateTotalInterest());
        loan4.setRemainingBalance(BigDecimal.ZERO);
        loan4 = loanRepository.save(loan4);
        createCompletedPayments(loan4);
        log.info("Loan #4: Completed Car ($18,000)");

        // ============================================================
        // LOAN 5: Active Medical Loan ($15,000 @ 9% for 12mo) - lender3 → borrower3
        // ============================================================
        Loan loan5 = Loan.builder()
                .lender(lender3).borrower(borrower3)
                .principalAmount(new BigDecimal("15000.00"))
                .interestRate(new BigDecimal("9.00"))
                .termMonths(12)
                .status(LoanStatus.ACTIVE)
                .description("Medical emergency loan for family surgery")
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now().plusMonths(9))
                .build();
        loan5.setMonthlyPayment(loan5.calculateMonthlyPayment());
        loan5.setTotalInterest(loan5.calculateTotalInterest());
        loan5.setRemainingBalance(new BigDecimal("11500.00"));
        loan5 = loanRepository.save(loan5);
        createPaymentsForActiveLoan(loan5, 3);
        log.info("Loan #5: Active Medical ($15,000)");

        // ============================================================
        // LOAN 6: Defaulted Emergency Loan ($8,000 @ 15% for 6mo) - lender1 → borrower4
        // ============================================================
        Loan loan6 = Loan.builder()
                .lender(lender1).borrower(borrower4)
                .principalAmount(new BigDecimal("8000.00"))
                .interestRate(new BigDecimal("15.00"))
                .termMonths(6)
                .status(LoanStatus.DEFAULTED)
                .description("Short-term emergency loan - defaulted")
                .startDate(LocalDate.now().minusMonths(8))
                .endDate(LocalDate.now().minusMonths(2))
                .build();
        loan6.setMonthlyPayment(loan6.calculateMonthlyPayment());
        loan6.setTotalInterest(loan6.calculateTotalInterest());
        loan6.setRemainingBalance(new BigDecimal("5500.00"));
        loan6.setTotalPenaltyAccrued(new BigDecimal("350.00"));
        loan6 = loanRepository.save(loan6);
        createDefaultedPayments(loan6);
        log.info("Loan #6: Defaulted Emergency ($8,000)");

        // ============================================================
        // LOAN 7: Completed Personal Loan ($12,000 @ 11% for 6mo) - lender3 → borrower2
        // ============================================================
        Loan loan7 = Loan.builder()
                .lender(lender3).borrower(borrower2)
                .principalAmount(new BigDecimal("12000.00"))
                .interestRate(new BigDecimal("11.00"))
                .termMonths(6)
                .status(LoanStatus.COMPLETED)
                .description("Personal loan for home appliances and furniture")
                .startDate(LocalDate.now().minusMonths(9))
                .endDate(LocalDate.now().minusMonths(3))
                .build();
        loan7.setMonthlyPayment(loan7.calculateMonthlyPayment());
        loan7.setTotalInterest(loan7.calculateTotalInterest());
        loan7.setRemainingBalance(BigDecimal.ZERO);
        loan7 = loanRepository.save(loan7);
        createCompletedPayments(loan7);
        log.info("Loan #7: Completed Personal ($12,000)");

        // ============================================================
        // LOAN 8: Active Wedding Loan ($35,000 @ 10.5% for 18mo) - lender2 → borrower4
        // ============================================================
        Loan loan8 = Loan.builder()
                .lender(lender2).borrower(borrower4)
                .principalAmount(new BigDecimal("35000.00"))
                .interestRate(new BigDecimal("10.50"))
                .termMonths(18)
                .status(LoanStatus.ACTIVE)
                .description("Wedding and reception expenses loan")
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(16))
                .build();
        loan8.setMonthlyPayment(loan8.calculateMonthlyPayment());
        loan8.setTotalInterest(loan8.calculateTotalInterest());
        loan8.setRemainingBalance(new BigDecimal("31500.00"));
        loan8 = loanRepository.save(loan8);
        createPaymentsForActiveLoan(loan8, 2);
        log.info("Loan #8: Active Wedding ($35,000)");

        // ============================================================
        // LOAN 9: Pending Offer ($40,000 @ 11% for 24mo) - lender1, no borrower
        // ============================================================
        Loan loan9 = Loan.builder()
                .lender(lender1)
                .principalAmount(new BigDecimal("40000.00"))
                .interestRate(new BigDecimal("11.00"))
                .termMonths(24)
                .status(LoanStatus.PENDING)
                .description("Investment opportunity - open for applications")
                .build();
        loan9.setMonthlyPayment(loan9.calculateMonthlyPayment());
        loan9.setTotalInterest(loan9.calculateTotalInterest());
        loan9 = loanRepository.save(loan9);
        log.info("Loan #9: Pending Offer ($40,000)");

        // ============================================================
        // LOAN 10: Pending Offer ($20,000 @ 8.5% for 12mo) - lender3, no borrower
        // ============================================================
        Loan loan10 = Loan.builder()
                .lender(lender3)
                .principalAmount(new BigDecimal("20000.00"))
                .interestRate(new BigDecimal("8.50"))
                .termMonths(12)
                .status(LoanStatus.PENDING)
                .description("Education & skill development loan offer")
                .build();
        loan10.setMonthlyPayment(loan10.calculateMonthlyPayment());
        loan10.setTotalInterest(loan10.calculateTotalInterest());
        loan10 = loanRepository.save(loan10);
        log.info("Loan #10: Pending Offer ($20,000)");

        // ============================================================
        // LOAN APPLICATIONS (5 total: 2 approved, 2 pending, 1 rejected)
        // ============================================================

        // Approved for loan1
        LoanApplication app1 = LoanApplication.builder()
                .borrower(borrower1).loan(loan1)
                .requestedAmount(new BigDecimal("25000.00"))
                .requestedTermMonths(12)
                .purpose("Home renovation - kitchen and bathroom remodel")
                .annualIncome(new BigDecimal("65000.00"))
                .employmentStatus("EMPLOYED").creditScore(720)
                .status(ApplicationStatus.APPROVED)
                .build();
        loanApplicationRepository.save(app1);

        // Approved for loan3
        LoanApplication app2 = LoanApplication.builder()
                .borrower(borrower2).loan(loan3)
                .requestedAmount(new BigDecimal("50000.00"))
                .requestedTermMonths(24)
                .purpose("Business expansion - opening second retail location")
                .annualIncome(new BigDecimal("85000.00"))
                .employmentStatus("EMPLOYED").creditScore(680)
                .status(ApplicationStatus.APPROVED)
                .build();
        loanApplicationRepository.save(app2);

        // Pending for loan9
        LoanApplication app3 = LoanApplication.builder()
                .borrower(borrower3).loan(loan9)
                .requestedAmount(new BigDecimal("40000.00"))
                .requestedTermMonths(24)
                .purpose("Starting an e-commerce business")
                .annualIncome(new BigDecimal("52000.00"))
                .employmentStatus("SELF_EMPLOYED").creditScore(750)
                .status(ApplicationStatus.PENDING)
                .build();
        loanApplicationRepository.save(app3);

        // Pending for loan10
        LoanApplication app4 = LoanApplication.builder()
                .borrower(borrower4).loan(loan10)
                .requestedAmount(new BigDecimal("20000.00"))
                .requestedTermMonths(12)
                .purpose("Professional certification and training")
                .annualIncome(new BigDecimal("48000.00"))
                .employmentStatus("EMPLOYED").creditScore(640)
                .status(ApplicationStatus.PENDING)
                .build();
        loanApplicationRepository.save(app4);

        // Rejected
        LoanApplication app5 = LoanApplication.builder()
                .borrower(borrower4).loan(loan9)
                .requestedAmount(new BigDecimal("40000.00"))
                .requestedTermMonths(24)
                .purpose("Speculative stock trading capital")
                .annualIncome(new BigDecimal("48000.00"))
                .employmentStatus("EMPLOYED").creditScore(640)
                .status(ApplicationStatus.REJECTED)
                .build();
        app5.setRejectionReason("High-risk purpose and insufficient income for requested amount");
        loanApplicationRepository.save(app5);

        log.info("Created 5 loan applications (2 approved, 2 pending, 1 rejected)");
        log.info("Demo data seeding complete! Loans: {}, Payments: {}, Applications: {}",
                loanRepository.count(), paymentRepository.count(), loanApplicationRepository.count());
    }

    // ==== HELPER METHODS ====

    private void createPaymentsForActiveLoan(Loan loan, int paidCount) {
        BigDecimal emi = loan.getMonthlyPayment();
        int term = loan.getTermMonths();
        for (int i = 1; i <= term; i++) {
            LocalDate dueDate = loan.getStartDate().plusMonths(i);
            Payment payment = Payment.builder()
                    .loan(loan)
                    .paymentNumber(i)
                    .amountDue(emi)
                    .principalPortion(emi.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP))
                    .interestPortion(emi.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP))
                    .dueDate(dueDate)
                    .lateFee(BigDecimal.ZERO)
                    .build();

            if (i <= paidCount - 1) {
                // On-time payments
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setAmountPaid(emi);
                payment.setPaidDate(dueDate.minusDays(2));
                payment.setPaymentMethod("BANK_TRANSFER");
                payment.setTransactionReference("TXN-DEMO-" + loan.getId() + "-" + i);
            } else if (i == paidCount) {
                // Last paid - late payment
                payment.setStatus(PaymentStatus.LATE);
                payment.setAmountPaid(emi.add(new BigDecimal("50.00")));
                payment.setLateFee(new BigDecimal("50.00"));
                payment.setPaidDate(dueDate.plusDays(8));
                payment.setPaymentMethod("UPI");
                payment.setTransactionReference("TXN-DEMO-" + loan.getId() + "-" + i + "-LATE");
            } else {
                // Future pending
                payment.setStatus(PaymentStatus.PENDING);
            }
            paymentRepository.save(payment);
        }
    }

    private void createCompletedPayments(Loan loan) {
        BigDecimal emi = loan.getMonthlyPayment();
        int term = loan.getTermMonths();
        for (int i = 1; i <= term; i++) {
            Payment payment = Payment.builder()
                    .loan(loan)
                    .paymentNumber(i)
                    .amountDue(emi)
                    .principalPortion(emi.multiply(new BigDecimal("0.82")).setScale(2, RoundingMode.HALF_UP))
                    .interestPortion(emi.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP))
                    .dueDate(loan.getStartDate().plusMonths(i))
                    .status(PaymentStatus.COMPLETED)
                    .amountPaid(emi)
                    .lateFee(BigDecimal.ZERO)
                    .build();
            payment.setPaidDate(loan.getStartDate().plusMonths(i).minusDays(1));
            payment.setPaymentMethod("BANK_TRANSFER");
            payment.setTransactionReference("TXN-DEMO-" + loan.getId() + "-" + i);
            paymentRepository.save(payment);
        }
    }

    private void createDefaultedPayments(Loan loan) {
        BigDecimal emi = loan.getMonthlyPayment();
        int term = loan.getTermMonths();
        for (int i = 1; i <= term; i++) {
            Payment payment = Payment.builder()
                    .loan(loan)
                    .paymentNumber(i)
                    .amountDue(emi)
                    .principalPortion(emi.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP))
                    .interestPortion(emi.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP))
                    .dueDate(loan.getStartDate().plusMonths(i))
                    .lateFee(BigDecimal.ZERO)
                    .build();
            if (i <= 2) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setAmountPaid(emi);
                payment.setPaidDate(loan.getStartDate().plusMonths(i));
                payment.setPaymentMethod("CARD");
                payment.setTransactionReference("TXN-DEMO-" + loan.getId() + "-" + i);
            } else {
                payment.setStatus(PaymentStatus.MISSED);
                payment.setLateFee(new BigDecimal("85.00"));
            }
            paymentRepository.save(payment);
        }
    }
}
