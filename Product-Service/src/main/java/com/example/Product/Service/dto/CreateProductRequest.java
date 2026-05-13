package com.example.Product.Service.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CreateProductRequest {

    private String name;
    private Double price;
    private String brand;
    private String description;
    private Boolean inStock = true;
    private Integer stock = 0;           // 🔥 NEW
    private Long categoryId;  // Client sends category ID
    private Integer primaryImageIndex;  // 🔥 ADD THIS
    private List<MultipartFile> images;   // 🔥 NEW: Multiple image files
}