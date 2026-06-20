package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaStockEntryAdapter implements StockEntryRepository {
    private final JpaStockEntryRepository repo;
    public JpaStockEntryAdapter(JpaStockEntryRepository repo) { this.repo = repo; }
    @Override public StockEntry save(StockEntry e) {
        StockEntryEntity en = new StockEntryEntity(); en.setId(e.getId()); en.setTenantId(e.getTenantId()); en.setBranchId(e.getBranchId()); en.setWarehouseId(e.getWarehouseId());
        en.setProductId(e.getProductId()); en.setQuantity(e.getQuantity()); en.setUnitCost(e.getUnitCost()); en.setBatchNumber(e.getBatchNumber());
        en.setExpiryDate(e.getExpiryDate()); en.setReferenceType(e.getReferenceType()); en.setReferenceId(e.getReferenceId());
        en.setNotes(e.getNotes()); en.setCreatedBy(e.getCreatedBy()); en.setCreatedAt(Instant.now());
        return toDomain(repo.save(en));
    }
    @Override public List<StockEntry> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        return (warehouseId != null ? repo.findByProductIdAndWarehouseId(productId, warehouseId) : repo.findByProductIdAndWarehouseId(productId, null))
                .stream().map(this::toDomain).toList();
    }
    @Override public List<StockEntry> findByTenantIdAndBranchIdAndProductId(UUID tenantId, UUID branchId, UUID productId) {
        return repo.findByTenantIdAndBranchIdAndProductId(tenantId, branchId, productId).stream().map(this::toDomain).toList();
    }
    private StockEntry toDomain(StockEntryEntity e) { return StockEntry.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).warehouseId(e.getWarehouseId()).productId(e.getProductId()).quantity(e.getQuantity()).unitCost(e.getUnitCost()).batchNumber(e.getBatchNumber()).expiryDate(e.getExpiryDate()).referenceType(e.getReferenceType()).referenceId(e.getReferenceId()).notes(e.getNotes()).createdBy(e.getCreatedBy()).createdAt(e.getCreatedAt()).build(); }
}