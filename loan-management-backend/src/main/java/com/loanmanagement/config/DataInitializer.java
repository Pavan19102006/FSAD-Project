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
import java.time.LocalDateTime;

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
        // Create demo users
        User admin = createUserIfNotExists("admin@12club.com", "admin123", "System", "Administrator",
                "+1234567890", Role.ADMIN, null, null, null);
        User lender = createUserIfNotExists("lender@12club.com", "lender123", "John", "Lender",
                "+1234567891", Role.LENDER, new BigDecimal("250000"), "BUSINESS_OWNER", null);
        User borrower = createUserIfNotExists("borrower@12club.com", "borrower123", "Jane", "Borrower",
                "+1234567892", Role.BORROWER, new BigDecimal("65000"), "EMPLOYED", 720);
        User analyst = createUserIfNotExists("analyst@12club.com", "analyst123", "Alex", "Analyst",
                "+1234567893", Role.ANALYST, null, null, null);

        // Only seed demo data if no loans exist yet (avoid duplicating on restart)
        if (loanRepository.count() == 0 && lender != null && borrower != null) {
            seedDemoData(lender, borrower);
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

    private void seedDemoData(User lender, User borrower) {
        log.info("Seeding demo data for demonstration...");

        // ============================================================
        // LOAN 1: Active Home Renovation Loan ($25,000 @ 10% for 12 months)
        // ============================================================
        Loan loan1 = Loan.builder()
                .lender(lender)
                .borrower(borrower)
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

        // Payments for Loan 1: 4 paid, 1 overdue, rest pending
        BigDecimal emi1 = loan1.getMonthlyPayment();
        for (int i = 1; i <= 12; i++) {
            LocalDate dueDate = loan1.getStartDate().plusMonths(i);
            Payment payment = Payment.builder()
                    .loan(loan1)
                    .paymentNumber(i)
                    .amountDue(emi1)
                    .principalPortion(emi1.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP))
                    .interestPortion(emi1.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP))
                    .dueDate(dueDate)
                    .lateFee(BigDecimal.ZERO)
                    .build();

            if (i <= 3) {
                // First 3 paid on time
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setAmountPaid(emi1);
                payment.setPaidDate(dueDate.minusDays(2));
                payment.setPaymentMethod("BANK_TRANSFER");
                payment.setTransactionReference("TXN-DEMO-L1-" + i);
            } else if (i == 4) {
                // 4th paid late
                payment.setStatus(PaymentStatus.LATE);
                payment.setAmountPaid(emi1.add(new BigDecimal("50.00")));
                payment.setLateFee(new BigDecimal("50.00"));
                payment.setPaidDate(dueDate.plusDays(10));
                payment.setPaymentMethod("UPI");
                payment.setTransactionReference("TXN-DEMO-L1-4-LATE");
            } else if (i == 5 && dueDate.isBefore(LocalDate.now())) {
                // 5th overdue
                payment.setStatus(PaymentStatus.OVERDUE);
                payment.setLateFee(new BigDecimal("75.00"));
            } else {
                // Future payments pending
                payment.setStatus(PaymentStatus.PENDING);
            }
            paymentRepository.save(payment);
        }
        log.info("Created Loan #1: Active Home Renovation ($25,000)");

        // ============================================================
        // LOAN 2: Completed Education Loan ($10,000 @ 8% for 6 months)
        // ============================================================
        Loan loan2 = Loan.builder()
                .lender(lender)
                .borrower(borrower)
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

        // All 6 payments completed
        BigDecimal emi2 = loan2.getMonthlyPayment();
        for (int i = 1; i <= 6; i++) {
            Payment payment = Payment.builder()
                    .loan(loan2)
                    .paymentNumber(i)
                    .amountDue(emi2)
                    .principalPortion(emi2.multiply(new BigDecimal("0.82")).setScale(2, RoundingMode.HALF_UP))
                    .interestPortion(emi2.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP))
                    .dueDate(loan2.getStartDate().plusMonths(i))
                    .status(PaymentStatus.COMPLETED)
                    .amountPaid(emi2)
                    .lateFee(BigDecimal.ZERO)
                    .build();
            payment.setPaidDate(loan2.getStartDate().plusMonths(i).minusDays(1));
            payment.setPaymentMethod("BANK_TRANSFER");
            payment.setTransactionReference("TXN-DEMO-L2-" + i);
            paymentRepository.save(payment);
        }
        log.info("Created Loan #2: Completed Education ($10,000)");

        // ============================================================
        // LOAN 3: Pending loan offer ($50,000 @ 12% for 24 months) - no borrower yet
        // ============================================================
        Loan loan3 = Loan.builder()
                .lender(lender)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("12.00"))
                .termMonths(24)
                .status(LoanStatus.PENDING)
                .description("Business expansion loan - open to applications")
                .build();
        loan3.setMonthlyPayment(loan3.calculateMonthlyPayment());
        loan3.setTotalInterest(loan3.calculateTotalInterest());
        loan3 = loanRepository.save(loan3);
        log.info("Created Loan #3: Pending Business Offer ($50,000)");

        // ============================================================
        // LOAN 4: Another pending offer ($15,000 @ 9% for 12 months)
        // ============================================================
        Loan loan4 = Loan.builder()
                .lender(lender)
                .principalAmount(new BigDecimal("15000.00"))
                .interestRate(new BigDecimal("9.00"))
                .termMonths(12)
                .status(LoanStatus.PENDING)
                .description("Personal loan for medical expenses")
                .build();
        loan4.setMonthlyPayment(loan4.calculateMonthlyPayment());
        loan4.setTotalInterest(loan4.calculateTotalInterest());
        loan4 = loanRepository.save(loan4);
        log.info("Created Loan #4: Pending Personal Offer ($15,000)");

        // ============================================================
        // LOAN 5: Defaulted loan ($8,000 @ 15% for 6 months)
        // ============================================================
        Loan loan5 = Loan.builder()
                .lender(lender)
                .borrower(borrower)
                .principalAmount(new BigDecimal("8000.00"))
                .interestRate(new BigDecimal("15.00"))
                .termMonths(6)
                .status(LoanStatus.DEFAULTED)
                .description("Short-term emergency loan - defaulted")
                .startDate(LocalDate.now().minusMonths(8))
                .endDate(LocalDate.now().minusMonths(2))
                .build();
        loan5.setMonthlyPayment(loan5.calculateMonthlyPayment());
        loan5.setTotalInterest(loan5.calculateTotalInterest());
        loan5.setRemainingBalance(new BigDecimal("5500.00"));
        loan5.setTotalPenaltyAccrued(new BigDecimal("350.00"));
        loan5 = loanRepository.save(loan5);

        // 2 paid, 4 missed
        BigDecimal emi5 = loan5.getMonthlyPayment();
        for (int i = 1; i <= 6; i++) {
            Payment payment = Payment.builder()
                    .loan(loan5)
                    .paymentNumber(i)
                    .amountDue(emi5)
                    .principalPortion(emi5.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP))
                    .interestPortion(emi5.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP))
                    .dueDate(loan5.getStartDate().plusMonths(i))
                    .lateFee(BigDecimal.ZERO)
                    .build();
            if (i <= 2) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setAmountPaid(emi5);
                payment.setPaidDate(loan5.getStartDate().plusMonths(i));
                payment.setPaymentMethod("CARD");
                payment.setTransactionReference("TXN-DEMO-L5-" + i);
            } else {
                payment.setStatus(PaymentStatus.MISSED);
                payment.setLateFee(new BigDecimal("85.00"));
            }
            paymentRepository.save(payment);
        }
        log.info("Created Loan #5: Defaulted Emergency ($8,000)");

        // ============================================================
        // LOAN APPLICATIONS
        // ============================================================

        // Application 1: Approved (for loan1)
        LoanApplication app1 = LoanApplication.builder()
                .borrower(borrower)
                .loan(loan1)
                .requestedAmount(new BigDecimal("25000.00"))
                .requestedTermMonths(12)
                .purpose("Home renovation - kitchen and bathroom remodel")
                .annualIncome(new BigDecimal("65000.00"))
                .employmentStatus("EMPLOYED")
                .creditScore(720)
                .status(ApplicationStatus.APPROVED)
                .build();
        loanApplicationRepository.save(app1);

        // Application 2: Pending (for loan3 - business expansion)
        LoanApplication app2 = LoanApplication.builder()
                .borrower(borrower)
                .loan(loan3)
                .requestedAmount(new BigDecimal("50000.00"))
                .requestedTermMonths(24)
                .purpose("Business expansion - opening second retail location")
                .annualIncome(new BigDecimal("65000.00"))
                .employmentStatus("EMPLOYED")
                .creditScore(720)
                .status(ApplicationStatus.PENDING)
                .build();
        loanApplicationRepository.save(app2);

        // Application 3: Rejected
        LoanApplication app3 = LoanApplication.builder()
                .borrower(borrower)
                .loan(loan4)
                .requestedAmount(new BigDecimal("15000.00"))
                .requestedTermMonths(12)
                .purpose("Medical expenses for family member surgery")
                .annualIncome(new BigDecimal("65000.00"))
                .employmentStatus("EMPLOYED")
                .creditScore(720)
                .status(ApplicationStatus.REJECTED)
                .build();
        app3.setRejectionReason("Debt-to-income ratio too high with existing active loans");
        loanApplicationRepository.save(app3);

        log.info("Created 3 loan applications (1 approved, 1 pending, 1 rejected)");
        log.info("Demo data seeding complete! Loans: {}, Payments: {}, Applications: {}",
                loanRepository.count(), paymentRepository.count(), loanApplicationRepository.count());
    }
}
