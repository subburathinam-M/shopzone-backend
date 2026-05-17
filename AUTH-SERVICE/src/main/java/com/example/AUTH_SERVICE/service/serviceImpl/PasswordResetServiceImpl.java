package com.example.AUTH_SERVICE.service.serviceImpl;

import com.example.AUTH_SERVICE.dto.ForgotPasswordRequest;
import com.example.AUTH_SERVICE.dto.PasswordResetResponse;
import com.example.AUTH_SERVICE.dto.ResetPasswordRequest;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.service.EmailService;
import com.example.AUTH_SERVICE.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.reset-password.path}")
    private String resetPasswordPath;

    @Override
    @Transactional
    public PasswordResetResponse generateResetToken(ForgotPasswordRequest request) {
        log.info("Generating reset token for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);

        user.setResetToken(token);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);

        // Send email with reset link
        try {
            String resetLink = frontendUrl + resetPasswordPath + "?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            // Don't fail the request — token is saved, user can retry
        }

        return new PasswordResetResponse(
                "Password reset link sent to your email. Check your inbox (or spam folder).",
                true
        );
    }

    @Override
    public boolean validateResetToken(String token) {
        log.info("Validating reset token");

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        return true;
    }

    @Override
    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password with token");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        // Update password in DB
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());

        return new PasswordResetResponse(
                "Password has been reset successfully. You can now login with your new password.",
                true
        );
    }
}
