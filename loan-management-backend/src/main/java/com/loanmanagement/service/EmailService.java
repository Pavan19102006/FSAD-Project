package com.loanmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Send OTP email. Falls back to console logging if SMTP is not configured.
     */
    public void sendOtpEmail(String toEmail, String otp) {
        // Always log to console for dev convenience
        logger.info("========================================");
        logger.info("  OTP for {}: {}", toEmail, otp);
        logger.info("========================================");

        if (mailSender == null) {
            logger.warn("Mail sender not configured. Check console for OTP.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("boppanapavan63@gmail.com", "THE 12%CLUB");
            helper.setTo(toEmail);
            helper.setSubject("Your Login Verification Code - THE 12%CLUB");
            helper.setText(buildOtpHtml(otp), true);

            mailSender.send(message);
            logger.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception e) {
            // Gracefully handle mail failures — OTP is already logged to console
            logger.warn("Failed to send OTP email to {} ({}). OTP is logged to console above.", toEmail, e.getMessage());
        }
    }

    private String buildOtpHtml(String otp) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='margin:0;padding:0;background:#f5f5f7;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,sans-serif;'>");
        sb.append("<div style='max-width:480px;margin:40px auto;'>");

        // Header
        sb.append("<div style='background:linear-gradient(135deg,#6366f1,#4f46e5);border-radius:16px 16px 0 0;padding:40px 32px;text-align:center;'>");
        sb.append("<div style='width:56px;height:56px;background:rgba(255,255,255,0.2);border-radius:12px;margin:0 auto 16px;line-height:56px;font-size:24px;'>🔐</div>");
        sb.append("<h1 style='color:white;font-size:22px;margin:0;font-weight:700;'>Verification Code</h1>");
        sb.append("<p style='color:rgba(255,255,255,0.8);font-size:14px;margin:8px 0 0;'>THE 12%CLUB Secure Login</p>");
        sb.append("</div>");

        // Body
        sb.append("<div style='background:white;padding:40px 32px;border-radius:0 0 16px 16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>");
        sb.append("<p style='color:#1d1d1f;font-size:15px;line-height:1.6;margin:0 0 24px;'>Your one-time verification code is:</p>");

        // OTP digits
        sb.append("<div style='text-align:center;margin:0 0 24px;'>");
        for (char c : otp.toCharArray()) {
            sb.append("<span style='display:inline-block;width:44px;height:52px;line-height:52px;");
            sb.append("background:#f0f0f5;border:2px solid #e5e5ea;border-radius:10px;");
            sb.append("font-size:24px;font-weight:700;color:#1d1d1f;margin:0 3px;'>")
              .append(c).append("</span>");
        }
        sb.append("</div>");

        sb.append("<p style='color:#86868b;font-size:13px;line-height:1.5;margin:0 0 16px;text-align:center;'>");
        sb.append("This code expires in <strong>5 minutes</strong>.</p>");

        sb.append("<div style='background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;'>");
        sb.append("<p style='color:#856404;font-size:12px;margin:0;'>⚠️ Never share this code with anyone. Our team will never ask for it.</p>");
        sb.append("</div>");

        sb.append("</div>");

        // Footer
        sb.append("<p style='text-align:center;color:#86868b;font-size:12px;margin:24px 0 0;'>");
        sb.append("If you didn't request this code, please ignore this email.</p>");

        sb.append("</div></body></html>");
        return sb.toString();
    }
}
