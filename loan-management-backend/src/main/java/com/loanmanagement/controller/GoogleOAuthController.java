package com.loanmanagement.controller;

import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.dto.response.AuthResponse;
import com.loanmanagement.service.GoogleOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/google")
@Tag(name = "Google OAuth", description = "Google OAuth2 authentication endpoints")
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;

    public GoogleOAuthController(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping
    @Operation(summary = "Authenticate with Google ID token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Google ID token is required"));
            }

            AuthResponse response = googleOAuthService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(ApiResponse.success("Google authentication successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Google authentication failed: " + e.getMessage()));
        }
    }
}
