package com.example.Product.Service.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private double price;

    private String brand;  
    // @Column(nullable = false, length = 2000)  // Increase from default 255 to 2000
    @Column(nullable = false, columnDefinition = "TEXT")  // Use TEXT type  very long descriptions:
    private String description;

    private Boolean inStock = true;

     // 🔥 NEW: Stock quantity
     private Integer stock = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

     // 🔥 NEW: Multiple images
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }






}