package com.example.Payment_Service.service;

import com.example.Payment_Service.dto.PaymentRequest;
import com.example.Payment_Service.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse processCODPayment(PaymentRequest request);
    PaymentResponse markAsPaid(Long paymentId) ;
    PaymentResponse getPaymentByOrderId(Long orderId);
    

}
