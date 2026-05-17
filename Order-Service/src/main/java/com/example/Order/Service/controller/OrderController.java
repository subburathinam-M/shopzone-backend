package com.example.Order.Service.controller;

import com.example.Order.Service.entity.Order;
import com.example.Order.Service.service.OrderService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<Order> orders = orderService.getUserOrders(username);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        String role = extractRole(userDetails);
        Order order = orderService.getOrderById(id);

        if (!order.getUserKeycloakId().equals(username) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> placeOrder(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();
        String email = extractEmail(request);

        Order order = orderService.placeOrder(productId, username, email, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order placed successfully",
                "order", order));
    }

    @PostMapping("/{productId}/quantity/{quantity}")
    public ResponseEntity<?> placeOrderWithQuantity(
            @PathVariable Long productId,
            @PathVariable int quantity,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();
        String email = extractEmail(request);

        Order order = orderService.placeOrderWithQuantity(productId, quantity, username, email, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order placed successfully",
                "order", order));
    }

    @PostMapping("/cod/{productId}")
    public ResponseEntity<?> placeCODOrder(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String city,
            @RequestParam String pincode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();
        String email = extractEmail(request);

        Order order = orderService.placeCODOrder(
                productId, quantity, address, phone, city, pincode,
                username, email, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "COD order placed successfully",
                "order", order));
    }

    @PostMapping("/online/{productId}")
    public ResponseEntity<?> placeOnlineOrder(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam Double price,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String city,
            @RequestParam String pincode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();
        String email = extractEmail(request);

        Order order = orderService.placeOnlineOrder(
                productId, quantity, price, address, phone, city, pincode,
                username, email, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Online order placed successfully",
                "order", order));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        String role = extractRole(userDetails);

        try {
            Order order = orderService.getOrderById(orderId);
            if (!"ADMIN".equals(role) && !order.getUserKeycloakId().equals(username)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "You don't have permission to delete this order"));
            }
            orderService.softDeleteOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Order deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private String extractRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
    }

    private String extractEmail(HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                return claims.get("email", String.class);
            }
        } catch (Exception e) {
            log.warn("Could not extract email from token: {}", e.getMessage());
        }
        return "";
    }
}
