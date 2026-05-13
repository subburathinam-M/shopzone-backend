package com.example.Order.Service.service.impl;

import com.example.Order.Service.Repository.OrderRepository;
import com.example.Order.Service.dto.PaymentRequest;
import com.example.Order.Service.dto.PaymentResponse;
import com.example.Order.Service.dto.Product;
import com.example.Order.Service.entity.Order;
import com.example.Order.Service.event.OrderPlacedEvent;
import com.example.Order.Service.exception.InvalidQuantityException;
import com.example.Order.Service.exception.OutOfStockException;
import com.example.Order.Service.exception.ProductNotFoundException;
import com.example.Order.Service.exception.ServiceUnavailableException;
import com.example.Order.Service.feign.PaymentFeignClient;
import com.example.Order.Service.feign.ProductFeignClient;
import com.example.Order.Service.service.OrderService;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductFeignClient productFeignClient;
    private final PaymentFeignClient paymentFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductFeignClient productFeignClient,
                            PaymentFeignClient paymentFeignClient,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productFeignClient = productFeignClient;
        this.paymentFeignClient = paymentFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<Order> getUserOrders(String  keycloakId) {
        log.info("Fetching orders for user ID: {}", keycloakId);
        return orderRepository.findByUserKeycloakId(keycloakId);
    }

    @Override
    public List<Order> getAllOrders() {
        log.info("Fetching ALL orders for admin");
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        log.info("Fetching order with ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    @Transactional
    public Order placeOrder(Long productId, String keycloakId, String email, String username) {
        return placeOrderWithQuantity(productId, 1, keycloakId, email, username);
    }

    @Override
    @Transactional
    public Order placeOrderWithQuantity(Long productId, int quantity, String keycloakId, String email, String username) {
        log.info("Placing order for product ID: {} with quantity: {}", productId, quantity);

        log.info("Order placed by user: {} (Keycloak ID: {}, Email: {})",
                username, keycloakId, email);

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        try {
            Product product = productFeignClient.getProductById(productId);
            log.info("Product received: {}, isFallback: {}", product.getName(), product.isFallback());

            if (product.isFallback()) {
                log.warn("⚠️ SERVICE DOWN - Creating PENDING order for ID: {}", productId);
                return createPendingOrder(productId, quantity, keycloakId, email, username);
            }

            log.info("✅ Service available - Processing order for: {} with stock: {}",
                    product.getName(), product.getStock());

            if (product.getStock() < quantity) {
                throw new OutOfStockException(
                        String.format("Insufficient stock for '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getStock(), quantity));
            }

            try {
                productFeignClient.decrementStock(productId, quantity);
                log.info("Stock decremented successfully for product: {} by {}", productId, quantity);
            } catch (Exception e) {
                log.error("Failed to decrement stock: {}", e.getMessage());
                throw new ServiceUnavailableException("Failed to update stock. Please try again.");
            }

            return createConfirmedOrder(product, quantity, keycloakId, email, username);

        } catch (ProductNotFoundException | OutOfStockException | InvalidQuantityException e) {
            log.error("❌ Order failed - Business exception: {}", e.getMessage());
            throw e;
        } catch (FeignException.ServiceUnavailable e) {
            log.error("❌ Service unavailable: {}", e.getMessage());
            throw new ServiceUnavailableException("Product service is temporarily unavailable.");
        } catch (FeignException e) {
            log.error("❌ Feign error: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to communicate with product service.");
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage());
            throw new RuntimeException("Failed to place order: " + e.getMessage());
        }
    }

    private Order createPendingOrder(Long productId, int quantity, String keycloakId, String email, String username) {
        Order pendingOrder = new Order();
        pendingOrder.setProductId(productId);
        pendingOrder.setProductName("PENDING_" + productId);
        pendingOrder.setPrice(0.0);
        pendingOrder.setQuantity(quantity);
        pendingOrder.setStatus("PENDING");
        pendingOrder.setNotes("Product service temporarily unavailable. Order will be processed when service is back.");
        pendingOrder.setUserKeycloakId(keycloakId);
        pendingOrder.setUserEmail(email);
        pendingOrder.setUserName(username);

        Order savedOrder = orderRepository.save(pendingOrder);

        // 👇 Also publish event for pending orders (with zero amount)
        OrderPlacedEvent event = new OrderPlacedEvent(
            savedOrder.getId(),
            null, // userId can be null now, use keycloakId instead
            email,
            username,
            productId,
            quantity,
            0.0,
            "PENDING",
            LocalDateTime.now()
        );

        kafkaTemplate.send("order-events", event);
        log.info("📤 Published PENDING OrderPlacedEvent to Kafka for order: {}", savedOrder.getId());

        log.info("✅ PENDING order created with ID: {}, quantity: {}, user: {}", savedOrder.getId(), quantity, username);
        return savedOrder;
    }

    private Order createConfirmedOrder(Product product, int quantity, String keycloakId, String email, String username) {
        Order order = new Order();
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setPrice(product.getPrice() * quantity);
        order.setQuantity(quantity);
        order.setStatus("CONFIRMED");
        order.setNotes(String.format("Order confirmed. %d x %s. Remaining stock: %d",
                quantity, product.getName(), product.getStock() - quantity));
        order.setUserKeycloakId(keycloakId);
        order.setUserEmail(email);
        order.setUserName(username);

        Order savedOrder = orderRepository.save(order);
        log.info("✅ CONFIRMED order created with ID: {}, quantity: {}, total: {}, user: {}",
                savedOrder.getId(), quantity, savedOrder.getPrice(), username);
        return savedOrder;
    }

    @Override
    @Transactional
    public Order placeCODOrder(Long productId, int quantity, 
                               String address, String phone, 
                               String city, String pincode,
                               String keycloakId, String email, String username) {
        
        log.info("Placing COD order for product ID: {} with quantity: {}", productId, quantity);

        log.info("COD order placed by user: {} (Keycloak ID: {}, Email: {})",
                username, keycloakId, email);

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        try {
            // 1. First get product details
            Product product = productFeignClient.getProductById(productId);
            log.info("Product received: {}, isFallback: {}", product.getName(), product.isFallback());

            // Check if product service is down
            if (product.isFallback()) {
                log.warn("⚠️ SERVICE DOWN - Creating PENDING order for ID: {}", productId);
                return createPendingOrder(productId, quantity, keycloakId, email, username);
            }

            // Check stock
            log.info("✅ Service available - Processing COD for: {} with stock: {}",
                    product.getName(), product.getStock());

            if (product.getStock() < quantity) {
                throw new OutOfStockException(
                        String.format("Insufficient stock for '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getStock(), quantity));
            }

            // 2. Decrement stock first
            try {
                productFeignClient.decrementStock(productId, quantity);
                log.info("Stock decremented successfully for product: {} by {}", productId, quantity);
            } catch (Exception e) {
                log.error("Failed to decrement stock: {}", e.getMessage());
                throw new ServiceUnavailableException("Failed to update stock. Please try again.");
            }

            // 3. Create order with COD details
            Order order = new Order();
            order.setProductId(product.getId());
            order.setProductName(product.getName());
            order.setPrice(product.getPrice() * quantity);
            order.setQuantity(quantity);
            order.setStatus("CONFIRMED");
            order.setPaymentMethod("COD");
            order.setPaymentStatus("PENDING");
            order.setShippingAddress(address);
            order.setPhoneNumber(phone);
            order.setNotes(String.format("COD Order: %d x %s. Amount to collect: $%.2f",
                    quantity, product.getName(), product.getPrice() * quantity));
            
            order.setUserKeycloakId(keycloakId);
            order.setUserEmail(email);
            order.setUserName(username);

            Order savedOrder = orderRepository.save(order);
            log.info("✅ COD order created with ID: {}, quantity: {}, total: {}",
                    savedOrder.getId(), quantity, savedOrder.getPrice());

            // 4. Call Payment Service to record COD payment
            try {
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(savedOrder.getId());
                paymentRequest.setUserId(null); // We don't have local userId anymore
                paymentRequest.setAmount(savedOrder.getPrice());
                paymentRequest.setPaymentMethod("COD");
                paymentRequest.setShippingAddress(address);
                paymentRequest.setPhoneNumber(phone);
                paymentRequest.setCity(city);
                paymentRequest.setPincode(pincode);

                PaymentResponse paymentResponse = paymentFeignClient.processCOD(paymentRequest);
                
                // Update order with payment ID
                savedOrder.setPaymentId(paymentResponse.getId());
                savedOrder.setPaymentStatus(paymentResponse.getPaymentStatus());
                log.info("✅ Payment recorded with ID: {} for order: {}", paymentResponse.getId(), savedOrder.getId());

                // 👇 5. PUBLISH KAFKA EVENT AFTER PAYMENT RECORDED
                OrderPlacedEvent event = new OrderPlacedEvent(
                    savedOrder.getId(),
                    null, // userId can be null
                    email,
                    username,
                    productId,
                    quantity,
                    savedOrder.getPrice(),
                    "COD",
                    LocalDateTime.now()
                );

                kafkaTemplate.send("order-events", event);
                log.info("📤 Published OrderPlacedEvent to Kafka for order: {}", savedOrder.getId());

                return orderRepository.save(savedOrder);

            } catch (Exception e) {
                log.error("Failed to record payment in payment service: {}", e.getMessage());
                throw new RuntimeException("Failed to create payment record for COD order", e);
            }

        } catch (ProductNotFoundException | OutOfStockException | InvalidQuantityException e) {
            log.error("❌ COD order failed - Business exception: {}", e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("❌ Feign error in COD order: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to process COD order");
        } catch (Exception e) {
            log.error("❌ Unexpected error in COD order: {}", e.getMessage());
            throw new RuntimeException("Failed to place COD order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Order placeOnlineOrder(
            Long productId, 
            int quantity, 
            Double price,  // ✅ ADD THIS - was missing!
            String address, 
            String phone, 
            String city, 
            String pincode,
            String keycloakId, 
            String email, 
            String username) {
        
        log.info("Placing ONLINE order for product ID: {} with quantity: {}, price: {}", 
                 productId, quantity, price);
    
        log.info("ONLINE order placed by user: {} (Keycloak ID: {}, Email: {})",
                username, keycloakId, email);
    
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
    
        try {
            Product product = productFeignClient.getProductById(productId);
            log.info("Product received: {}, isFallback: {}", product.getName(), product.isFallback());
    
            if (product.isFallback()) {
                log.warn("⚠️ SERVICE DOWN - Creating PENDING order for ID: {}", productId);
                return createPendingOrder(productId, quantity, keycloakId, email, username);
            }
    
            if (product.getStock() < quantity) {
                throw new OutOfStockException(
                        String.format("Insufficient stock for '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getStock(), quantity));
            }
    
            // Decrement stock
            try {
                productFeignClient.decrementStock(productId, quantity);
                log.info("Stock decremented successfully for product: {} by {}", productId, quantity);
            } catch (Exception e) {
                log.error("Failed to decrement stock: {}", e.getMessage());
                throw new ServiceUnavailableException("Failed to update stock. Please try again.");
            }
    
            // ✅ USE THE PRICE PARAMETER instead of fetching from product
            // This ensures we use the price from the cart, not from product service
            Order order = new Order();
            order.setProductId(product.getId());
            order.setProductName(product.getName());
            order.setPrice(price * quantity);  // ✅ Use the passed price!
            order.setQuantity(quantity);
            order.setStatus("CONFIRMED");
            order.setPaymentMethod("ONLINE");
            order.setPaymentStatus("PAID");
            order.setShippingAddress(address);
            order.setPhoneNumber(phone);
            order.setNotes(String.format("Online Order: %d x %s. Payment received.",
                    quantity, product.getName()));
            
            order.setUserKeycloakId(keycloakId);
            order.setUserEmail(email);
            order.setUserName(username);
    
            Order savedOrder = orderRepository.save(order);
            log.info("✅ ONLINE order created with ID: {}, quantity: {}, total: {}",
                    savedOrder.getId(), quantity, savedOrder.getPrice());
    
            // 👇 PUBLISH KAFKA EVENT
            OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                null,
                email,
                username,
                productId,
                quantity,
                savedOrder.getPrice(),
                "ONLINE",
                LocalDateTime.now()
            );
    
            kafkaTemplate.send("order-events", event);
            log.info("📤 Published OrderPlacedEvent to Kafka for online order: {}", savedOrder.getId());
    
            return savedOrder;
    
        } catch (ProductNotFoundException | OutOfStockException | InvalidQuantityException e) {
            log.error("❌ ONLINE order failed - Business exception: {}", e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("❌ Feign error in ONLINE order: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to process ONLINE order");
        } catch (Exception e) {
            log.error("❌ Unexpected error in ONLINE order: {}", e.getMessage());
            throw new RuntimeException("Failed to place ONLINE order: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        log.info("Updating order ID: {} to status: {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        
        if ("CONFIRMED".equals(status) && "COD".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("PAID");
            log.info("COD order {} payment status updated to PAID", orderId);
        }
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void softDeleteOrder(Long orderId) {
        log.info("Soft deleting order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setDeleted(true);
        orderRepository.save(order);
        
        log.info("Order {} soft deleted", orderId);
    }
}