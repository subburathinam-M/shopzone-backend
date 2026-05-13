package com.example.AUTH_SERVICE.service;

import com.example.AUTH_SERVICE.dto.AuthResponse;
import com.example.AUTH_SERVICE.dto.LoginRequest;
import com.example.AUTH_SERVICE.dto.RegisterRequest;
import com.example.AUTH_SERVICE.dto.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    public AuthResponse refreshToken(String refreshToken);
    boolean validateToken(String token);
    UserResponse getCurrentUser();
    AuthResponse logout();
}
