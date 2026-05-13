package com.example.AUTH_SERVICE.service.serviceImpl;

import com.example.AUTH_SERVICE.dto.*;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.event.UserRegisteredEvent;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.service.AuthService;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Keycloak keycloakAdmin;

    @Value("${spring.keycloak.admin.realm}")
    private String realm;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        long startTime = System.currentTimeMillis();
        log.info("🚀 START Register: {}", request.getEmail());

        String tempKeycloakUserId = null;
        User savedUser = null;

        try {
            // 1️⃣ DB check - username exists?
            if (userRepository.existsByUsername(request.getUsername())) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Username already exists")
                        .build();
            }

            // 2️⃣ DB check - email exists?
            if (userRepository.existsByEmail(request.getEmail())) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Email already exists")
                        .build();
            }

            // 3️⃣ Create Keycloak user
            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(request.getUsername());
            keycloakUser.setEmail(request.getEmail());
            keycloakUser.setFirstName(request.getFirstName());
            keycloakUser.setLastName(request.getLastName());
            keycloakUser.setEnabled(true);
            keycloakUser.setEmailVerified(false);
            keycloakUser.setRequiredActions(List.of("VERIFY_EMAIL"));

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            keycloakUser.setCredentials(List.of(credential));

            Response response = keycloakAdmin.realm(realm)
                    .users()
                    .create(keycloakUser);

            if (response.getStatus() != 201) {
                String error = response.readEntity(String.class);
                response.close();
                return AuthResponse.builder()
                        .success(false)
                        .message("Registration failed: " + error)
                        .build();
            }

            // 4️⃣ Get Keycloak user ID
            tempKeycloakUserId = response.getLocation()
                    .getPath()
                    .substring(response.getLocation().getPath().lastIndexOf("/") + 1);
            response.close();

            log.info("✅ Keycloak user created in {}ms", System.currentTimeMillis() - startTime);

            // 5️⃣ Determine role
            final String role = determineRole(request.getRole());

            // 6️⃣ Save to DB
            User user = User.builder()
                    .keycloakId(tempKeycloakUserId)
                    .username(request.getUsername())
                    .password(passwordEncoder.encode("KEYCLOAK_MANAGED"))
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(role)
                    .isActive(true)
                    .build();

            savedUser = userRepository.save(user);
            log.info("✅ DB saved in {}ms", System.currentTimeMillis() - startTime);

            // 7️⃣ ✅ ASSIGN ROLE (SYNC - NO BACKGROUND!)
            try {
                List<RoleRepresentation> existingRoles = keycloakAdmin.realm(realm)
                        .users()
                        .get(tempKeycloakUserId)
                        .roles()
                        .realmLevel()
                        .listAll();

                boolean roleAlreadyAssigned = existingRoles.stream()
                        .anyMatch(r -> r.getName().equals(role));

                if (!roleAlreadyAssigned) {
                    RoleRepresentation roleRep = keycloakAdmin.realm(realm)
                            .roles()
                            .get(role)
                            .toRepresentation();

                    keycloakAdmin.realm(realm)
                            .users()
                            .get(tempKeycloakUserId)
                            .roles()
                            .realmLevel()
                            .add(List.of(roleRep));

                    log.info("✅ Role '{}' assigned", role);
                } else {
                    log.info("✅ Role already assigned");
                }
            } catch (Exception e) {
                // Role assign failed → ROLLBACK everything
                log.error("❌ Role assign failed: {}", e.getMessage());
                userRepository.delete(savedUser);
                keycloakAdmin.realm(realm).users().get(tempKeycloakUserId).remove();
                throw new RuntimeException("Role assignment failed. Registration cancelled.");
            }

            // 8️⃣ ✅ SEND VERIFY EMAIL (SYNC - NO BACKGROUND!)
            try {
                keycloakAdmin.realm(realm)
                        .users()
                        .get(tempKeycloakUserId)
                        .sendVerifyEmail();
                log.info("✅ Verification email sent");
            } catch (Exception e) {
                // Email send failed → ROLLBACK everything
                log.error("❌ Email send failed: {}", e.getMessage());
                userRepository.delete(savedUser);
                keycloakAdmin.realm(realm).users().get(tempKeycloakUserId).remove();
                throw new RuntimeException("Verification email failed. Registration cancelled.");
            }

            // 9️⃣ Kafka event (non-critical - can fail without rollback)
            try {
                kafkaTemplate.send("user-events", new UserRegisteredEvent(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getUsername(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        LocalDateTime.now()
                ));
                log.info("✅ Kafka event sent");
            } catch (Exception ex) {
                log.error("⚠️ Kafka failed (non-critical): {}", ex.getMessage());
            }

            log.info("🎉 Register SUCCESS in {}ms for: {}", 
                    System.currentTimeMillis() - startTime, request.getEmail());

            // 🔟 Return success ONLY after ALL critical steps succeed
            return AuthResponse.builder()
                    .success(true)
                    .message("Registration successful! Please check your email to verify your account.")
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole())
                    .build();

        } catch (Exception e) {
            log.error("❌ Registration FAILED: {}", e.getMessage(), e);
            
            // Cleanup if Keycloak user was created but something failed
            if (tempKeycloakUserId != null) {
                try {
                    keycloakAdmin.realm(realm)
                            .users()
                            .get(tempKeycloakUserId)
                            .remove();
                    log.info("✅ Keycloak user cleaned up after failure");
                } catch (Exception cleanupError) {
                    log.error("❌ Failed to cleanup Keycloak user: {}", cleanupError.getMessage());
                }
            }
            
            // Also delete DB user if saved (though shouldn't happen)
            if (savedUser != null && savedUser.getId() != null) {
                try {
                    userRepository.delete(savedUser);
                    log.info("✅ DB user cleaned up after failure");
                } catch (Exception dbCleanupError) {
                    log.error("❌ Failed to cleanup DB user: {}", dbCleanupError.getMessage());
                }
            }
            
            return AuthResponse.builder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build();
        }
    }

    private String determineRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) return "USER";
        String role = requestedRole.toUpperCase();
        return (role.equals("ADMIN") || role.equals("USER")) ? role : "USER";
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return AuthResponse.builder()
                .success(false)
                .message("Login handled by Keycloak")
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        return AuthResponse.builder()
                .success(false)
                .message("Token refresh handled by Keycloak")
                .build();
    }

    @Override
    public UserResponse getCurrentUser() {
        throw new UnsupportedOperationException("Use /api/users/me endpoint");
    }

    @Override
    public AuthResponse logout() {
        return AuthResponse.builder()
                .success(true)
                .message("Logout handled by Keycloak")
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        return false;
    }
}