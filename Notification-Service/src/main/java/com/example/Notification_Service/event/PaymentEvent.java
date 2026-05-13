package com.example.Notification_Service.event;

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
    private Long userId;
    private String userEmail;        // 👈 ADD THIS
    private Double amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime timestamp;
}