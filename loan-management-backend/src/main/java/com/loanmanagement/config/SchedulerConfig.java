package com.loanmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable scheduling and async processing
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {
    // Configuration for scheduled tasks
    // Tasks are defined in LoanSchedulerService
}
