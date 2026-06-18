package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockEntryRepository {
    StockEntry save(StockEntry e);
    List<StockEntry> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);
}
