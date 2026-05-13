package com.example.API_GATEWAY;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {


	// ✅ ADD THIS METHOD
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
            .route("auth_api_route", r -> r  // ✅ NEW: for /api/auth/**
                .path("/api/auth/**")
                .uri("lb://AUTH-SERVICE"))
            .route("auth_route", r -> r
                .path("/auth/**")
                .uri("lb://AUTH-SERVICE"))
            .route("admin_route", r -> r
                .path("/admin/**")
                .uri("lb://AUTH-SERVICE"))
            .route("address_route", r -> r  // ✅ ADD THIS
                .path("/api/addresses/**")
                .uri("lb://AUTH-SERVICE"))
            .route("user_route", r -> r  // ✅ ADD THIS
                .path("/api/users/**")
                .uri("lb://AUTH-SERVICE"))
            .route("product_route", r -> r
                .path("/products/**")
                .uri("lb://PRODUCT-SERVICE"))
            .route("order_route", r -> r
                .path("/orders/**")
                .uri("lb://ORDER-SERVICE"))
            .route("category_route", r -> r
                .path("/categories/**", "/api/categories/**")
                .uri("lb://PRODUCT-SERVICE"))
            // ✅ ADD PAYMENT SERVICE ROUTE
            .route("payment_route", r -> r
                .path("/api/payments/**", "/api/stripe/**")
                .uri("lb://PAYMENT-SERVICE"))
              // ✅ UPI Payment route (NEW)
            .route("upi_route", r -> r
              .path("/api/payments/upi/**")
              .uri("lb://PAYMENT-SERVICE"))
            .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
