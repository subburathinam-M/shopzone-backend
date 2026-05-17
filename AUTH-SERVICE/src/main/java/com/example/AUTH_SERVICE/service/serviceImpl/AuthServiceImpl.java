package com.example.AUTH_SERVICE.service.serviceImpl;

import com.example.AUTH_SERVICE.Jwtutils.JwtService;
import com.example.AUTH_SERVICE.dto.*;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.security.CustomUserDetails;
import com.example.AUTH_SERVICE.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());

        // 1. Check username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Username already exists")
                    .build();
        }

        // 2. Check email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email already exists")
                    .build();
        }

        // 3. Determine role
        String role = determineRole(request.getRole());

        // 4. Save user to DB with BCrypt password
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User saved to DB: {}", savedUser.getEmail());

        // 5. Generate JWT token
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Registration successful for: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .success(true)
                .message("Registration successful!")
                .token(token)
                .refreshToken(refreshToken)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for: {}", request.getUsername());
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid username or password")
                    .build();
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Login successful for: {}", user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .token(token)
                .refreshToken(refreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtService.isTokenValid(refreshToken)) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Invalid or expired refresh token")
                        .build();
            }

            String username = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CustomUserDetails userDetails = new CustomUserDetails(user);
            String newToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            return AuthResponse.builder()
                    .success(true)
                    .message("Token refreshed")
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch (Exception e) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Token refresh failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }

    @Override
    public UserResponse getCurrentUser() {
        throw new UnsupportedOperationException("Use /api/users/me endpoint");
    }

    @Override
    public AuthResponse logout() {
        // JWT is stateless - client just deletes the token
        return AuthResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    private String determineRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) return "USER";
        String role = requestedRole.toUpperCase();
        return (role.equals("ADMIN") || role.equals("USER")) ? role : "USER";
    }
}
