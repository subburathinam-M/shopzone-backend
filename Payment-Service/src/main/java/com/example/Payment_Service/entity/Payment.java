package com.example.Payment_Service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long orderId;           // From Order Service
    private String userId;             // From Auth Service
    @Column(name = "user_email")
    private String userEmail;  // 👈 Add this field
    private Double amount;
    private String paymentMethod;    // "COD", "CARD", "UPI"
    private String paymentStatus;    // "PENDING", "PAID", "FAILED"
    
    private String transactionId;     // For future online payments
    private String clientSecret;
    private String paymentDetails;    // JSON string for payment details
    
    // COD specific fields
    private String shippingAddress;
    private String phoneNumber;
    private String city;
    private String pincode;
    private String country;
    
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
