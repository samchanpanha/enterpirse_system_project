package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.StockTransfer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockTransferRepository {
    StockTransfer save(StockTransfer transfer);
    Optional<StockTransfer> findById(UUID id);
    List<StockTransfer> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<StockTransfer> findByTenantIdAndStatus(UUID tenantId, String status);
    List<StockTransfer> findByTenantIdAndBranch(UUID tenantId, UUID branchId);
    List<StockTransfer> findIncomingForBranch(UUID branchId);
    Optional<StockTransfer> findByTenantIdAndTransferNumber(UUID tenantId, String transferNumber);
}
