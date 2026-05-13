package com.example.Payment_Service.service;

import com.example.Payment_Service.dto.CreatePaymentIntentRequest;
import com.example.Payment_Service.dto.CreatePaymentIntentResponse;

public interface StripeService {

    CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) ;
    void handlePaymentSuccess(String paymentIntentId);
    void handlePaymentFailure(String paymentIntentId, String error);


}
