package com.example.Payment_Service.Controller;


import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.dto.UpiConfirmRequest;
import com.example.Payment_Service.dto.UpiConfirmResponse;
import com.example.Payment_Service.service.UpiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/upi")
@RequiredArgsConstructor
@Slf4j
// @CrossOrigin(origins = "*")
public class UpiController {

    private final UpiService upiService;

    @PostMapping("/create-intent")
    public ResponseEntity<?> createUpiIntent(
            @RequestBody CreatePaymentIntentRequest request) {
        log.info("REST request to create UPI intent for order: {}", request.getOrderId());
        log.info("Request body: {}", request); // 👈 Log full request
        
        try {
            CreatePaymentIntentResponse response = upiService.createUpiIntent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error creating UPI intent: ", e); // 👈 Log full stack trace
            return ResponseEntity.status(500).body("Failed to create UPI payment: " + e.getMessage());
        }
    }


    @PostMapping("/confirm/{clientSecret}")
    public ResponseEntity<UpiConfirmResponse> confirmUpiPayment(
            @PathVariable String clientSecret,
            @RequestBody UpiConfirmRequest request) {
        log.info("REST request to confirm UPI payment for clientSecret: {}", clientSecret);
        UpiConfirmResponse response = upiService.confirmUpiPayment(clientSecret, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<?> getUpiPaymentStatus(@PathVariable String transactionId) {
        log.info("Checking UPI payment status for transaction: {}", transactionId);
        return ResponseEntity.ok().build();
    }

}