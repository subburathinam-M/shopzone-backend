package com.example.Order.Service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long productId;
    private String productName;
    private Double price;

    @Column(nullable = false)
    private Integer quantity = 1;  // 🔥 NEW: Add quantity field
    
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED, FAILED
    
    private String notes;

    // 👇 NEW: User details - WHO ORDERED?
    // 👇 NEW: User details from Keycloak
    private String userKeycloakId;      // Keycloak user ID (sub claim)
    private String userEmail;            // User's email
    private String userName;             // User's username


     // 🔥 NEW: Payment related fields
     private String paymentMethod;     // "COD", "CARD", "UPI"
     private String paymentStatus;     // "PENDING", "PAID", "FAILED"
     private Long paymentId;           // Reference to payment service
     private String shippingAddress;
     private String phoneNumber;

     @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
     private boolean deleted = false;
    
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}