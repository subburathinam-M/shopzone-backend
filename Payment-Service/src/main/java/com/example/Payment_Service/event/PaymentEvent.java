package com.example.Payment_Service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private String userId;
    private String userEmail;
    private Double amount;
    private String status;  // "SUCCESS", "FAILED", "PENDING"
    private String paymentMethod;  // "COD", "ONLINE", "UPI", "CARD"
    private LocalDateTime timestamp;
}