package com.example.AUTH_SERVICE.service.serviceImpl;

import com.example.AUTH_SERVICE.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Reset Request - ShopZone");
            message.setText(
                "Hello " + username + ",\n\n" +
                "You have requested to reset your password. Click the link below:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Thanks,\nShopZone Team"
            );
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Log token so user can still reset in dev/test
            log.info("RESET LINK for {}: {}", to, resetLink);
        }
    }
}
