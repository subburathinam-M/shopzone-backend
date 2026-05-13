package com.example.Order.Service.fallback;


import com.example.Order.Service.dto.Product;
import com.example.Order.Service.exception.InvalidQuantityException;
import com.example.Order.Service.exception.OutOfStockException;
import com.example.Order.Service.exception.ProductNotFoundException;
import com.example.Order.Service.exception.ServiceUnavailableException;
import com.example.Order.Service.feign.ProductFeignClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeoutException;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductFallbackFactory implements FallbackFactory<ProductFeignClient> {

    @Override
    public ProductFeignClient create(Throwable cause) {
        
        log.error("Fallback triggered. Cause type: {}, Message: {}", 
                cause.getClass().getSimpleName(), cause.getMessage());

        return new ProductFeignClient() {
            
            @Override
            public Product getProductById(Long id) {
                
                // 🔥 FIX: Handle 401 Unauthorized specifically
                if (cause instanceof FeignException.Unauthorized) {
                    log.error("🔐 AUTHENTICATION FAILED! JWT token missing or invalid for product service: {}", id);
                    
                    // Return fallback product but log clearly that it's an auth issue
                    Product fallbackProduct = new Product();
                    fallbackProduct.setId(id);
                    fallbackProduct.setName("PENDING_PRODUCT_" + id);
                    fallbackProduct.setPrice(0.0);
                    fallbackProduct.setDescription("Authentication failed. Check token propagation.");
                    fallbackProduct.setStock(0);
                    fallbackProduct.setFallback(true);
                    
                    log.warn("⚠️ Returning fallback due to 401 Unauthorized for product {}", id);
                    return fallbackProduct;
                }
                
                // Case 1: Product not found (404)
                if (cause instanceof FeignException.NotFound) {
                    log.error("Product not found: {}", id);
                    throw new ProductNotFoundException("Product not found with id: " + id);
                }
                
                // Case 2: Service unavailable (503)
                if (cause instanceof FeignException.ServiceUnavailable) {
                    log.warn("🛑 SERVICE UNAVAILABLE - Product service down for product {}", id);
                    
                    Product fallbackProduct = new Product();
                    fallbackProduct.setId(id);
                    fallbackProduct.setName("PENDING_PRODUCT_" + id);
                    fallbackProduct.setPrice(0.0);
                    fallbackProduct.setDescription("Product service is currently unavailable");
                    fallbackProduct.setStock(0);
                    fallbackProduct.setFallback(true);
                    
                    return fallbackProduct;
                }
                
                // Case 3: Timeout - Product service is slow!
                if (cause instanceof TimeoutException || 
                    (cause.getMessage() != null && cause.getMessage().contains("timeout")) ||
                    (cause instanceof FeignException && cause.getMessage() != null && 
                     cause.getMessage().contains("timed out"))) {
                    log.warn("⏱️ TIMEOUT - Product service slow for product {}", id);
                    
                    Product fallbackProduct = new Product();
                    fallbackProduct.setId(id);
                    fallbackProduct.setName("PENDING_PRODUCT_" + id);
                    fallbackProduct.setPrice(0.0);
                    fallbackProduct.setDescription("Product service is slow. Try again.");
                    fallbackProduct.setStock(0);
                    fallbackProduct.setFallback(true);
                    
                    return fallbackProduct;
                }
                
                // Case 4: Business exceptions
                if (cause instanceof ProductNotFoundException || 
                    cause instanceof OutOfStockException ||
                    cause instanceof InvalidQuantityException) {
                    log.error("Business exception for product {}: {}", id, cause.getMessage());
                    throw (RuntimeException) cause;
                }
                
                // Case 5: Any other FeignException (including 500, 400, etc)
                if (cause instanceof FeignException) {
                    FeignException fe = (FeignException) cause;
                    int status = fe.status();
                    
                    log.warn("⚠️ Feign error with status {} for product {}", status, id);
                    
                    Product fallbackProduct = new Product();
                    fallbackProduct.setId(id);
                    fallbackProduct.setName("PENDING_PRODUCT_" + id);
                    fallbackProduct.setPrice(0.0);
                    fallbackProduct.setDescription("Product service error (HTTP " + status + ")");
                    fallbackProduct.setStock(0);
                    fallbackProduct.setFallback(true);
                    
                    return fallbackProduct;
                }
                
                // Case 6: Default - Service down / Circuit open
                log.warn("⚠️ SERVICE DOWN/CIRCUIT OPEN - Using fallback for product {}", id);
                
                Product fallbackProduct = new Product();
                fallbackProduct.setId(id);
                fallbackProduct.setName("PENDING_PRODUCT_" + id);
                fallbackProduct.setPrice(0.0);
                fallbackProduct.setDescription("Product service temporarily unavailable");
                fallbackProduct.setStock(0);
                fallbackProduct.setFallback(true);
                
                return fallbackProduct;
            }
            
            @Override
            public void decrementStock(Long id, int quantity) {
                // Check for 401 Unauthorized
                if (cause instanceof FeignException.Unauthorized) {
                    log.error("🔐 Cannot decrement stock - Authentication failed for product {}", id);
                    throw new ServiceUnavailableException("Authentication failed with product service");
                }
                
                log.warn("Cannot decrement stock - Service issue for product {}", id);
                throw new ServiceUnavailableException("Product service is temporarily unavailable");
            }
        };
    }
}