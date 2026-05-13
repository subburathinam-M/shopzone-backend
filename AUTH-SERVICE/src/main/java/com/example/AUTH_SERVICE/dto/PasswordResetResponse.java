package com.example.AUTH_SERVICE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetResponse {

    private String message;
    private boolean success;

}
