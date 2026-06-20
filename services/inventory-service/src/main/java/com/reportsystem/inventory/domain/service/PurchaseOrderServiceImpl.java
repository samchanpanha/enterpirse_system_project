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


public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository poiRepo;
    private final StockEntryRepository entryRepo;
    public PurchaseOrderServiceImpl(PurchaseOrderRepository poRepo, PurchaseOrderItemRepository poiRepo, StockEntryRepository entryRepo) {
        this.poRepo = poRepo; this.poiRepo = poiRepo; this.entryRepo = entryRepo;
    }
    @Override public PurchaseOrder createPO(UUID tenantId, UUID branchId, UUID supplierId, LocalDate orderDate) {
        return poRepo.save(PurchaseOrder.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).supplierId(supplierId)
                .poNumber(poRepo.generatePoNumber(tenantId)).status("draft").orderDate(orderDate).currency("USD")
                .subtotal(BigDecimal.ZERO).total(BigDecimal.ZERO).createdAt(Instant.now()).build());
    }
    @Override public Optional<PurchaseOrder> getPOById(UUID id) { return poRepo.findById(id); }
    @Override public List<PurchaseOrder> getPOsByTenant(UUID tenantId) { return poRepo.findByTenantId(tenantId); }
    @Override public List<PurchaseOrder> getPOsByTenantAndBranch(UUID tenantId, UUID branchId) { return poRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public PurchaseOrder addItem(UUID poId, UUID productId, BigDecimal qty, BigDecimal unitCost) {
        PurchaseOrder po = poRepo.findById(poId).orElseThrow();
        BigDecimal subtotal = qty.multiply(unitCost);
        poiRepo.save(PurchaseOrderItem.builder().id(UUID.randomUUID()).purchaseOrderId(poId).productId(productId)
                .quantityOrdered(qty).quantityReceived(BigDecimal.ZERO).unitCost(unitCost).subtotal(subtotal).createdAt(Instant.now()).build());
        return poRepo.save(PurchaseOrder.builder().id(po.getId()).tenantId(po.getTenantId()).branchId(po.getBranchId()).supplierId(po.getSupplierId())
                .poNumber(po.getPoNumber()).status(po.getStatus()).orderDate(po.getOrderDate()).currency(po.getCurrency())
                .subtotal(po.getSubtotal().add(subtotal)).total(po.getTotal().add(subtotal)).createdAt(po.getCreatedAt()).build());
    }
    @Override public PurchaseOrder receiveItems(UUID poId) {
        PurchaseOrder po = poRepo.findById(poId).orElseThrow();
        List<PurchaseOrderItem> items = poiRepo.findByPurchaseOrderId(poId);
        for (PurchaseOrderItem item : items) {
            entryRepo.save(StockEntry.builder().id(UUID.randomUUID()).tenantId(po.getTenantId()).branchId(po.getBranchId()).productId(item.getProductId())
                    .quantity(item.getQuantityOrdered()).unitCost(item.getUnitCost()).referenceType("purchase_order")
                    .referenceId(poId).createdAt(Instant.now()).build());
        }
        return poRepo.save(PurchaseOrder.builder().id(po.getId()).tenantId(po.getTenantId()).branchId(po.getBranchId()).supplierId(po.getSupplierId())
                .poNumber(po.getPoNumber()).status("received").receivedDate(LocalDate.now())
                .orderDate(po.getOrderDate()).currency(po.getCurrency()).subtotal(po.getSubtotal()).total(po.getTotal())
                .createdAt(po.getCreatedAt()).build());
    }
    @Override public PurchaseOrder cancelPO(UUID id) {
        PurchaseOrder po = poRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("PO not found: " + id));
        return poRepo.save(PurchaseOrder.builder().id(po.getId()).tenantId(po.getTenantId()).branchId(po.getBranchId()).supplierId(po.getSupplierId())
                .poNumber(po.getPoNumber()).status("cancelled").orderDate(po.getOrderDate()).currency(po.getCurrency())
                .subtotal(po.getSubtotal()).total(po.getTotal()).createdAt(po.getCreatedAt()).build());
    }
}