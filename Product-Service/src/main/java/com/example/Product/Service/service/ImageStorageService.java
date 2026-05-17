package com.example.Product.Service.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {
    
    private final Path imageLocation = Paths.get("./uploads/products");
    
    public ImageStorageService() {
        try {
            Files.createDirectories(imageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    public String storeImage(MultipartFile file, Long productId) throws IOException {
        // Create product-specific folder
        Path productFolder = imageLocation.resolve(String.valueOf(productId));
        Files.createDirectories(productFolder);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = productFolder.resolve(filename);

    System.out.println("Saving image to: " + filePath.toAbsolutePath());

    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return URL path
        return "/uploads/products/" + productId + "/" + filename;
    }
    
    public void deleteImage(String imageUrl) {
        try {
            Path filePath = Paths.get("uploads", imageUrl.replace("/uploads/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete image", e);
        }
    }
}