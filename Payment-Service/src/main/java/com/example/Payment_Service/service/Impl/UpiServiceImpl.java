package com.example.Payment_Service.service.Impl;



import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.dto.UpiConfirmRequest;
import com.example.Payment_Service.dto.UpiConfirmResponse;
import com.example.Payment_Service.entity.Payment;
import com.example.Payment_Service.repository.PaymentRepository;
import com.example.Payment_Service.service.UpiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpiServiceImpl implements UpiService {

    private final PaymentRepository paymentRepository;

    @Override
@Transactional
public CreatePaymentIntentResponse createUpiIntent(CreatePaymentIntentRequest request) {
    log.info("Creating UPI intent for order: {}, amount: {}", 
            request.getOrderId(), request.getAmount());

    // Validate required fields
    if (request.getOrderId() == null) {
        throw new IllegalArgumentException("Order ID is required");
    }
    if (request.getAmount() == null) {
        throw new IllegalArgumentException("Amount is required");
    }
    if (request.getUserEmail() == null) {
        log.warn("User email is null, but continuing...");
    }

    // Generate unique transaction ID and client secret
    String transactionId = "upi_" + UUID.randomUUID().toString() + "_" + request.getOrderId();
    String clientSecret = "upi_secret_" + System.currentTimeMillis() + "_" + request.getOrderId();

    // Create payment record
    Payment payment = new Payment();
    payment.setOrderId(request.getOrderId());
    payment.setUserId(request.getUserId());
    payment.setUserEmail(request.getUserEmail());
    payment.setAmount(request.getAmount());
    payment.setPaymentMethod("UPI");
    payment.setPaymentStatus("PENDING");
    payment.setTransactionId(transactionId);
    payment.setClientSecret(clientSecret);
    payment.setShippingAddress(request.getShippingAddress());
    payment.setPhoneNumber(request.getPhoneNumber());
    payment.setCity(request.getCity());
    payment.setPincode(request.getPincode());
    payment.setCountry(request.getCountry() != null ? request.getCountry() : "India");
    
    paymentRepository.save(payment);
    log.info("✅ UPI intent created with transaction ID: {} for order: {}", transactionId, request.getOrderId());

    CreatePaymentIntentResponse response = new CreatePaymentIntentResponse();
    response.setClientSecret(clientSecret);
    response.setPaymentIntentId(transactionId);
    response.setOrderId(request.getOrderId());
    response.setAmount(request.getAmount());
    response.setStatus("PENDING");

    return response;
}

    @Override
    @Transactional
    public UpiConfirmResponse confirmUpiPayment(String clientSecret, UpiConfirmRequest request) {
        log.info("Confirming UPI payment for clientSecret: {}", clientSecret);
        
        // 🔍 Debug logs
        log.info("Request received - upiId: {}, upiApp: {}", request.getUpiId(), request.getUpiApp());
    
        try {
            // Find payment by client secret
            Payment payment = paymentRepository.findByClientSecret(clientSecret)
                    .orElseThrow(() -> new RuntimeException("Payment not found with client secret: " + clientSecret));
            
            log.info("Payment found: {}", payment.getTransactionId());
    
            // Validate
            if (request.getUpiId() == null || request.getUpiId().isEmpty()) {
                log.error("UPI ID is empty");
                throw new IllegalArgumentException("UPI ID is required");
            }
    
            log.info("Calling simulateUpiVerification...");
            boolean paymentSuccess = simulateUpiVerification(request.getUpiId(), request.getUpiApp());
            log.info("simulateUpiVerification returned: {}", paymentSuccess);
    
            if (paymentSuccess) {
                payment.setPaymentMethod("UPI");
                payment.setPaymentStatus("PAID");
                payment.setPaymentDetails(String.format("UPI Payment via %s | UPI ID: %s", 
                        request.getUpiApp(), request.getUpiId()));
                paymentRepository.save(payment);
    
                log.info("UPI payment confirmed successfully for transaction: {}", payment.getTransactionId());
    
                return UpiConfirmResponse.builder()
                        .status("SUCCESS")
                        .message("Payment successful")
                        .transactionId(payment.getTransactionId())
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .upiId(request.getUpiId())
                        .upiApp(request.getUpiApp())
                        .paymentTime(LocalDateTime.now())
                        .build();
            } else {
                payment.setPaymentStatus("FAILED");
                payment.setPaymentDetails("UPI payment failed");
                paymentRepository.save(payment);
    
                throw new RuntimeException("UPI payment failed");
            }
        } catch (Exception e) {
            log.error("Error in confirmUpiPayment: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void handleUpiSuccess(String transactionId) {
        log.info("Handling UPI success for transaction: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setPaymentStatus("PAID");
        paymentRepository.save(payment);
        
        log.info("UPI payment {} marked as PAID", transactionId);
    }

    @Override
    @Transactional
    public void handleUpiFailure(String transactionId, String error) {
        log.info("Handling UPI failure for transaction: {} - {}", transactionId, error);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setPaymentStatus("FAILED");
        payment.setPaymentDetails(error);
        paymentRepository.save(payment);
        
        log.info("UPI payment {} marked as FAILED", transactionId);
    }

    private boolean simulateUpiVerification(String upiId, String upiApp) {
        // In test mode, always return true
        return true;
    }
}