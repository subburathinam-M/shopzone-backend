package com.example.Order.Service.dto;

import lombok.Data;

@Data
public class Product {
    private Long id;
    private String name;
    private double price;
    private String description;
    private Integer stock;           // 🔥 NEW
    private boolean fallback  = false; // ADD THIS LINE - New field!

    // Helper method to check if enough stock
    public boolean hasEnoughStock(int quantity) {
        return stock != null && stock >= quantity;
    }
}
