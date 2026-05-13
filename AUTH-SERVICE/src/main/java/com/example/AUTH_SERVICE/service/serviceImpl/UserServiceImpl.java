package com.example.AUTH_SERVICE.service.serviceImpl;

import com.example.AUTH_SERVICE.dto.UpdateProfileRequest;
import com.example.AUTH_SERVICE.dto.UserResponse;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserProfileByKeycloakId(String keycloakId) {
        log.info("Fetching user profile by Keycloak ID: {}", keycloakId);
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with Keycloak ID: " + keycloakId));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        updateUserFields(user, request);
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user ID: {}", userId);
        
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateProfileByKeycloakId(String keycloakId, UpdateProfileRequest request) {
        log.info("Updating profile for Keycloak ID: {}", keycloakId);
        
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with Keycloak ID: " + keycloakId));
        
        updateUserFields(user, request);
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated for Keycloak ID: {}", keycloakId);
        
        return mapToUserResponse(updatedUser);
    }

    private void updateUserFields(User user, UpdateProfileRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}