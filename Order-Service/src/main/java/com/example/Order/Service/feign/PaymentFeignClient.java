package com.example.Order.Service.feign;
// com.example.Order.Service.feign/PaymentFeignClient.java


import com.example.Order.Service.config.FeignClientConfig;
import com.example.Order.Service.config.TokenRelayConfig;
import com.example.Order.Service.dto.PaymentRequest;
import com.example.Order.Service.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "PAYMENT-SERVICE", 
    configuration = TokenRelayConfig.class  // ✅ Use TokenRelayConfig instead
)
public interface PaymentFeignClient {
    
    @PostMapping("/api/payments/cod")
    PaymentResponse processCOD(@RequestBody PaymentRequest request);
}