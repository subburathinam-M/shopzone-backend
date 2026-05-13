package com.example.Payment_Service.service.Impl;





import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.entity.Payment;
import com.example.Payment_Service.repository.PaymentRepository;
import com.example.Payment_Service.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("stripeUPIServiceImpl")
public class StripeUPIServiceImpl implements StripeService {

    private final PaymentRepository paymentRepository;

    @Transactional
    @Override
    public CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        log.info("Creating UPI payment intent for order: {}, amount: {}", 
                request.getOrderId(), request.getAmount());

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", String.valueOf(request.getOrderId()));
            metadata.put("userId", String.valueOf(request.getUserId()));

            // ✅ Use the same AutomaticPaymentMethods approach as StripeServiceImpl
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
            log.info("UPI payment intent created: {}", paymentIntent.getId());

            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setUserId(request.getUserId());
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod("STRIPE_UPI");
            payment.setPaymentStatus("PENDING");
            payment.setTransactionId(paymentIntent.getId());
            payment.setShippingAddress(request.getShippingAddress());
            payment.setPhoneNumber(request.getPhoneNumber());
            payment.setCity(request.getCity());
            payment.setPincode(request.getPincode());
            payment.setCountry(request.getCountry() != null ? request.getCountry() : "India");
            
            paymentRepository.save(payment);

            CreatePaymentIntentResponse response = new CreatePaymentIntentResponse();
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount());
            response.setStatus(paymentIntent.getStatus());

            return response;

        } catch (StripeException e) {
            log.error("Stripe UPI error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create UPI payment intent: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create UPI payment intent: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void handlePaymentSuccess(String paymentIntentId) {
        log.info("Handling UPI payment success for: {}", paymentIntentId);
        
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus("PAID");
        
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            String paymentMethodId = paymentIntent.getPaymentMethod();
            if (paymentMethodId != null) {
                PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
                // For UPI, we'll store as "UPI"
                payment.setPaymentMethod("UPI");
            } else {
                payment.setPaymentMethod("UPI");
            }
        } catch (Exception e) {
            log.warn("Could not retrieve payment method details: {}", e.getMessage());
            payment.setPaymentMethod("UPI");
        }
        
        paymentRepository.save(payment);
        log.info("UPI payment {} marked as PAID for order: {}", paymentIntentId, payment.getOrderId());
    }

    @Transactional
    @Override
    public void handlePaymentFailure(String paymentIntentId, String error) {
        log.info("Handling UPI payment failure for: {} - {}", paymentIntentId, error);
        
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setPaymentStatus("FAILED");
        payment.setPaymentDetails(error);
        paymentRepository.save(payment);
        
        log.info("UPI payment {} marked as FAILED", paymentIntentId);
    }
}

