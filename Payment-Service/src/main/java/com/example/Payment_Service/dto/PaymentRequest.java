package com.example.Payment_Service.dto;

// PaymentRequest.java


import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private String userId;
    private Double amount;
    private String paymentMethod;     // "COD"
    
    // COD fields
    private String shippingAddress;
    private String phoneNumber;
    private String city;
    private String pincode;
    private String country;
}
