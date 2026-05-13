package com.example.AUTH_SERVICE.dto;

// AddressRequest.java
// com.example.AUTH_SERVICE.dto.AddressRequest.java


import lombok.Data;

@Data
public class AddressRequest {
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
}
