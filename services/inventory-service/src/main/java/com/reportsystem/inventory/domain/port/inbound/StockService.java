package com.reportsystem.inventory.domain.port.inbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockService {
    StockEntry addStock(UUID tenantId, UUID branchId, UUID warehouseId, UUID productId, java.math.BigDecimal quantity, java.math.BigDecimal unitCost, String batchNumber, String notes);
    StockExit removeStock(UUID tenantId, UUID branchId, UUID warehouseId, UUID productId, java.math.BigDecimal quantity, String referenceType, UUID referenceId, String notes);
    java.math.BigDecimal getCurrentStock(UUID tenantId, UUID productId);
}
