package com.example.Order.Service.feign;


import com.example.Order.Service.config.FeignClientConfig;
import com.example.Order.Service.config.TokenRelayConfig;
// import com.example.Order.Service.config.FeignConfig;
import com.example.Order.Service.dto.Product;
import com.example.Order.Service.fallback.ProductFallbackFactory;
// import com.example.Order.Service.fallback.ProductServiceFallback;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "PRODUCT-SERVICE",
    configuration = TokenRelayConfig.class,  // ✅ Use TokenRelayConfig instead
    fallbackFactory = ProductFallbackFactory.class
)
    
public interface ProductFeignClient {
    
    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable("id") Long id);
    
     // 🔥 NEW: Decrement stock endpoint
    @PutMapping("/products/{id}/decrement-stock")
    void decrementStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}