package com.example.AUTH_SERVICE.dto;

// com.example.AUTH_SERVICE.dto.UpdateProfileRequest.java

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String gender;
}