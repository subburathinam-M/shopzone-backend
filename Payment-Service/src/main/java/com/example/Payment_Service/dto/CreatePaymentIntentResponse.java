package com.example.Payment_Service.dto;

import lombok.Data;

@Data
public class CreatePaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private Long orderId;
    private Double amount;
    private String status;
}