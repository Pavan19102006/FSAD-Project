package com.loanmanagement.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 80;
    private static final long EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    private static final Random random = new Random();

    // Store: captchaId -> { answer, createdAt }
    private final ConcurrentHashMap<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    private static class CaptchaEntry {
        final String answer;
        final long createdAt;

        CaptchaEntry(String answer) {
            this.answer = answer;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > EXPIRY_MS;
        }
    }

    public Map<String, Object> generateCaptcha() throws IOException {
        // Clean expired entries
        captchaStore.entrySet().removeIf(e -> e.getValue().isExpired());

        // Generate math problem
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(10) + 1;
        String[] ops = {"+", "-", "×"};
        String op = ops[random.nextInt(ops.length)];

        int answer;
        switch (op) {
            case "+": answer = a + b; break;
            case "-": answer = a - b; break;
            case "×": answer = a * b; break;
            default: answer = a + b;
        }

        String text = a + " " + op + " " + b + " = ?";
        String captchaId = UUID.randomUUID().toString();

        captchaStore.put(captchaId, new CaptchaEntry(String.valueOf(answer)));

        // Generate image
        byte[] imageBytes = renderCaptchaImage(text);

        return Map.of(
                "captchaId", captchaId,
                "image", java.util.Base64.getEncoder().encodeToString(imageBytes)
        );
    }

    public boolean verifyCaptcha(String captchaId, String userAnswer) {
        if (captchaId == null || userAnswer == null) return false;

        CaptchaEntry entry = captchaStore.remove(captchaId);
        if (entry == null || entry.isExpired()) return false;

        return entry.answer.trim().equals(userAnswer.trim());
    }

    private byte[] renderCaptchaImage(String text) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Background gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(30, 30, 50),
                WIDTH, HEIGHT, new Color(50, 40, 70)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw noise dots
        for (int i = 0; i < 150; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g2d.setColor(new Color(
                    random.nextInt(100) + 50,
                    random.nextInt(100) + 50,
                    random.nextInt(100) + 100,
                    random.nextInt(100) + 50
            ));
            g2d.fillOval(x, y, 2, 2);
        }

        // Draw noise lines
        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(
                    random.nextInt(100) + 80,
                    random.nextInt(100) + 80,
                    random.nextInt(150) + 100,
                    random.nextInt(80) + 40
            ));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(
                    random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT)
            );
        }

        // Draw text with distortion
        Font font = new Font("Monospaced", Font.BOLD, 30);
        g2d.setFont(font);

        int startX = 20;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            AffineTransform orig = g2d.getTransform();
            double angle = (random.nextDouble() - 0.5) * 0.3;
            int yOffset = random.nextInt(10) - 5;

            g2d.translate(startX, 50 + yOffset);
            g2d.rotate(angle);

            // Glow effect
            g2d.setColor(new Color(99, 102, 241, 60));
            g2d.drawString(String.valueOf(c), 1, 1);

            // Main text
            GradientPaint textGradient = new GradientPaint(
                    0, -15, new Color(180, 180, 255),
                    0, 15, new Color(255, 255, 255)
            );
            g2d.setPaint(textGradient);
            g2d.drawString(String.valueOf(c), 0, 0);

            g2d.setTransform(orig);
            startX += g2d.getFontMetrics().charWidth(c) + 2;
        }

        // Draw bezier curves for extra distortion
        g2d.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i < 3; i++) {
            g2d.setColor(new Color(
                    random.nextInt(100) + 100,
                    random.nextInt(100) + 100,
                    random.nextInt(100) + 155,
                    random.nextInt(60) + 30
            ));
            g2d.drawArc(
                    random.nextInt(WIDTH / 2), random.nextInt(HEIGHT / 2),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(360), random.nextInt(360)
            );
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
