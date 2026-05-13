package com.example.Order.Service.dto;
// com.example.Order.Service.dto/PaymentRequest.java


import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String paymentMethod;     // "COD"
    private String shippingAddress;
    private String phoneNumber;
    private String city;
    private String pincode;
}