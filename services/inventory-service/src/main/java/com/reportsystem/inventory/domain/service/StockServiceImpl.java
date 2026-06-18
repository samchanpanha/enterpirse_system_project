package com.reportsystem.inventory.domain.service;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class StockServiceImpl implements StockService {
    private final StockEntryRepository entryRepo;
    private final StockExitRepository exitRepo;
    private final ProductRepository productRepo;
    public StockServiceImpl(StockEntryRepository entryRepo, StockExitRepository exitRepo, ProductRepository productRepo) {
        this.entryRepo = entryRepo; this.exitRepo = exitRepo; this.productRepo = productRepo;
    }
    @Override public StockEntry addStock(UUID tenantId, UUID branchId, UUID warehouseId, UUID productId, BigDecimal quantity, BigDecimal unitCost, String batchNumber, String notes) {
        return entryRepo.save(StockEntry.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).warehouseId(warehouseId)
                .productId(productId).quantity(quantity).unitCost(unitCost).batchNumber(batchNumber).notes(notes)
                .createdAt(Instant.now()).build());
    }
    @Override public StockExit removeStock(UUID tenantId, UUID branchId, UUID warehouseId, UUID productId, BigDecimal quantity, String referenceType, UUID referenceId, String notes) {
        return exitRepo.save(StockExit.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).warehouseId(warehouseId)
                .productId(productId).quantity(quantity).referenceType(referenceType).referenceId(referenceId)
                .notes(notes).createdAt(Instant.now()).build());
    }
    @Override public BigDecimal getCurrentStock(UUID tenantId, UUID productId) {
        BigDecimal in = entryRepo.findByProductIdAndWarehouseId(productId, null).stream()
                .map(StockEntry::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal out = exitRepo.findByProductIdAndWarehouseId(productId, null).stream()
                .map(StockExit::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        return in.subtract(out);
    }
}