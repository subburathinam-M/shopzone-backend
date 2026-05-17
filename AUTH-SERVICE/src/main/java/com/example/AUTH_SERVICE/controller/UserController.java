package com.example.AUTH_SERVICE.controller;

import com.example.AUTH_SERVICE.Jwtutils.JwtService;
import com.example.AUTH_SERVICE.dto.UpdateProfileRequest;
import com.example.AUTH_SERVICE.dto.UserResponse;
import com.example.AUTH_SERVICE.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Getting profile for user: {}", username);
        UserResponse userProfile = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return getProfile(userDetails);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        String username = userDetails.getUsername();
        UserResponse updatedUser = userService.updateProfileByUsername(username, request);
        return ResponseEntity.ok(updatedUser);
    }
}
