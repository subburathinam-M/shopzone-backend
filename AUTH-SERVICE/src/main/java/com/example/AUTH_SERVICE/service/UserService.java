package com.example.AUTH_SERVICE.service;

import com.example.AUTH_SERVICE.dto.UpdateProfileRequest;
import com.example.AUTH_SERVICE.dto.UserResponse;

public interface UserService {
    UserResponse getUserProfile(Long userId);
    UserResponse getUserProfileByKeycloakId(String keycloakId);  // 👈 NEW
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserResponse updateProfileByKeycloakId(String keycloakId, UpdateProfileRequest request);  // 👈 NEW

}
