package com.reportsystem.finance.domain.service;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import com.reportsystem.finance.domain.port.outbound.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invRepo;
    public InvoiceServiceImpl(InvoiceRepository invRepo) { this.invRepo = invRepo; }

    @Override public Invoice createInvoice(UUID tenantId, UUID branchId, String invoiceType, String customerName, LocalDate issueDate, LocalDate dueDate, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal total) {
        return invRepo.save(Invoice.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .invoiceNumber(invRepo.generateInvoiceNumber(tenantId)).invoiceType(invoiceType)
                .customerName(customerName).issueDate(issueDate).dueDate(dueDate)
                .subtotal(subtotal).taxAmount(taxAmount).total(total).amountPaid(BigDecimal.ZERO).balanceDue(total)
                .status("pending").currency("USD").createdAt(Instant.now()).build());
    }
    @Override public Optional<Invoice> getInvoiceById(UUID id) { return invRepo.findById(id); }
    @Override public List<Invoice> getInvoicesByTenant(UUID tenantId) { return invRepo.findByTenantId(tenantId); }
    @Override public List<Invoice> getInvoicesByTenantAndBranch(UUID tenantId, UUID branchId) { return invRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public Invoice recordPayment(UUID id, BigDecimal amount) {
        Invoice inv = invRepo.findById(id).orElseThrow();
        BigDecimal newPaid = inv.getAmountPaid().add(amount);
        BigDecimal newBalance = inv.getTotal().subtract(newPaid);
        String status = newBalance.compareTo(BigDecimal.ZERO) <= 0 ? "paid" : "partial";
        return invRepo.save(Invoice.builder().id(inv.getId()).tenantId(inv.getTenantId()).branchId(inv.getBranchId()).invoiceNumber(inv.getInvoiceNumber())
                .invoiceType(inv.getInvoiceType()).customerName(inv.getCustomerName()).issueDate(inv.getIssueDate())
                .dueDate(inv.getDueDate()).subtotal(inv.getSubtotal()).taxAmount(inv.getTaxAmount()).total(inv.getTotal())
                .amountPaid(newPaid).balanceDue(newBalance).status(status).currency(inv.getCurrency()).createdAt(inv.getCreatedAt()).build());
    }
}