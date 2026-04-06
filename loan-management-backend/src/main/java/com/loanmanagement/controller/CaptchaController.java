package com.loanmanagement.controller;

import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/captcha")
@Tag(name = "CAPTCHA", description = "CAPTCHA generation and verification")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping
    @Operation(summary = "Generate a new CAPTCHA")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCaptcha() throws IOException {
        Map<String, Object> captcha = captchaService.generateCaptcha();
        return ResponseEntity.ok(ApiResponse.success("CAPTCHA generated", captcha));
    }
}
