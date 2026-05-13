package com.example.Payment_Service.Controller;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Payment_Service.dto.PaymentRequest;
import com.example.Payment_Service.dto.PaymentResponse;
import com.example.Payment_Service.service.PaymentService;  // ✅ Use interface 

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/cod")
    public ResponseEntity<PaymentResponse> processCOD(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processCODPayment(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/mark-paid")
    public ResponseEntity<PaymentResponse> markAsPaid(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.markAsPaid(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}