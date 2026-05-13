package com.example.Product.Service.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.Product.Service.dto.CreateProductRequest;
import com.example.Product.Service.dto.ProductResponse;
import com.example.Product.Service.dto.UpdateProductRequest;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request,List<MultipartFile> images);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
    List<ProductResponse> getProductsByCategory(Long categoryId) ;

      // 🔥 NEW: Stock management
    boolean decrementStock(Long productId, int quantity);
    boolean hasStock(Long productId, int quantity);
    
    // 🔥 NEW: Image management
    ProductResponse addImages(Long productId, List<MultipartFile> images);
    void deleteImage(Long productId, Long imageId);
    void setPrimaryImage(Long productId, Long imageId);

}
