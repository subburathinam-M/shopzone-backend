package com.example.Order.Service.dto;

// com.example.Order.Service.dto/PaymentResponse.java

import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String paymentStatus;
    private String message;
}