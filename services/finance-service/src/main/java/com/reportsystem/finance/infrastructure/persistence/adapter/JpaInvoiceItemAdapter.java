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


public class JpaInvoiceItemAdapter implements InvoiceItemRepository {
    private final JpaInvoiceItemRepository repo;
    public JpaInvoiceItemAdapter(JpaInvoiceItemRepository r) { repo = r; }
    @Override public InvoiceItem save(InvoiceItem i) { InvoiceItemEntity e = new InvoiceItemEntity(); e.setId(i.getId()); e.setInvoiceId(i.getInvoiceId()); e.setDescription(i.getDescription()); e.setQuantity(i.getQuantity()); e.setUnitPrice(i.getUnitPrice()); e.setTaxRate(i.getTaxRate()); e.setTaxAmount(i.getTaxAmount()); e.setTotal(i.getTotal()); e.setAccountId(i.getAccountId()); return toDomain(repo.save(e)); }
    @Override public List<InvoiceItem> findByInvoiceId(UUID id) { return repo.findByInvoiceId(id).stream().map(this::toDomain).toList(); }
    private InvoiceItem toDomain(InvoiceItemEntity e) { return InvoiceItem.builder().id(e.getId()).invoiceId(e.getInvoiceId()).description(e.getDescription()).quantity(e.getQuantity()).unitPrice(e.getUnitPrice()).taxRate(e.getTaxRate()).taxAmount(e.getTaxAmount()).total(e.getTotal()).accountId(e.getAccountId()).build(); }
}