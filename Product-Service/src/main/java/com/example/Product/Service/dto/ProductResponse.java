package com.example.Product.Service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private double price;
     private String brand;
    private String description;
    private Boolean inStock;
    private Integer stock;                 // 🔥 NEW
    private CategoryDTO category;  // Will contain category details
    // private List<String> imageUrls;        // 🔥 NEW: List of image URLs
    private List<ImageDto> images;  // 🔥 NEW: Replace imageUrls with images list
    private LocalDateTime createdAt;
    
    // public ProductResponse(Long id, String name, double price) {
    //     this.id = id;
    //     this.name = name;
    //     this.price = price;
    // }
}