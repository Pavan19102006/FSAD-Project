package com.loanmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_RESENDS = 3;
    private static final SecureRandom secureRandom = new SecureRandom();

    private final ConcurrentHashMap<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        final String otp;
        final long createdAt;
        int attempts;
        int resendCount;

        OtpEntry(String otp, int resendCount) {
            this.otp = otp;
            this.createdAt = System.currentTimeMillis();
            this.attempts = 0;
            this.resendCount = resendCount;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > OTP_EXPIRY_MS;
        }

        boolean isMaxAttemptsReached() {
            return attempts >= MAX_ATTEMPTS;
        }

        boolean canResend() {
            return resendCount < MAX_RESENDS;
        }
    }

    /**
     * Generate a 6-digit OTP for the given email.
     * Returns null if max resends exceeded.
     */
    public String generateOtp(String email) {
        // Clean expired entries
        otpStore.entrySet().removeIf(e -> e.getValue().isExpired());

        OtpEntry existing = otpStore.get(email);
        int resendCount = 0;
        if (existing != null && !existing.isExpired()) {
            if (!existing.canResend()) {
                return null; // Max resends reached
            }
            resendCount = existing.resendCount + 1;
        }

        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        otpStore.put(email, new OtpEntry(otp, resendCount));

        logger.info("========================================");
        logger.info("  OTP for {}: {}", email, otp);
        logger.info("========================================");

        return otp;
    }

    /**
     * Verify the OTP for the given email.
     * Returns a VerificationResult.
     */
    public VerificationResult verifyOtp(String email, String userOtp) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            return new VerificationResult(false, "No OTP found. Please request a new one.");
        }

        if (entry.isExpired()) {
            otpStore.remove(email);
            return new VerificationResult(false, "OTP has expired. Please request a new one.");
        }

        if (entry.isMaxAttemptsReached()) {
            otpStore.remove(email);
            return new VerificationResult(false, "Too many failed attempts. Please request a new OTP.");
        }

        entry.attempts++;

        if (entry.otp.equals(userOtp.trim())) {
            otpStore.remove(email);
            return new VerificationResult(true, "OTP verified successfully.");
        }

        int remaining = MAX_ATTEMPTS - entry.attempts;
        return new VerificationResult(false,
                "Invalid OTP. " + remaining + " attempt(s) remaining.");
    }

    /**
     * Check if resend is allowed for this email.
     */
    public boolean canResend(String email) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null || entry.isExpired()) return true;
        return entry.canResend();
    }

    public record VerificationResult(boolean success, String message) {}
}
