package com.example.Order.Service.controller;

import com.example.Order.Service.entity.Order;
import com.example.Order.Service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    // ✅ USER: Get their own orders (from JWT token)
    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();  // Get Keycloak user ID
        List<Order> orders = orderService.getUserOrders(keycloakId);  // Pass keycloakId directly
        return ResponseEntity.ok(orders);
    }

    // ✅ ADMIN: Get all orders with user details
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // ✅ Get single order by ID (with permission check)
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakId = jwt.getSubject();
        String role = extractRole(jwt);
        
        Order order = orderService.getOrderById(id);
        
        // Check if user owns this order or is admin
        if (!order.getUserKeycloakId().equals(keycloakId) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(order);
    }

    // Single quantity (default = 1)
    @PostMapping("/{productId}")
    public ResponseEntity<?> placeOrder(
            @PathVariable Long productId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String username = jwt.getClaim("preferred_username");
        
        Order order = orderService.placeOrder(productId, keycloakId, email, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Order placed successfully");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    // Multiple quantity endpoint
    @PostMapping("/{productId}/quantity/{quantity}")
    public ResponseEntity<?> placeOrderWithQuantity(
            @PathVariable Long productId,
            @PathVariable int quantity,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String username = jwt.getClaim("preferred_username");
        
        Order order = orderService.placeOrderWithQuantity(productId, quantity, keycloakId, email, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Order placed successfully");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    // 🔥 COD Order endpoint
    @PostMapping("/cod/{productId}")
    public ResponseEntity<?> placeCODOrder(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String city,
            @RequestParam String pincode,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String username = jwt.getClaim("preferred_username");
        
        Order order = orderService.placeCODOrder(
            productId, quantity, address, phone, city, pincode, 
            keycloakId, email, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "COD order placed successfully");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    // 🔥 ONLINE Order endpoint
  // In OrderController.java - update the method signature
@PostMapping("/online/{productId}")
public ResponseEntity<?> placeOnlineOrder(
        @PathVariable Long productId,
        @RequestParam int quantity,
        @RequestParam Double price,  // ✅ Make sure this exists
        @RequestParam String address,
        @RequestParam String phone,
        @RequestParam String city,
        @RequestParam String pincode,       
        @AuthenticationPrincipal Jwt jwt) {
    
    String keycloakId = jwt.getSubject();
    String email = jwt.getClaim("email");
    String username = jwt.getClaim("preferred_username");
    
    Order order = orderService.placeOnlineOrder(
        productId, quantity, price, address, phone, city, pincode,  // ✅ Pass price here!
        keycloakId, email, username);
    
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Online order placed successfully");
    response.put("order", order);
    return ResponseEntity.ok(response);
}

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String role = extractRole(jwt);
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        
        String status = request.get("status");
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }
    
    // @DeleteMapping("/{orderId}")
    // public ResponseEntity<?> deleteOrder(
    //         @PathVariable Long orderId,
    //         @AuthenticationPrincipal Jwt jwt) {
        
    //     String role = extractRole(jwt);
    //     if (!"ADMIN".equals(role)) {
    //         return ResponseEntity.status(403).build();
    //     }
        
    //     orderService.softDeleteOrder(orderId);
    //     return ResponseEntity.ok().build();
    // }

    @DeleteMapping("/{orderId}")
public ResponseEntity<?> deleteOrder(
        @PathVariable Long orderId,
        @AuthenticationPrincipal Jwt jwt) {
    
    String keycloakId = jwt.getSubject();
    String role = extractRole(jwt);
    
    try {
        // Get the order first
        Order order = orderService.getOrderById(orderId);
        
        // Allow if: ADMIN OR Order Owner
        if (!"ADMIN".equals(role) && !order.getUserKeycloakId().equals(keycloakId)) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "You don't have permission to delete this order"
            ));
        }
        
        // Soft delete
        orderService.softDeleteOrder(orderId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Order deleted successfully"
        ));
        
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of(
            "success", false,
            "message", "Failed to delete order: " + e.getMessage()
        ));
    }
}

    // Helper method to extract role from JWT
    private String extractRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            if (roles.contains("ADMIN")) {
                return "ADMIN";
            }
        }
        return "USER";
    }

    // Helper method to map keycloakId to local userId (implement based on your needs)
    private Long getUserIdFromKeycloakId(String keycloakId) {
        // You might want to call Auth Service or have a local mapping
        // For now, return null or throw exception
        throw new UnsupportedOperationException("Implement mapping from keycloakId to local userId");
    }
}