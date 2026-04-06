package com.loanmanagement.controller;

import com.loanmanagement.dto.request.LoginRequest;
import com.loanmanagement.dto.request.RegisterRequest;
import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.AuthResponse;
import com.loanmanagement.service.CaptchaService;
import com.loanmanagement.service.EmailService;
import com.loanmanagement.service.OtpService;
import com.loanmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final CaptchaService captchaService;
    private final OtpService otpService;
    private final EmailService emailService;

    // Temporary storage for pre-authenticated users awaiting OTP
    private final java.util.concurrent.ConcurrentHashMap<String, AuthResponse> pendingOtpAuth = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthController(UserService userService, CaptchaService captchaService,
                          OtpService otpService, EmailService emailService) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    /**
     * REGISTER: Creates user, sends OTP for email verification.
     * User must verify OTP before getting JWT.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user - requires OTP verification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = userService.register(request);

        // Generate OTP for email verification
        String otp = otpService.generateOtp(authResponse.getEmail());
        emailService.sendOtpEmail(authResponse.getEmail(), otp);

        // Store auth response for retrieval after OTP verification
        pendingOtpAuth.put(authResponse.getEmail(), authResponse);

        String maskedEmail = maskEmail(authResponse.getEmail());

        Map<String, Object> result = new HashMap<>();
        result.put("otpRequired", true);
        result.put("email", authResponse.getEmail());
        result.put("maskedEmail", maskedEmail);
        result.put("message", "Verify your email to complete registration. OTP sent to " + maskedEmail);
        result.put("isRegistration", true);

        return ResponseEntity.ok(ApiResponse.success("Registration initiated. Please verify OTP.", result));
    }

    /**
     * LOGIN WITH PASSWORD: Validates credentials + CAPTCHA, grants direct access.
     * No OTP required for password login.
     */
    @PostMapping("/login")
    @Operation(summary = "Login with password + CAPTCHA")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Validate CAPTCHA
        if (request.getCaptchaId() == null || request.getCaptchaAnswer() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CAPTCHA verification is required"));
        }

        if (!captchaService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaAnswer())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid CAPTCHA. Please try again."));
        }

        // Validate credentials and return JWT directly
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * SEND OTP FOR LOGIN: Send an OTP to the user's email for passwordless login.
     */
    @PostMapping("/send-login-otp")
    @Operation(summary = "Send OTP for passwordless login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendLoginOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
        }

        // Verify user exists
        try {
            com.loanmanagement.entity.User user = userService.findByEmail(email);

            String otp = otpService.generateOtp(email);
            if (otp == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Too many OTP requests. Please try again later."));
            }

            emailService.sendOtpEmail(email, otp);

            // Pre-generate auth response
            AuthResponse authResponse = userService.buildAuthResponseForUser(user);
            pendingOtpAuth.put(email, authResponse);

            String maskedEmail = maskEmail(email);

            Map<String, Object> result = new HashMap<>();
            result.put("otpSent", true);
            result.put("email", email);
            result.put("maskedEmail", maskedEmail);
            result.put("message", "OTP sent to " + maskedEmail);

            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No account found with this email address"));
        }
    }

    /**
     * VERIFY OTP: Used for both registration verification and OTP-based login.
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and complete login/registration")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email and OTP are required"));
        }

        OtpService.VerificationResult result = otpService.verifyOtp(email, otp);

        if (!result.success()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.message()));
        }

        // OTP verified — return the stored auth response
        AuthResponse authResponse = pendingOtpAuth.remove(email);
        if (authResponse == null) {
            // Re-generate tokens if expired from pending store
            com.loanmanagement.entity.User user = userService.findByEmail(email);
            authResponse = userService.buildAuthResponseForUser(user);
        }

        return ResponseEntity.ok(ApiResponse.success("Verification successful", authResponse));
    }

    /**
     * RESEND OTP: Resend OTP to email.
     */
    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP to email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
        }

        String otp = otpService.generateOtp(email);
        if (otp == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Maximum resend attempts reached. Please try again later."));
        }

        emailService.sendOtpEmail(email, otp);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("message", "OTP resent to " + maskEmail(email));
        resultMap.put("maskedEmail", maskEmail(email));

        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully", resultMap));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update own profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestBody Map<String, String> request) {
        com.loanmanagement.entity.User user = userService.getCurrentUser();

        if (request.get("firstName") != null) {
            user.setFirstName(request.get("firstName"));
        }
        if (request.get("lastName") != null) {
            user.setLastName(request.get("lastName"));
        }
        if (request.get("phoneNumber") != null) {
            user.setPhoneNumber(request.get("phoneNumber"));
        }
        if (request.get("email") != null && !request.get("email").equals(user.getEmail())) {
            user.setEmail(request.get("email"));
        }

        userService.saveUser(user);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", user.getId());
        resultMap.put("email", user.getEmail());
        resultMap.put("firstName", user.getFirstName());
        resultMap.put("lastName", user.getLastName());
        resultMap.put("phoneNumber", user.getPhoneNumber());
        resultMap.put("role", user.getRole());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", resultMap));
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.charAt(0) + "***" + email.substring(atIndex - 1);
    }
}
