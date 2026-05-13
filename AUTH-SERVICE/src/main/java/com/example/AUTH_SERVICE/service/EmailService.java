package com.example.AUTH_SERVICE.service;

public interface EmailService {

    public void sendPasswordResetEmail(String to, String token);

}
