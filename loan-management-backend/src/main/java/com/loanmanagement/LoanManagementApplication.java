package com.loanmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LoanManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanManagementApplication.class, args);
    }
}
