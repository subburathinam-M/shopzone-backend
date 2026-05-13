package com.example.Payment_Service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Payment_Service.entity.Payment;


@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByUserId(String userId);
    List<Payment> findByPaymentStatus(String status);
    // Add this method to PaymentRepository.java
Optional<Payment> findByTransactionId(String transactionId);
Optional<Payment> findByClientSecret(String clientSecret);

}
