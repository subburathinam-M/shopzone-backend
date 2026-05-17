package com.example.AUTH_SERVICE.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String username, String resetLink);
}
