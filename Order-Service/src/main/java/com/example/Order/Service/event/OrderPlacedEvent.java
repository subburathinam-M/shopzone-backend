package com.example.Order.Service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long productId;
    private int quantity;
    private Double totalAmount;
    private String paymentMethod;
    private LocalDateTime timestamp;
}