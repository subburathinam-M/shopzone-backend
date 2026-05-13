package com.example.Product.Service.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Product.Service.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find active category by ID
    Optional<Category> findByIdAndIsDeletedFalse(Long id);
      // Find all active categories
    List<Category> findByIsDeletedFalse();
    
    // Find category by name (active only)
    Optional<Category> findByNameAndIsDeletedFalse(String name);
    
    // Search categories by name (active only)
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.isDeleted = false")
    List<Category> findByNameContainingIgnoreCaseAndIsDeletedFalse(@Param("name") String name);
    
    // Find categories with product count
    @Query("SELECT c, COUNT(p) as productCount FROM Category c LEFT JOIN c.products p WHERE c.isDeleted = false GROUP BY c")
    List<Object[]> findAllCategoriesWithProductCount();

    
    Optional<Category> findByName(String name);
}