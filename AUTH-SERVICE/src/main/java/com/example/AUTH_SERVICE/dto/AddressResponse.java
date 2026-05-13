package com.example.AUTH_SERVICE.dto;

// AddressResponse.java


// com.example.AUTH_SERVICE.dto.AddressResponse.java


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AddressResponse {
    private Long id;
    private String addressType;
    private String name;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}