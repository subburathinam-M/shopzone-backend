package com.example.Product.Service.service;

import java.util.List;

import com.example.Product.Service.dto.CategoryDTO;
import com.example.Product.Service.dto.CreateCategoryRequest;
import com.example.Product.Service.dto.UpdateCategoryRequest;

public interface CategoryService {

     CategoryDTO createCategory(CreateCategoryRequest request);
    
    CategoryDTO getCategoryById(Long id);
    
    List<CategoryDTO> getAllCategories();
    
    CategoryDTO updateCategory(Long id, UpdateCategoryRequest request);
    
    void deleteCategory(Long id);
    
    List<CategoryDTO> getCategoriesByName(String name);

}
