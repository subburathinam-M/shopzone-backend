package com.example.AUTH_SERVICE.service.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.AUTH_SERVICE.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService{

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.reset-password.path}")
    private String resetPasswordPath;

    private final JavaMailSender mailSender;



     public void sendPasswordResetEmail(String to, String token) {
        try {
            // String resetLink = "http://localhost:3000/reset-password?token=" + token;
            String resetLink = frontendUrl + resetPasswordPath + "?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Reset Request - ShopZone");
            message.setText(
                "Hello,\n\n" +
                "You have requested to reset your password. Click the link below to proceed:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Thanks,\n" +
                "ShopZone Team"
            );
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            // For testing - log the token
            log.info("🔐 RESET TOKEN for {}: {}", to, token);
        }
    }



}
