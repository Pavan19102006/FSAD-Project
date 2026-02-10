package com.loanmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * KeepAlive service to prevent Render free-tier cold starts.
 * Pings the server's own health endpoint every 14 minutes
 * so the container never spins down due to inactivity.
 */
@Service
public class KeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveService.class);

    @Value("${app.keep-alive.url:}")
    private String keepAliveUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Runs every 14 minutes (Render spins down after 15 min of inactivity).
     * The self-ping keeps the service warm.
     */
    @Scheduled(fixedRate = 840000) // 14 minutes in milliseconds
    public void keepAlive() {
        if (keepAliveUrl == null || keepAliveUrl.isBlank()) {
            return; // Don't ping in local development
        }
        try {
            String response = restTemplate.getForObject(keepAliveUrl + "/actuator/health", String.class);
            log.info("🏓 Keep-alive ping successful: {}", response);
        } catch (Exception e) {
            log.warn("⚠️ Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
