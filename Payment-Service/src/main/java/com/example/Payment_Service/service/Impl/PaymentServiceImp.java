package com.example.Payment_Service.service.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Payment_Service.dto.PaymentRequest;
import com.example.Payment_Service.dto.PaymentResponse;
import com.example.Payment_Service.entity.Payment;
import com.example.Payment_Service.event.PaymentEvent;
import com.example.Payment_Service.repository.PaymentRepository;
import com.example.Payment_Service.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository paymentRepository;
      private final KafkaTemplate<String, Object> kafkaTemplate;  // 👈 ADD

    @Override  // ✅ ADD THIS
    @Transactional
    public PaymentResponse processCODPayment(PaymentRequest request) {
        log.info("Processing COD payment for order: {}", request.getOrderId());

        // Validate COD payment
        if (!"COD".equals(request.getPaymentMethod())) {
            throw new RuntimeException("Invalid payment method for COD");
        }

        // Validate address fields
        if (request.getShippingAddress() == null || request.getPhoneNumber() == null) {
            throw new RuntimeException("Address and phone number required for COD");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod("COD");
        payment.setPaymentStatus("PENDING");
        payment.setShippingAddress(request.getShippingAddress());
        payment.setPhoneNumber(request.getPhoneNumber());
        payment.setCity(request.getCity());
        payment.setPincode(request.getPincode());
        payment.setCountry(request.getCountry() != null ? request.getCountry() : "India");

        Payment savedPayment = paymentRepository.save(payment);
        log.info("COD payment created with ID: {} for order: {}", savedPayment.getId(), request.getOrderId());


         // 👇 PUBLISH PAYMENT EVENT (PENDING)
        PaymentEvent event = new PaymentEvent(
            savedPayment.getId(),
            savedPayment.getOrderId(),
            savedPayment.getUserId(),
            payment.getUserEmail(),  // 👈 Now has value! // userEmail - you need to get this from User Service or add to request
            savedPayment.getAmount(),
            "PENDING",
            "COD",
            LocalDateTime.now()
        );
        
        kafkaTemplate.send("payment-events", event);
        log.info("📤 Published PaymentEvent (PENDING) for order: {}", request.getOrderId());

        return mapToResponse(savedPayment);
    }

    @Override  // ✅ ADD THIS
    @Transactional
    public PaymentResponse markAsPaid(Long paymentId) {
        log.info("Marking payment as PAID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setPaymentStatus("PAID");
        Payment updated = paymentRepository.save(payment);
        
        log.info("Payment {} marked as PAID", paymentId);

         // 👇 PUBLISH PAYMENT SUCCESS EVENT
         PaymentEvent event = new PaymentEvent(
            updated.getId(),
            updated.getOrderId(),
            updated.getUserId(),
            payment.getUserEmail(),  // 👈 Now has value!,  // userEmail - you need to get this
            updated.getAmount(),
            "SUCCESS",
            updated.getPaymentMethod(),
            LocalDateTime.now()
        );
        
        kafkaTemplate.send("payment-events", event);
        log.info("📤 Published PaymentEvent (SUCCESS) for order: {}", updated.getOrderId());

        return mapToResponse(updated);
    }

    @Override  // ✅ ADD THIS
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setCreatedAt(payment.getCreatedAt());
        response.setMessage("Payment processed successfully");
        return response;
    }
}