package com.example.Product.Service.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {

    private Long id;
    private String name;
    private String description;
    private Integer productCount;  // Number of products in this category
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
