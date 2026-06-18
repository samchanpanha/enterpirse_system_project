package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class StockEntry {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID warehouseId;
    private UUID productId;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String batchNumber;
    private LocalDate expiryDate;
    private String referenceType;
    private UUID referenceId;
    private String notes;
    private UUID createdBy;
    private final Instant createdAt;
}
