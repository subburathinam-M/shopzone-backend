package com.example.AUTH_SERVICE.service;

import com.example.AUTH_SERVICE.dto.ForgotPasswordRequest;
import com.example.AUTH_SERVICE.dto.PasswordResetResponse;
import com.example.AUTH_SERVICE.dto.ResetPasswordRequest;

public interface PasswordResetService {
    public PasswordResetResponse generateResetToken(ForgotPasswordRequest request);
    public boolean validateResetToken(String token) ;
    public PasswordResetResponse resetPassword(ResetPasswordRequest request);
}
