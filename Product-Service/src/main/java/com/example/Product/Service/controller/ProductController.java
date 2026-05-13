package com.example.Product.Service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.Product.Service.dto.CreateProductRequest;
import com.example.Product.Service.dto.ProductResponse;
import com.example.Product.Service.dto.UpdateProductRequest;
import com.example.Product.Service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/products")
// @RequiredArgsConstructor
public class ProductController {

    private ProductService productService;
     private final ObjectMapper objectMapper;

    public ProductController(ProductService productService,  ObjectMapper objectMapper){
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ProductResponse> createProduct(
            // @RequestBody CreateProductRequest request
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
        ) {
            // ProductResponse response = productService.createProduct(request);
            // return new ResponseEntity<>(response, HttpStatus.CREATED);
            try {
                CreateProductRequest request = objectMapper.readValue(productJson, CreateProductRequest.class);
                log.info("Creating product with primaryImageIndex: {}", request.getPrimaryImageIndex());
                ProductResponse response = productService.createProduct(request, images);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } catch (Exception e) {
                throw new RuntimeException("Invalid product data: " + e.getMessage());
            }
        }
    
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts();
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId) {
        List<ProductResponse> responses = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(responses);
    }

    

    // 🔥 NEW: Stock check endpoint
    @GetMapping("/{id}/stock")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(productService.hasStock(id, quantity));
    }
    
    // 🔥 NEW: Add images to existing product
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> addImages(
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images) {
        ProductResponse response = productService.addImages(id, images);
        return ResponseEntity.ok(response);
    }
    
    // 🔥 NEW: Delete image
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
    
    // 🔥 NEW: Set primary image
    @PutMapping("/{productId}/images/{imageId}/primary")
    public ResponseEntity<Void> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productService.setPrimaryImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    
    // 🔥 NEW: Decrement stock endpoint
    @PutMapping("/{id}/decrement-stock")
    public ResponseEntity<Void> decrementStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        
        log.info("Decrementing stock for product ID: {} by quantity: {}", id, quantity);
        
        boolean success = productService.decrementStock(id, quantity);
        
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


//     @GetMapping
//     public List<Product> getProducts() {
//         return productService.getProducts();
//     }
    

// @GetMapping("/{id}")
// public ProductResponse getProduct(@PathVariable Long id) {
//     return productService.getByProduct(id);
// }

//     @PostMapping
//     public Product save(@RequestBody Product p) {
//         return productService.addProduct(p);
//     }


}
