package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaPurchaseOrderItemAdapter implements PurchaseOrderItemRepository {
    private final JpaPurchaseOrderItemRepository repo;
    public JpaPurchaseOrderItemAdapter(JpaPurchaseOrderItemRepository repo) { this.repo = repo; }
    @Override public PurchaseOrderItem save(PurchaseOrderItem item) {
        PurchaseOrderItemEntity e = new PurchaseOrderItemEntity(); e.setId(item.getId()); e.setPurchaseOrderId(item.getPurchaseOrderId());
        e.setProductId(item.getProductId()); e.setQuantityOrdered(item.getQuantityOrdered()); e.setQuantityReceived(item.getQuantityReceived());
        e.setUnitCost(item.getUnitCost()); e.setSubtotal(item.getSubtotal()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }
    @Override public List<PurchaseOrderItem> findByPurchaseOrderId(UUID poId) { return repo.findByPurchaseOrderId(poId).stream().map(this::toDomain).toList(); }
    private PurchaseOrderItem toDomain(PurchaseOrderItemEntity e) { return PurchaseOrderItem.builder().id(e.getId()).purchaseOrderId(e.getPurchaseOrderId()).productId(e.getProductId()).quantityOrdered(e.getQuantityOrdered()).quantityReceived(e.getQuantityReceived()).unitCost(e.getUnitCost()).subtotal(e.getSubtotal()).createdAt(e.getCreatedAt()).build(); }
}