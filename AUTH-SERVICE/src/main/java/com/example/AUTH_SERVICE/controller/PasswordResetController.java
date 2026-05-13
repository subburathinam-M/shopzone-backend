package com.example.AUTH_SERVICE.controller;

// com.example.Auth.Service.controller.PasswordResetController.java



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.AUTH_SERVICE.dto.ForgotPasswordRequest;
import com.example.AUTH_SERVICE.dto.PasswordResetResponse;
import com.example.AUTH_SERVICE.dto.ResetPasswordRequest;
import com.example.AUTH_SERVICE.service.PasswordResetService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // Step 1: Request password reset (send email)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            PasswordResetResponse response = passwordResetService.generateResetToken(request);
            return ResponseEntity.ok(response);  // ✅ This returns JSON
        } catch (Exception e) {
            log.error("Error in forgot password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new PasswordResetResponse(e.getMessage(), false)
            );
        }
    }

    // Step 2: Validate token (called when user clicks email link)
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        return ResponseEntity.ok(new PasswordResetResponse("Token is valid", isValid));
    }

    // Step 3: Reset password with token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetResponse response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}