package com.example.Order.Service.service;

import com.example.Order.Service.entity.Order;
import java.util.List;

public interface OrderService {
    List<Order> getUserOrders(String  keycloakId);  // Keep for backward compatibility
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    
    // Updated methods with Keycloak fields
    Order placeOrder(Long productId, String keycloakId, String email, String username);
    Order placeOrderWithQuantity(Long productId, int quantity, String keycloakId, String email, String username);
    Order placeCODOrder(Long productId, int quantity, String address, String phone, 
                    String city, String pincode, String keycloakId, String email, String username);
    Order placeOnlineOrder(Long productId, int quantity, Double price, String address, String phone, 
                        String city, String pincode, String keycloakId, String email, String username);
    Order updateOrderStatus(Long orderId, String status);
    void softDeleteOrder(Long orderId);
}