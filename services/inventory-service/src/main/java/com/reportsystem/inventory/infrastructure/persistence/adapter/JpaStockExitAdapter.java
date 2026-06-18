package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaStockExitAdapter implements StockExitRepository {
    private final JpaStockExitRepository repo;
    public JpaStockExitAdapter(JpaStockExitRepository repo) { this.repo = repo; }
    @Override public StockExit save(StockExit e) {
        StockExitEntity en = new StockExitEntity(); en.setId(e.getId()); en.setTenantId(e.getTenantId()); en.setBranchId(e.getBranchId()); en.setWarehouseId(e.getWarehouseId());
        en.setProductId(e.getProductId()); en.setQuantity(e.getQuantity()); en.setReferenceType(e.getReferenceType());
        en.setReferenceId(e.getReferenceId()); en.setNotes(e.getNotes()); en.setCreatedBy(e.getCreatedBy()); en.setCreatedAt(Instant.now());
        return toDomain(repo.save(en));
    }
    @Override public List<StockExit> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        return (warehouseId != null ? repo.findByProductIdAndWarehouseId(productId, warehouseId) : repo.findByProductIdAndWarehouseId(productId, null))
                .stream().map(this::toDomain).toList();
    }
    private StockExit toDomain(StockExitEntity e) { return StockExit.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).warehouseId(e.getWarehouseId()).productId(e.getProductId()).quantity(e.getQuantity()).referenceType(e.getReferenceType()).referenceId(e.getReferenceId()).notes(e.getNotes()).createdBy(e.getCreatedBy()).createdAt(e.getCreatedAt()).build(); }
}