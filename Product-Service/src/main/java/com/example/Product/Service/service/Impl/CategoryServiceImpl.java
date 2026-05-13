package com.example.Product.Service.service.Impl;

import com.example.Product.Service.Repository.CategoryRepository;
import com.example.Product.Service.dto.CategoryDTO;
import com.example.Product.Service.dto.CreateCategoryRequest;
import com.example.Product.Service.dto.UpdateCategoryRequest;
import com.example.Product.Service.entity.Category;
import com.example.Product.Service.exception.ResourceAlreadyExistsException;
import com.example.Product.Service.exception.ResourceNotFoundException;
import com.example.Product.Service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Override
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());
        
        // Check if category with same name already exists
        if (categoryRepository.findByNameAndIsDeletedFalse(request.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Category with name '" + request.getName() + "' already exists"
            );
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsDeleted(false);
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created with ID: {}", savedCategory.getId());
        
        return mapToDTO(savedCategory);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + id
            ));
        
        return mapToDTO(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching all active categories");
        
        return categoryRepository.findByIsDeletedFalse()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + id
            ));
        
        // Check if new name conflicts with existing category
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            categoryRepository.findByNameAndIsDeletedFalse(request.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException(
                            "Category with name '" + request.getName() + "' already exists"
                        );
                    }
                });
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated with ID: {}", id);
        
        return mapToDTO(updatedCategory);
    }
    
    @Override
    public void deleteCategory(Long id) {
        log.info("Soft deleting category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + id
            ));
        
        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete category with existing products. Remove products first."
            );
        }
        
        // Soft delete
        category.setIsDeleted(true);
        categoryRepository.save(category);
        
        log.info("Category soft deleted with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByName(String name) {
        log.info("Searching categories by name: {}", name);
        
        return categoryRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        
        return dto;
    }
}