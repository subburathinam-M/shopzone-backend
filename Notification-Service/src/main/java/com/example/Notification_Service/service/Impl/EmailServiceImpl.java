package com.example.Notification_Service.service.Impl;

import com.example.Notification_Service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor

public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;



    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("🎉 Welcome to ShopZone!");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Welcome to ShopZone! We're excited to have you on board.\n\n" +
                "Start shopping now and enjoy amazing products!\n\n" +
                "Best regards,\n" +
                "ShopZone Team",
                username
            ));
            
            mailSender.send(message);
            log.info("✅ Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email: {}", e.getMessage());
        }
    }

    @Override
    public void sendOrderConfirmationEmail(String to, Long orderId, Double amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("✅ Order Confirmed - ShopZone");
            message.setText(String.format(
                "Dear Customer,\n\n" +
                "Your order #%d has been confirmed!\n\n" +
                "Order Details:\n" +
                "- Order ID: %d\n" +
                "- Total Amount: ₹%.2f\n\n" +
                "We'll notify you when your order ships.\n\n" +
                "Thank you for shopping with ShopZone!",
                orderId, orderId, amount
            ));
            
            mailSender.send(message);
            log.info("✅ Order confirmation email sent for order: {}", orderId);
        } catch (Exception e) {
            log.error("❌ Failed to send order confirmation: {}", e.getMessage());
        }
    }

    @Override
    public void sendPaymentSuccessEmail(String to, Long orderId, Double amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("💰 Payment Successful - ShopZone");
            message.setText(String.format(
                "Dear Customer,\n\n" +
                "Payment for order #%d has been successfully processed!\n\n" +
                "Amount Paid: ₹%.2f\n\n" +
                "Your order will be shipped soon.\n\n" +
                "Thanks for shopping with ShopZone!",
                orderId, amount
            ));
            
            mailSender.send(message);
            log.info("✅ Payment success email sent for order: {}", orderId);
        } catch (Exception e) {
            log.error("❌ Failed to send payment success email: {}", e.getMessage());
        }
    }

    @Override
public void sendPasswordResetEmail(String to, String token, String username) {
    try {
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Password Reset Request - ShopZone");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "You have requested to reset your password. Click the link below to proceed:\n\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Thanks,\n" +
            "ShopZone Team",
            username, resetLink
        ));
        
        mailSender.send(message);
        log.info("✅ Password reset email sent to: {}", to);
    } catch (Exception e) {
        log.error("❌ Failed to send password reset email: {}", e.getMessage());
    }
}

}
