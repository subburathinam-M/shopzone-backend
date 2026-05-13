package com.example.Product.Service.dto;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String name;
    private Double price;
    private String brand;
    private String description;
    private Boolean inStock;
    private Integer stock;        // 🔥 NEW
    private Long categoryId;
}