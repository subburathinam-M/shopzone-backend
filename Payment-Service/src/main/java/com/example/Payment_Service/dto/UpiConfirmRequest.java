package com.example.Payment_Service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiConfirmRequest {
    private String upiId;      // "success@stripeupi"
    private String upiApp;      // "Google Pay", "PhonePe", "Paytm", "BHIM"
    private String transactionId;
}