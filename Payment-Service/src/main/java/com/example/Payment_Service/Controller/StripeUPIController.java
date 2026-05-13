package com.example.Payment_Service.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.service.StripeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/stripe/upi")
@Slf4j
public class StripeUPIController {

    private final StripeService stripeUPIService;

    @Autowired
    public StripeUPIController(@Qualifier("stripeUPIServiceImpl") StripeService stripeUPIService) {
        this.stripeUPIService = stripeUPIService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<CreatePaymentIntentResponse> createUPIPaymentIntent(
            @RequestBody CreatePaymentIntentRequest request) {
                log.info("Creating Stripe UPI payment for order: {}", request.getOrderId());
        return ResponseEntity.ok(stripeUPIService.createPaymentIntent(request));
    }
}