package com.loanmanagement.controller;

import com.loanmanagement.dto.request.LoginRequest;
import com.loanmanagement.dto.request.RegisterRequest;
import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.AuthResponse;
import com.loanmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update own profile")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateProfile(
            @RequestBody java.util.Map<String, String> request) {
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

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("phoneNumber", user.getPhoneNumber());
        result.put("role", user.getRole());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", result));
    }
}
