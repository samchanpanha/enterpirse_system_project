package com.reportsystem.payment.infrastructure.persistence.adapter;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.outbound.*;
import com.reportsystem.payment.infrastructure.persistence.entity.*;
import com.reportsystem.payment.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaTransactionAdapter implements TransactionRepository {
    private final JpaTransactionRepository repo;
    public JpaTransactionAdapter(JpaTransactionRepository r) { repo = r; }
    @Override public Transaction save(Transaction t) {
        TransactionEntity e = new TransactionEntity(); e.setId(t.getId()); e.setTenantId(t.getTenantId()); e.setBranchId(t.getBranchId());
        e.setTransactionId(t.getTransactionId()); e.setInvoiceId(t.getInvoiceId()); e.setAmount(t.getAmount());
        e.setCurrency(t.getCurrency()); e.setGateway(t.getGateway()); e.setGatewayRef(t.getGatewayRef());
        e.setMethod(t.getMethod()); e.setStatus(t.getStatus()); e.setCustomerName(t.getCustomerName());
        e.setCustomerPhone(t.getCustomerPhone()); e.setSourceType(t.getSourceType()); e.setSourceId(t.getSourceId());
        e.setMetadata(t.getMetadata() != null ? t.getMetadata() : "{}"); e.setPaidAt(t.getPaidAt());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<Transaction> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Transaction> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<Transaction> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    @Override public List<Transaction> findByInvoiceId(UUID id) { return repo.findByInvoiceId(id).stream().map(this::toDomain).toList(); }
    @Override public String generateTransactionId(UUID tenantId) { return "TXN-" + tenantId.toString().substring(0,8).toUpperCase() + "-" + System.currentTimeMillis(); }
    private Transaction toDomain(TransactionEntity e) { return Transaction.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).transactionId(e.getTransactionId()).invoiceId(e.getInvoiceId()).amount(e.getAmount()).currency(e.getCurrency()).gateway(e.getGateway()).gatewayRef(e.getGatewayRef()).method(e.getMethod()).status(e.getStatus()).customerName(e.getCustomerName()).customerPhone(e.getCustomerPhone()).sourceType(e.getSourceType()).sourceId(e.getSourceId()).metadata(e.getMetadata()).paidAt(e.getPaidAt()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}