package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaPurchaseOrderAdapter implements PurchaseOrderRepository {
    private final JpaPurchaseOrderRepository repo;
    public JpaPurchaseOrderAdapter(JpaPurchaseOrderRepository repo) { this.repo = repo; }
    @Override public PurchaseOrder save(PurchaseOrder po) {
        PurchaseOrderEntity e = new PurchaseOrderEntity(); e.setId(po.getId()); e.setTenantId(po.getTenantId()); e.setBranchId(po.getBranchId()); e.setSupplierId(po.getSupplierId());
        e.setPoNumber(po.getPoNumber()); e.setStatus(po.getStatus()); e.setOrderDate(po.getOrderDate()); e.setExpectedDate(po.getExpectedDate());
        e.setReceivedDate(po.getReceivedDate()); e.setSubtotal(po.getSubtotal()); e.setTaxAmount(po.getTaxAmount()); e.setShippingCost(po.getShippingCost());
        e.setTotal(po.getTotal()); e.setCurrency(po.getCurrency()); e.setNotes(po.getNotes()); e.setCreatedBy(po.getCreatedBy()); e.setApprovedBy(po.getApprovedBy());
        e.setCreatedAt(po.getCreatedAt()); e.setUpdatedAt(po.getUpdatedAt());
        return toDomain(repo.save(e));
    }
    @Override public Optional<PurchaseOrder> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<PurchaseOrder> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<PurchaseOrder> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    @Override public List<PurchaseOrder> findBySupplierId(UUID s) { return repo.findBySupplierId(s).stream().map(this::toDomain).toList(); }
    @Override public String generatePoNumber(UUID tenantId) { return "PO-" + tenantId.toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis(); }
    private PurchaseOrder toDomain(PurchaseOrderEntity e) { return PurchaseOrder.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).supplierId(e.getSupplierId()).poNumber(e.getPoNumber()).status(e.getStatus()).orderDate(e.getOrderDate()).expectedDate(e.getExpectedDate()).receivedDate(e.getReceivedDate()).subtotal(e.getSubtotal()).taxAmount(e.getTaxAmount()).shippingCost(e.getShippingCost()).total(e.getTotal()).currency(e.getCurrency()).notes(e.getNotes()).createdBy(e.getCreatedBy()).approvedBy(e.getApprovedBy()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}