package com.example.Product.Service.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.Product.Service.Repository.CategoryRepository;
import com.example.Product.Service.Repository.ProductImageRepository;
import com.example.Product.Service.Repository.ProductRepository;
import com.example.Product.Service.dto.CategoryDTO;
import com.example.Product.Service.dto.CreateProductRequest;
import com.example.Product.Service.dto.ImageDto;
import com.example.Product.Service.dto.ProductResponse;
import com.example.Product.Service.dto.UpdateProductRequest;
import com.example.Product.Service.entity.Category;  // Your Category
import com.example.Product.Service.entity.Product;  // Your Product
import com.example.Product.Service.entity.ProductImage;
import com.example.Product.Service.exception.ResourceAlreadyExistsException;
import com.example.Product.Service.exception.ResourceNotFoundException;
import com.example.Product.Service.service.ImageStorageService;
import com.example.Product.Service.service.ProductService;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
      private final ProductImageRepository imageRepository;
    private final ImageStorageService imageStorageService;


    public ProductServiceImpl(ProductRepository productRepository,CategoryRepository categoryRepository,
        ProductImageRepository imageRepository,
        ImageStorageService imageStorageService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) {
        log.info("Creating product: {} with {} images", request.getName(), 
                 images != null ? images.size() : 0);
        

         // Validate category exists and is not deleted
        Category category = categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Category not found with id: " + request.getCategoryId()
        ));

         // create product

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setInStock(request.getInStock() != null ? request.getInStock() : true);
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        // product.setInStock(request.getInStock());
        product.setCategory(category);
        product.setIsDeleted(false);

        Product  savedProduct = productRepository.save(product);
        // Save images if any
        // if (images != null && !images.isEmpty()) {
        //     saveImages(savedProduct, images);
        // }

          // Save images if any - WITH PRIMARY INDEX
    if (images != null && !images.isEmpty()) {
        saveImagesWithPrimary(savedProduct, images, request.getPrimaryImageIndex());
    }

        log.info("Product created with ID: {}, stock: {}", savedProduct.getId(), savedProduct.getStock());
        return mapToResponse(savedProduct);

    }

    // 🔥 NEW: Save images with primary index
private void saveImagesWithPrimary(Product product, List<MultipartFile> imageFiles, Integer primaryIndex) {
    if (primaryIndex == null) primaryIndex = 0; // Default to first image
    
    List<ProductImage> images = new ArrayList<>();
    
    for (int i = 0; i < imageFiles.size(); i++) {
        MultipartFile file = imageFiles.get(i);
        try {
            String imageUrl = imageStorageService.storeImage(file, product.getId());
            
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imageUrl);
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setFileSize(file.getSize());
            image.setDisplayOrder(i);
            
            // 🔥 Set primary flag based on index from frontend
            image.setIsPrimary(i == primaryIndex);
            
            images.add(image);
        } catch (IOException e) {
            log.error("Failed to save image: {}", e.getMessage());
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }
    
    imageRepository.saveAll(images);
    product.setImages(images);
}


    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));
        
        return mapToResponse(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active products");
        
        // Only fetch non-deleted products
        return productRepository.findByIsDeletedFalse()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));
        
        // Update fields if provided
        if (request.getName() != null) product.setName(request.getName());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getInStock() != null) product.setInStock(request.getInStock());
        if (request.getStock() != null) product.setStock(request.getStock());  // 🔥 Update stock
        
        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + request.getCategoryId()
                ));
            product.setCategory(category);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with ID: {}", id);
        
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Soft deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));
        
        // Soft delete - just mark as deleted
        product.setIsDeleted(true);
        productRepository.save(product);
        
        log.info("Product soft deleted with ID: {}", id);
    }
    
    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category ID: {}", categoryId);
        
        // Verify category exists and is not deleted
        categoryRepository.findByIdAndIsDeletedFalse(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + categoryId
            ));
        
        return productRepository.findByCategoryIdAndIsDeletedFalse(categoryId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }



     // 🔥 NEW: Decrement stock implementation
     @Override
     @Transactional
     public boolean decrementStock(Long productId, int quantity) {
         log.info("Decrementing stock for product {} by {}", productId, quantity);
         
         Product product = productRepository.findByIdAndIsDeletedFalse(productId)
             .orElseThrow(() -> new ResourceNotFoundException(
                 "Product not found with id: " + productId));
         
         if (product.getStock() < quantity) {
             log.error("Insufficient stock. Available: {}, Requested: {}", 
                      product.getStock(), quantity);
             return false;
         }
         
         product.setStock(product.getStock() - quantity);
         productRepository.save(product);
         
         log.info("Stock decremented successfully. New stock: {}", product.getStock());
         return true;
     }
 
     @Override
     public boolean hasStock(Long productId, int quantity) {
         Product product = productRepository.findByIdAndIsDeletedFalse(productId)
             .orElseThrow(() -> new ResourceNotFoundException(
                 "Product not found with id: " + productId));
         
         return product.getStock() >= quantity;
     }
 
     // 🔥 NEW: Image management methods
     @Override
     @Transactional
     public ProductResponse addImages(Long productId, List<MultipartFile> images) {
         Product product = productRepository.findByIdAndIsDeletedFalse(productId)
             .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
         
         int currentImageCount = product.getImages().size();
         List<ProductImage> newImages = new ArrayList<>();
         
         for (MultipartFile file : images) {
             try {
                 String imageUrl = imageStorageService.storeImage(file, productId);
                 
                 ProductImage image = new ProductImage();
                 image.setProduct(product);
                 image.setImageUrl(imageUrl);
                 image.setFileName(file.getOriginalFilename());
                 image.setFileType(file.getContentType());
                 image.setFileSize(file.getSize());
                 image.setDisplayOrder(currentImageCount++);
                 image.setIsPrimary(product.getImages().isEmpty() && currentImageCount == 1);
                 
                 newImages.add(image);
             } catch (IOException e) {
                 throw new RuntimeException("Failed to save image: " + e.getMessage());
             }
         }
         
         imageRepository.saveAll(newImages);
         product.getImages().addAll(newImages);
         
         return mapToResponse(product);
     }
 
     @Override
     @Transactional
     public void deleteImage(Long productId, Long imageId) {
         ProductImage image = imageRepository.findById(imageId)
             .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
         
         if (!image.getProduct().getId().equals(productId)) {
             throw new RuntimeException("Image does not belong to product");
         }
         
         imageStorageService.deleteImage(image.getImageUrl());
         imageRepository.delete(image);
     }
 
     @Override
     @Transactional
     public void setPrimaryImage(Long productId, Long imageId) {
         log.info("Setting primary image for product {} to image {}", productId, imageId);
         
         Product product = productRepository.findById(productId)
             .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
         
         List<ProductImage> images = product.getImages();
         
         // Find the selected image
         ProductImage selectedImage = images.stream()
             .filter(img -> img.getId().equals(imageId))
             .findFirst()
             .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
         
         // Reset all images to non-primary
         images.forEach(img -> img.setIsPrimary(false));
         
         // Set selected image as primary
         selectedImage.setIsPrimary(true);
         
         // 🔥 CRITICAL: Reorder display_order
         int currentOrder = 0;
         for (ProductImage img : images) {
             img.setDisplayOrder(currentOrder++);
         }
         
         // Save all changes
         imageRepository.saveAll(images);
         
         log.info("Primary image set successfully. Image {} is now primary with order 0", imageId);
     }
    
   private ProductResponse mapToResponse(Product product) {
    ProductResponse response = new ProductResponse();
    response.setId(product.getId());
    response.setName(product.getName());
    response.setPrice(product.getPrice());
    response.setBrand(product.getBrand());
    response.setDescription(product.getDescription());
    response.setInStock(product.getInStock());
    response.setStock(product.getStock());
    response.setCreatedAt(product.getCreateAt());
    
    // Map category
    if (product.getCategory() != null) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(product.getCategory().getId());
        categoryDTO.setName(product.getCategory().getName());
        categoryDTO.setDescription(product.getCategory().getDescription());
        response.setCategory(categoryDTO);
    }

    // 🔥 FIX: Return ImageDto objects instead of just URLs
    if (product.getImages() != null && !product.getImages().isEmpty()) {
        List<ImageDto> imageDtos = product.getImages().stream()
            .sorted((img1, img2) -> {
                if (img1.getIsPrimary() && !img2.getIsPrimary()) return -1;
                if (!img1.getIsPrimary() && img2.getIsPrimary()) return 1;
                return Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder());
            })
            .map(img -> {
                ImageDto dto = new ImageDto();
                dto.setId(img.getId());
                dto.setImageUrl(img.getImageUrl());
                dto.setFileName(img.getFileName());
                dto.setIsPrimary(img.getIsPrimary());
                dto.setDisplayOrder(img.getDisplayOrder());
                return dto;
            })
            .collect(Collectors.toList());
        response.setImages(imageDtos); // You need to add List<ImageDto> images to ProductResponse
    }
    
    return response;
    }


}