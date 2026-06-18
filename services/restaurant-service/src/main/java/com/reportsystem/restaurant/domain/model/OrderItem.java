package com.reportsystem.restaurant.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItem {
    private final UUID id;
    private final UUID orderId;
    private final UUID menuItemId;
    private int quantity;
    private BigDecimal unitPrice;
    private String modifiers;
    private BigDecimal subtotal;
    private String status;
    private String notes;
    private final Instant createdAt;
}
