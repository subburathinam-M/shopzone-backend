package com.example.Payment_Service.service.Impl;






import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.entity.Payment;
import com.example.Payment_Service.event.PaymentEvent;
import com.example.Payment_Service.repository.PaymentRepository;
import com.example.Payment_Service.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;  
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary  // 👈 Add this
public class StripeServiceImpl implements StripeService {

    private final PaymentRepository paymentRepository;

    @Transactional
    @Override
    public CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        log.info("Creating payment intent for order: {}, amount: {}", 
                 request.getOrderId(), request.getAmount());
        
        // ✅ Log the full request to see what's coming
        log.info("Full request: orderId={}, amount={}, userId={}, city={}, phone={}", 
                 request.getOrderId(), request.getAmount(), request.getUserId(), request.getUserEmail(),
                 request.getCity(), request.getPhoneNumber());

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", String.valueOf(request.getOrderId()));
            metadata.put("userId", String.valueOf(request.getUserId()));

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (request.getAmount() * 100))
                    .setCurrency(request.getCurrency())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("Payment intent created: {}", paymentIntent.getId());

            // ✅ FIX: Save ALL fields from the request
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setUserId(request.getUserId());                    // ✅ Save userId
            payment.setUserEmail(request.getUserEmail());  // 👈 ADD THIS
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod("STRIPE");
            payment.setPaymentStatus("PENDING");
            payment.setTransactionId(paymentIntent.getId());
            
            // ✅ Save address fields
            payment.setShippingAddress(request.getShippingAddress());
            payment.setPhoneNumber(request.getPhoneNumber());
            payment.setCity(request.getCity());
            payment.setPincode(request.getPincode());
            payment.setCountry(request.getCountry() != null ? request.getCountry() : "India");
            
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment saved with ID: {} for user: {}", payment.getId(), request.getUserId());


             // 👇 PUBLISH PAYMENT EVENT (PENDING)
        //     try {
        //     PaymentEvent event = new PaymentEvent(
        //         savedPayment.getId(),
        //         savedPayment.getOrderId(),
        //         savedPayment.getUserId(),
        //         savedPayment.getUserEmail(),  // Add userEmail to CreatePaymentIntentRequest
        //         savedPayment.getAmount(),
        //         "PENDING",
        //         "ONLINE",
        //         LocalDateTime.now()
        //     );
            
        //        //      kafkaTemplate.send("payment-events", event);
        //     log.info(   "📤 Published PaymentEvent (PENDING) for online order: {}", request.getOrderId());
        // } catch (Exception e) {
        //     log.error("❌ Kafka error but continuing: {}", e.getMessage());
        //     // Don't throw - let payment succeed even if Kafka fails
        // }


            CreatePaymentIntentResponse response = new CreatePaymentIntentResponse();
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount());
            response.setStatus(paymentIntent.getStatus());

            return response;

        } catch (StripeException e) {
            log.error("========== STRIPE EXCEPTION ==========");
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);  // This prints full stack trace
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

   @Transactional
@Override
public void handlePaymentSuccess(String paymentIntentId) {
    log.info("Handling payment success for: {}", paymentIntentId);
    
    Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    // ✅ IMPORTANT: Only process if it's a Stripe payment
    if (payment.getPaymentMethod() != null && !payment.getPaymentMethod().equals("UPI")) {
    
    payment.setPaymentStatus("PAID");
    
    // Try to get the actual payment method (card brand) from Stripe
    try {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        String paymentMethodId = paymentIntent.getPaymentMethod();
        if (paymentMethodId != null) {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            String cardBrand = paymentMethod.getCard().getBrand(); // e.g., "visa", "mastercard"
            payment.setPaymentMethod(cardBrand.toUpperCase()); // Store as "VISA", "MASTERCARD"
        }
    } catch (Exception e) {
        log.warn("Could not retrieve payment method details: {}", e.getMessage());
        // Fallback to original payment method
        payment.setPaymentMethod("STRIPE");
    }
} else {

    // This is a UPI payment - don't modify the payment method
    payment.setPaymentStatus("PAID");
    log.info("UPI payment {} marked as PAID", paymentIntentId);
}
    
    paymentRepository.save(payment);
    log.info("Payment {} marked as PAID for order: {}", paymentIntentId, payment.getOrderId());
    // 👇 PUBLISH PAYMENT SUCCESS EVENT
    // PaymentEvent event = new PaymentEvent(
    //     payment.getId(),
    //     payment.getOrderId(),
    //     payment.getUserId(),
    //     payment.getUserEmail(),  // Need user email
    //     payment.getAmount(),
    //     "SUCCESS",
    //     "ONLINE",
    //     LocalDateTime.now()
    // );
    
    //        //  kafkaTemplate.send("payment-events", event);
    // log.info("📤 Published PaymentEvent (SUCCESS) for order: {}", payment.getOrderId());
}

    @Transactional
    @Override
    public void handlePaymentFailure(String paymentIntentId, String error) {
        log.info("Handling payment failure for: {} - {}", paymentIntentId, error);
        
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setPaymentStatus("FAILED");
        payment.setPaymentDetails(error);
        paymentRepository.save(payment);
        
        log.info("Payment {} marked as FAILED", paymentIntentId);


        // 👇 PUBLISH PAYMENT FAILURE EVENT
        // PaymentEvent event = new PaymentEvent(
        //     payment.getId(),
        //     payment.getOrderId(),
        //     payment.getUserId(),
        //     payment.getUserEmail(), // Need user email
        //     payment.getAmount(),
        //     "FAILED",
        //     "ONLINE",
        //     LocalDateTime.now()
        // );
        
        //        //  kafkaTemplate.send("payment-events", event);
        // log.info("📤 Published PaymentEvent (FAILED) for order: {}", payment.getOrderId());
    }
}


