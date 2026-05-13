package com.example.Payment_Service.Controller;



import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.service.StripeService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
// @RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;

    @Autowired
    public StripeController(@Qualifier("stripeServiceImpl") StripeService stripeService) {
        this.stripeService = stripeService;
    }


    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
            @RequestBody CreatePaymentIntentRequest request) {
        
        CreatePaymentIntentResponse response = stripeService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received webhook from Stripe");

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();
                    stripeService.handlePaymentSuccess(paymentIntent.getId());
                    log.info("Payment succeeded: {}", paymentIntent.getId());
                    break;
                    
                case "payment_intent.payment_failed":
                    PaymentIntent failedIntent = (PaymentIntent) event.getData().getObject();
                    stripeService.handlePaymentFailure(failedIntent.getId(), 
                                                    failedIntent.getLastPaymentError().getMessage());
                    log.info("Payment failed: {}", failedIntent.getId());
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

@PostMapping("/confirm/{paymentIntentId}")
public ResponseEntity<?> confirmPayment(@PathVariable String paymentIntentId) {
    stripeService.handlePaymentSuccess(paymentIntentId);
    return ResponseEntity.ok("Payment confirmed");
}
}

