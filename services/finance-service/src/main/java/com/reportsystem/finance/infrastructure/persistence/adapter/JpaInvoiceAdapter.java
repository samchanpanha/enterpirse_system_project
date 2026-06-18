package com.reportsystem.finance.infrastructure.persistence.adapter;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.outbound.*;
import com.reportsystem.finance.infrastructure.persistence.entity.*;
import com.reportsystem.finance.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaInvoiceAdapter implements InvoiceRepository {
    private final JpaInvoiceRepository repo;
    public JpaInvoiceAdapter(JpaInvoiceRepository r) { repo = r; }
    @Override public Invoice save(Invoice i) {
        InvoiceEntity e = new InvoiceEntity(); e.setId(i.getId()); e.setTenantId(i.getTenantId()); e.setBranchId(i.getBranchId()); e.setInvoiceNumber(i.getInvoiceNumber());
        e.setInvoiceType(i.getInvoiceType()); e.setSourceType(i.getSourceType()); e.setSourceId(i.getSourceId());
        e.setCustomerName(i.getCustomerName()); e.setCustomerTin(i.getCustomerTin()); e.setIssueDate(i.getIssueDate());
        e.setDueDate(i.getDueDate()); e.setSubtotal(i.getSubtotal()); e.setDiscount(i.getDiscount()); e.setTaxAmount(i.getTaxAmount());
        e.setTotal(i.getTotal()); e.setAmountPaid(i.getAmountPaid()); e.setBalanceDue(i.getBalanceDue());
        e.setStatus(i.getStatus()); e.setCurrency(i.getCurrency()); e.setNotes(i.getNotes()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<Invoice> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Invoice> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<Invoice> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    @Override public String generateInvoiceNumber(UUID tenantId) { return "INV-" + tenantId.toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis(); }
    private Invoice toDomain(InvoiceEntity e) { return Invoice.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).invoiceNumber(e.getInvoiceNumber()).invoiceType(e.getInvoiceType()).sourceType(e.getSourceType()).sourceId(e.getSourceId()).customerName(e.getCustomerName()).customerTin(e.getCustomerTin()).issueDate(e.getIssueDate()).dueDate(e.getDueDate()).subtotal(e.getSubtotal()).discount(e.getDiscount()).taxAmount(e.getTaxAmount()).total(e.getTotal()).amountPaid(e.getAmountPaid()).balanceDue(e.getBalanceDue()).status(e.getStatus()).currency(e.getCurrency()).notes(e.getNotes()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}