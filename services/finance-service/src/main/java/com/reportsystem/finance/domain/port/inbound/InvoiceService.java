package com.reportsystem.finance.domain.port.inbound;

import com.reportsystem.finance.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceService {
    Invoice createInvoice(UUID tenantId, UUID branchId, String invoiceType, String customerName, LocalDate issueDate, LocalDate dueDate, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal total);
    Invoice createInvoiceWithItems(UUID tenantId, UUID branchId, String invoiceType, String customerName, LocalDate issueDate, LocalDate dueDate, List<InvoiceItem> items);
    Optional<Invoice> getInvoiceById(UUID id);
    List<Invoice> getInvoicesByTenant(UUID tenantId);
    List<Invoice> getInvoicesByTenantAndBranch(UUID tenantId, UUID branchId);
    Invoice recordPayment(UUID id, BigDecimal amount);
}
