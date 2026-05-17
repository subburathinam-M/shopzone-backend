package com.example.Order.Service.feign;

import com.example.Order.Service.config.FeignClientConfig;
import com.example.Order.Service.dto.Product;
import com.example.Order.Service.fallback.ProductFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "PRODUCT-SERVICE",
    configuration = FeignClientConfig.class,  // ✅ Changed from TokenRelayConfig
    fallbackFactory = ProductFallbackFactory.class
)
public interface ProductFeignClient {
    
    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable("id") Long id);
    
    @PutMapping("/products/{id}/decrement-stock")
    void decrementStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}