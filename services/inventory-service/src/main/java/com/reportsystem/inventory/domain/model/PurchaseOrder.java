package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class PurchaseOrder {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID supplierId;
    private String poNumber;
    private String status;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDate receivedDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String currency;
    private String notes;
    private UUID createdBy;
    private UUID approvedBy;
    private final Instant createdAt;
    private Instant updatedAt;
}
