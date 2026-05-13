package com.example.Notification_Service.service;

public interface EmailService {

    public void sendWelcomeEmail(String to, String username);
    public void sendOrderConfirmationEmail(String to, Long orderId, Double amount);
    public void sendPaymentSuccessEmail(String to, Long orderId, Double amount);
    void sendPasswordResetEmail(String to, String token, String username);


}
