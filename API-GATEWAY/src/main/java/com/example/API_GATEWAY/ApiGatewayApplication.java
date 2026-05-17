package com.example.API_GATEWAY;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${PRODUCT_SERVICE_URL}")
    private String productServiceUrl;

    @Value("${ORDER_SERVICE_URL}")
    private String orderServiceUrl;

    @Value("${PAYMENT_SERVICE_URL}")
    private String paymentServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            // AUTH SERVICE
            .route("auth_api_route", r -> r
                .path("/api/auth/**")
                .uri(authServiceUrl))

            .route("auth_route", r -> r
                .path("/auth/**")
                .uri(authServiceUrl))

            .route("admin_route", r -> r
                .path("/admin/**")
                .uri(authServiceUrl))

            .route("address_route", r -> r
                .path("/api/addresses/**")
                .uri(authServiceUrl))

            .route("user_route", r -> r
                .path("/api/users/**")
                .uri(authServiceUrl))

            // PRODUCT SERVICE
            .route("product_route", r -> r
                .path("/products/**")
                .uri(productServiceUrl))

            .route("category_route", r -> r
                .path("/categories/**", "/api/categories/**")
                .uri(productServiceUrl))

            // ORDER SERVICE
            .route("order_route", r -> r
                .path("/orders/**")
                .uri(orderServiceUrl))

            // PAYMENT SERVICE
            .route("payment_route", r -> r
                .path("/api/payments/**", "/api/stripe/**")
                .uri(paymentServiceUrl))

            .route("upi_route", r -> r
                .path("/api/payments/upi/**")
                .uri(paymentServiceUrl))

            .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}