package com.example.Product.Service.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Product.Service.entity.Product;


@Repository
public interface ProductRepository extends JpaRepository<Product,Long>{

      // Find active (non-deleted) product by ID
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
    
    // Find all active products
    List<Product> findByIsDeletedFalse();
    
    // Find products by category (only active)
    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId);

    // Check if product with name exists (for duplicate prevention)

    // Optional<Product> findByNameAndIsDeletedFalse(String name);
    
    // Search products by name (only active)
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isDeleted = false")
    List<Product> searchByName(@Param("name") String name);

     // 🔥 NEW: Decrement stock
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    // 🔥 NEW: Check stock availability
    @Query("SELECT CASE WHEN p.stock >= :quantity THEN true ELSE false END FROM Product p WHERE p.id = :id")
    boolean hasStock(@Param("id") Long id, @Param("quantity") int quantity);

}
