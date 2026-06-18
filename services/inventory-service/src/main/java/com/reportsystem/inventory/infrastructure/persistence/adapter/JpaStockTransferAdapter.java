package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.StockTransfer;
import com.reportsystem.inventory.domain.model.StockTransferItem;
import com.reportsystem.inventory.domain.port.outbound.StockTransferRepository;
import com.reportsystem.inventory.infrastructure.persistence.entity.StockTransferEntity;
import com.reportsystem.inventory.infrastructure.persistence.entity.StockTransferItemEntity;
import com.reportsystem.inventory.infrastructure.persistence.repository.JpaStockTransferItemRepository;
import com.reportsystem.inventory.infrastructure.persistence.repository.JpaStockTransferRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JpaStockTransferAdapter implements StockTransferRepository {

    private final JpaStockTransferRepository jpa;
    private final JpaStockTransferItemRepository jpaItem;

    public JpaStockTransferAdapter(JpaStockTransferRepository jpa, JpaStockTransferItemRepository jpaItem) {
        this.jpa = jpa;
        this.jpaItem = jpaItem;
    }

    @Override
    @Transactional
    public StockTransfer save(StockTransfer transfer) {
        StockTransferEntity entity = toEntity(transfer);
        // Items reference the parent via FK. Save the parent first to ensure the FK
        // target exists; then save items with the parent's ID.
        StockTransferEntity saved = jpa.save(entity);
        if (transfer.getItems() != null && !transfer.getItems().isEmpty()) {
            // For new transfers, replace the item IDs with new ones and link to parent
            List<StockTransferItemEntity> itemEntities = new ArrayList<>();
            int order = 0;
            for (StockTransferItem item : transfer.getItems()) {
                StockTransferItemEntity ie = StockTransferItemEntity.builder()
                    .id(item.getId() != null ? item.getId() : UUID.randomUUID())
                    .transferId(saved.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .receivedQuantity(item.getReceivedQuantity() != null ? item.getReceivedQuantity() : java.math.BigDecimal.ZERO)
                    .unitCost(item.getUnitCost() != null ? item.getUnitCost() : java.math.BigDecimal.ZERO)
                    .discrepancyNotes(item.getDiscrepancyNotes())
                    .lineOrder(order++)
                    .createdAt(item.getCreatedAt() != null ? item.getCreatedAt() : java.time.Instant.now())
                    .build();
                itemEntities.add(ie);
            }
            jpaItem.saveAll(itemEntities);
        }
        return populateItems(toDomain(saved));
    }

    @Override
    public Optional<StockTransfer> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain).map(this::populateItems);
    }

    @Override
    public List<StockTransfer> findByTenantIdOrderByCreatedAtDesc(UUID tenantId) {
        return jpa.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
            .map(this::toDomain).map(this::populateItems).collect(Collectors.toList());
    }

    @Override
    public List<StockTransfer> findByTenantIdAndStatus(UUID tenantId, String status) {
        return jpa.findByTenantIdAndStatus(tenantId, status).stream()
            .map(this::toDomain).map(this::populateItems).collect(Collectors.toList());
    }

    @Override
    public List<StockTransfer> findByTenantIdAndBranch(UUID tenantId, UUID branchId) {
        return jpa.findByTenantIdAndBranch(tenantId, branchId).stream()
            .map(this::toDomain).map(this::populateItems).collect(Collectors.toList());
    }

    @Override
    public List<StockTransfer> findIncomingForBranch(UUID branchId) {
        return jpa.findIncomingForBranch(branchId).stream()
            .map(this::toDomain).map(this::populateItems).collect(Collectors.toList());
    }

    @Override
    public Optional<StockTransfer> findByTenantIdAndTransferNumber(UUID tenantId, String transferNumber) {
        return jpa.findByTenantIdAndTransferNumber(tenantId, transferNumber).map(this::toDomain).map(this::populateItems);
    }

    private StockTransfer populateItems(StockTransfer t) {
        if (t.getId() == null) return t;
        List<StockTransferItem> items = jpaItem.findByTransferIdOrderByLineOrder(t.getId()).stream()
            .map(this::toItemDomain)
            .toList();
        t.setItems(items);
        return t;
    }

    private StockTransferItem toItemDomain(StockTransferItemEntity ie) {
        return StockTransferItem.builder()
            .id(ie.getId())
            .transferId(ie.getTransferId())
            .productId(ie.getProductId())
            .quantity(ie.getQuantity())
            .receivedQuantity(ie.getReceivedQuantity())
            .unitCost(ie.getUnitCost())
            .discrepancyNotes(ie.getDiscrepancyNotes())
            .lineOrder(ie.getLineOrder())
            .createdAt(ie.getCreatedAt())
            .build();
    }

    private StockTransferEntity toEntity(StockTransfer t) {
        return StockTransferEntity.builder()
            .id(t.getId())
            .tenantId(t.getTenantId())
            .transferNumber(t.getTransferNumber())
            .fromBranchId(t.getFromBranchId())
            .toBranchId(t.getToBranchId())
            .fromWarehouseId(t.getFromWarehouseId())
            .toWarehouseId(t.getToWarehouseId())
            .status(t.getStatus())
            .notes(t.getNotes())
            .createdBy(t.getCreatedBy())
            .approvedBy(t.getApprovedBy())
            .shippedBy(t.getShippedBy())
            .receivedBy(t.getReceivedBy())
            .shippedAt(t.getShippedAt())
            .receivedAt(t.getReceivedAt())
            .cancelledAt(t.getCancelledAt())
            .cancelReason(t.getCancelReason())
            .totalItems(t.getTotalItems())
            .totalValue(t.getTotalValue())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }

    private StockTransfer toDomain(StockTransferEntity e) {
        return StockTransfer.builder()
            .id(e.getId())
            .tenantId(e.getTenantId())
            .transferNumber(e.getTransferNumber())
            .fromBranchId(e.getFromBranchId())
            .toBranchId(e.getToBranchId())
            .fromWarehouseId(e.getFromWarehouseId())
            .toWarehouseId(e.getToWarehouseId())
            .status(e.getStatus())
            .notes(e.getNotes())
            .createdBy(e.getCreatedBy())
            .approvedBy(e.getApprovedBy())
            .shippedBy(e.getShippedBy())
            .receivedBy(e.getReceivedBy())
            .shippedAt(e.getShippedAt())
            .receivedAt(e.getReceivedAt())
            .cancelledAt(e.getCancelledAt())
            .cancelReason(e.getCancelReason())
            .totalItems(e.getTotalItems())
            .totalValue(e.getTotalValue())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .items(new ArrayList<>())
            .build();
    }
}
