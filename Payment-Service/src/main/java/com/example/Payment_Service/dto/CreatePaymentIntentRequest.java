package com.example.Payment_Service.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class CreatePaymentIntentRequest {
    private Long orderId;
    private Double amount;
    private String currency = "inr";
    private String paymentMethod = "STRIPE";

    // Add these fields
    private String  userId;
    private String userEmail;  // 👈 ADD THIS
    private String shippingAddress;
    private String phoneNumber;
    private String city;
    private String pincode;
    private String country;

    // 👇 Add this field
    private Map<String, String> metadata = new HashMap<>();
  
}