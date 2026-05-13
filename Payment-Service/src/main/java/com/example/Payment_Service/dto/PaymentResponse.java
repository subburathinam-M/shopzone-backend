package com.example.Payment_Service.dto;

// PaymentResponse.java


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
    private String message;
    private LocalDateTime createdAt;
}
