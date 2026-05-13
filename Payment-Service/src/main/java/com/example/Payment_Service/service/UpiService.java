package com.example.Payment_Service.service;


import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;
import com.example.Payment_Service.dto.UpiConfirmRequest;
import com.example.Payment_Service.dto.UpiConfirmResponse;


public interface UpiService {
    CreatePaymentIntentResponse createUpiIntent(CreatePaymentIntentRequest request);
    UpiConfirmResponse confirmUpiPayment(String clientSecret, UpiConfirmRequest request);
    void handleUpiSuccess(String transactionId);
    void handleUpiFailure(String transactionId, String error);
}