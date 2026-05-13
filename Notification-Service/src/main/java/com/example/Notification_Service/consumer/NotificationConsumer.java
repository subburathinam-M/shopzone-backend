package com.example.Notification_Service.consumer;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.Notification_Service.event.OrderPlacedEvent;
import com.example.Notification_Service.event.PasswordResetEvent;
import com.example.Notification_Service.event.PaymentEvent;
import com.example.Notification_Service.event.UserRegisteredEvent;
import com.example.Notification_Service.service.EmailService;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "user-events", 
                containerFactory = "userRegisteredKafkaListenerContainerFactory")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("📥 Received UserRegisteredEvent for: {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());
    }

    @KafkaListener(topics = "password-reset-events", 
                containerFactory = "passwordResetKafkaListenerContainerFactory")
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("📥 Received PasswordResetEvent for: {}", event.getEmail());
        emailService.sendPasswordResetEmail(
            event.getEmail(),
            event.getResetToken(),
            event.getUsername()
        );
    }

    @KafkaListener(topics = "order-events", 
                   containerFactory = "orderPlacedKafkaListenerContainerFactory")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("📥 Received OrderPlacedEvent: Order #{} for user: {}", 
                event.getOrderId(), event.getUserEmail());
        emailService.sendOrderConfirmationEmail(
            event.getUserEmail(),
            event.getOrderId(),
            event.getTotalAmount()
        );
    }

    @KafkaListener(topics = "payment-events", 
                   containerFactory = "paymentKafkaListenerContainerFactory")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("📥 Received PaymentEvent: Order #{}, Status: {}", 
                 event.getOrderId(), event.getStatus());
        if ("SUCCESS".equals(event.getStatus())) {
            emailService.sendPaymentSuccessEmail(
                event.getUserEmail(),
                event.getOrderId(),
                event.getAmount()
            );
        } else if ("FAILED".equals(event.getStatus())) {
            log.warn("❌ Payment failed for order #{}", event.getOrderId());
        }
    }
}