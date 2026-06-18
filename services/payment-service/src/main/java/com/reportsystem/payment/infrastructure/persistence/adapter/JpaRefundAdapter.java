package com.reportsystem.payment.infrastructure.persistence.adapter;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.outbound.*;
import com.reportsystem.payment.infrastructure.persistence.entity.*;
import com.reportsystem.payment.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaRefundAdapter implements RefundRepository {
    private final JpaRefundRepository repo;
    public JpaRefundAdapter(JpaRefundRepository r) { repo = r; }
    @Override public Refund save(Refund rf) {
        RefundEntity e = new RefundEntity(); e.setId(rf.getId()); e.setTenantId(rf.getTenantId()); e.setBranchId(rf.getBranchId());
        e.setTransactionId(rf.getTransactionId()); e.setAmount(rf.getAmount()); e.setReason(rf.getReason());
        e.setGatewayRef(rf.getGatewayRef()); e.setStatus(rf.getStatus()); e.setProcessedAt(rf.getProcessedAt());
        e.setCreatedBy(rf.getCreatedBy()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public List<Refund> findByTransactionId(UUID id) { return repo.findByTransactionId(id).stream().map(this::toDomain).toList(); }
    private Refund toDomain(RefundEntity e) { return Refund.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).transactionId(e.getTransactionId()).amount(e.getAmount()).reason(e.getReason()).gatewayRef(e.getGatewayRef()).status(e.getStatus()).processedAt(e.getProcessedAt()).createdBy(e.getCreatedBy()).createdAt(e.getCreatedAt()).build(); }
}