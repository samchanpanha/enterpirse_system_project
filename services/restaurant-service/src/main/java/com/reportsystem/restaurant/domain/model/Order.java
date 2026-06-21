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
public class Order {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID outletId;
    private final UUID tableId;
    private final UUID customerId;
    private String orderNumber;
    private String type;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private String discountType;
    private BigDecimal taxAmount;
    private BigDecimal serviceCharge;
    private BigDecimal total;
    private String paymentStatus;
    private String paymentMethod;
    private String notes;
    private UUID servedBy;
    private final Instant createdAt;
    private Instant completedAt;
}
