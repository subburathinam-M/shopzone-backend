package com.example.AUTH_SERVICE.service;

import com.example.AUTH_SERVICE.dto.UpdateProfileRequest;
import com.example.AUTH_SERVICE.dto.UserResponse;

public interface UserService {
    UserResponse getUserProfile(Long userId);
    UserResponse getUserProfileByUsername(String username);
    UserResponse getUserProfileByKeycloakId(String keycloakId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserResponse updateProfileByUsername(String username, UpdateProfileRequest request);
    UserResponse updateProfileByKeycloakId(String keycloakId, UpdateProfileRequest request);
}
