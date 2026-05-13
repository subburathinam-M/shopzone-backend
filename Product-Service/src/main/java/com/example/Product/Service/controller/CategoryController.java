package com.example.Product.Service.controller;



import com.example.Product.Service.dto.CategoryDTO;
import com.example.Product.Service.dto.CreateCategoryRequest;
import com.example.Product.Service.dto.UpdateCategoryRequest;
import com.example.Product.Service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryDTO response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> responses = categoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request) {
        CategoryDTO response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategoriesByName(
            @RequestParam String name) {
        List<CategoryDTO> responses = categoryService.getCategoriesByName(name);
        return ResponseEntity.ok(responses);
    }
}