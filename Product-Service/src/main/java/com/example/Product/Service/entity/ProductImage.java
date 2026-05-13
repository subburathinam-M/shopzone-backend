package com.example.Product.Service.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private String imageUrl;      // Path to stored image
    
    private String fileName;       // Original file name
    
    private String fileType;       // image/jpeg, image/png
    
    private Long fileSize;         // Size in bytes
    
    private Boolean isPrimary = false;  // Main display image
    
    private Integer displayOrder;  // For sorting images
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}