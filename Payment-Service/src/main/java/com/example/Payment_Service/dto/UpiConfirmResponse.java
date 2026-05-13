package com.example.Payment_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiConfirmResponse {
    private String status;          // "SUCCESS", "FAILED", "PENDING"
    private String message;
    private String transactionId;
    private Long orderId;
    private Double amount;
    private String upiId;
    private String upiApp;
    private String referenceId;      // UPI reference number
    private LocalDateTime paymentTime;
}