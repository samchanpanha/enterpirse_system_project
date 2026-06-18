package com.reportsystem.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://auth-service"))
            .route("branches-service", r -> r
                .path("/api/branches/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://auth-service"))
            .route("user-branches-service", r -> r
                .path("/api/user-branches/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://auth-service"))
            .route("features-service", r -> r
                .path("/api/features/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://auth-service"))
            .route("admin-service", r -> r
                .path("/api/admin/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://auth-service"))
            .route("property-service", r -> r
                .path("/api/property/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://property-service"))
            .route("restaurant-service", r -> r
                .path("/api/restaurant/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://restaurant-service"))
            .route("inventory-service", r -> r
                .path("/api/inventory/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://inventory-service"))
            .route("inventory-transfers-service", r -> r
                .path("/api/inventory/transfers/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://inventory-service"))
            .route("finance-service", r -> r
                .path("/api/finance/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://finance-service"))
            .route("payment-service", r -> r
                .path("/api/payment/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://payment-service"))
            .route("reporting-service", r -> r
                .path("/api/reporting/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://reporting-service"))
            .build();
    }
}
