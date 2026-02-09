package com.loanmanagement.config;

import com.loanmanagement.entity.Role;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@12club.com")) {
            User admin = User.builder()
                    .email("admin@12club.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("+1234567890")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user: admin@12club.com / admin123");
        }

        if (!userRepository.existsByEmail("lender@12club.com")) {
            User lender = User.builder()
                    .email("lender@12club.com")
                    .password(passwordEncoder.encode("lender123"))
                    .firstName("John")
                    .lastName("Lender")
                    .phoneNumber("+1234567891")
                    .role(Role.LENDER)
                    .enabled(true)
                    .build();
            userRepository.save(lender);
            log.info("Created sample lender: lender@12club.com / lender123");
        }

        if (!userRepository.existsByEmail("borrower@12club.com")) {
            User borrower = User.builder()
                    .email("borrower@12club.com")
                    .password(passwordEncoder.encode("borrower123"))
                    .firstName("Jane")
                    .lastName("Borrower")
                    .phoneNumber("+1234567892")
                    .role(Role.BORROWER)
                    .enabled(true)
                    .build();
            userRepository.save(borrower);
            log.info("Created sample borrower: borrower@12club.com / borrower123");
        }

        if (!userRepository.existsByEmail("analyst@12club.com")) {
            User analyst = User.builder()
                    .email("analyst@12club.com")
                    .password(passwordEncoder.encode("analyst123"))
                    .firstName("Alex")
                    .lastName("Analyst")
                    .phoneNumber("+1234567893")
                    .role(Role.ANALYST)
                    .enabled(true)
                    .build();
            userRepository.save(analyst);
            log.info("Created sample analyst: analyst@12club.com / analyst123");
        }

        log.info("Data initialization complete. Total users: {}", userRepository.count());
    }
}
