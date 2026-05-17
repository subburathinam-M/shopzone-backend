package com.example.Order.Service.feign;

import com.example.Order.Service.config.FeignClientConfig;
import com.example.Order.Service.dto.PaymentRequest;
import com.example.Order.Service.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "PAYMENT-SERVICE", 
    configuration = FeignClientConfig.class  // ✅ Changed from TokenRelayConfig
)
public interface PaymentFeignClient {
    
    @PostMapping("/api/payments/cod")
    PaymentResponse processCOD(@RequestBody PaymentRequest request);
}