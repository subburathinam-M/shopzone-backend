package com.example.AUTH_SERVICE.controller;

import com.example.AUTH_SERVICE.dto.UpdateProfileRequest;
import com.example.AUTH_SERVICE.dto.UserResponse;
import com.example.AUTH_SERVICE.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile using Keycloak JWT token
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        log.info("Getting profile for Keycloak user: {}", keycloakId);
        
        UserResponse userProfile = userService.getUserProfileByKeycloakId(keycloakId);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Get current user profile using old method (for backward compatibility)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return getProfile(jwt);
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateProfileRequest request) {
        
        String keycloakId = jwt.getSubject();
        UserResponse updatedUser = userService.updateProfileByKeycloakId(keycloakId, request);
        return ResponseEntity.ok(updatedUser);
    }
}