package com.example.AUTH_SERVICE.service.serviceImpl;

import com.example.AUTH_SERVICE.dto.ForgotPasswordRequest;
import com.example.AUTH_SERVICE.dto.PasswordResetResponse;
import com.example.AUTH_SERVICE.dto.ResetPasswordRequest;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.event.PasswordResetEvent;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.service.EmailService;
import com.example.AUTH_SERVICE.service.PasswordResetService;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Keycloak keycloakAdmin;  // ✅ ADD - Keycloak inject pannuvom

    @org.springframework.beans.factory.annotation.Value("${spring.keycloak.admin.realm}")
    private String realm;

    // Step 1: Generate reset token and send email
    @Transactional
    public PasswordResetResponse generateResetToken(ForgotPasswordRequest request) {
        log.info("Generating reset token for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Generate unique token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);

        user.setResetToken(token);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);

        // Publish Kafka event
        // PasswordResetEvent event = new PasswordResetEvent(
        //     user.getId(),
        //     user.getEmail(),
        //     user.getUsername(),
        //     token,
        //     LocalDateTime.now()
        // );
        // kafkaTemplate.send("password-reset-events", event);
        // log.info("📤 Published PasswordResetEvent for user: {}", user.getEmail());

        return new PasswordResetResponse(
            "Password reset link sent to your email. Check your inbox (or spam folder).",
            true
        );
    }

    // Step 2: Validate token
    public boolean validateResetToken(String token) {
        log.info("Validating reset token: {}", token);

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        return true;
    }

    // Step 3: Reset password — LOCAL DB + KEYCLOAK both update
    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password with token");

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Check token expiry
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        // ✅ Step A: Update password in KEYCLOAK
        updateKeycloakPassword(user.getKeycloakId(), request.getNewPassword());

        // ✅ Step B: Update password in LOCAL DB
        // user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPassword(passwordEncoder.encode("KEYCLOAK_MANAGED")); 
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("✅ Password reset successful for user: {}", user.getEmail());

        return new PasswordResetResponse(
            "Password has been reset successfully. You can now login with your new password.",
            true
        );
    }

    // ✅ Keycloak password update helper method
    private void updateKeycloakPassword(String keycloakUserId, String newPassword) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false); // false = user must NOT change again on next login

            keycloakAdmin.realm(realm)
                    .users()
                    .get(keycloakUserId)
                    .resetPassword(credential);

            log.info("✅ Keycloak password updated for user: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("❌ Keycloak password update failed: {}", e.getMessage());
            throw new RuntimeException("Failed to update password in Keycloak: " + e.getMessage());
        }
    }
}