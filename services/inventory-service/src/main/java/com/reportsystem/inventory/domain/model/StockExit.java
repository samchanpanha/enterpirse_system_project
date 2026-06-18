package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class StockExit {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID warehouseId;
    private UUID productId;
    private BigDecimal quantity;
    private String referenceType;
    private UUID referenceId;
    private String notes;
    private UUID createdBy;
    private final Instant createdAt;
}
