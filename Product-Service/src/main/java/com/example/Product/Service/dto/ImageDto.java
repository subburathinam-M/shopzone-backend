package com.example.Product.Service.dto;


import lombok.Data;

@Data
public class ImageDto {
    private Long id;
    private String imageUrl;
    private String fileName;
    private Boolean isPrimary;
    private Integer displayOrder;
}

