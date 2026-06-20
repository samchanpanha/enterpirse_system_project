package com.reportsystem.finance.infrastructure.web;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/invoices")
public class InvoiceController {
    private final InvoiceService invService;
    public InvoiceController(InvoiceService i) { invService = i; }
    @PostMapping public ResponseEntity<Invoice> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(invService.createInvoice(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("invoiceType"), (String)b.get("customerName"),
                java.time.LocalDate.parse((String)b.get("issueDate")),
                java.time.LocalDate.parse((String)b.get("dueDate")),
                new java.math.BigDecimal(b.get("subtotal").toString()),
                b.get("taxAmount") != null ? new java.math.BigDecimal(b.get("taxAmount").toString()) : java.math.BigDecimal.ZERO,
                new java.math.BigDecimal(b.get("total").toString())));
    }
    @PostMapping("/with-items") public ResponseEntity<Invoice> createWithItems(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) b.get("items");
        List<InvoiceItem> items = new ArrayList<>();
        if (rawItems != null) {
            for (Map<String, Object> raw : rawItems) {
                BigDecimal quantity = new java.math.BigDecimal(raw.get("quantity").toString());
                BigDecimal unitPrice = new java.math.BigDecimal(raw.get("unitPrice").toString());
                BigDecimal total = raw.get("total") != null ? new java.math.BigDecimal(raw.get("total").toString()) : quantity.multiply(unitPrice);
                BigDecimal taxRate = raw.get("taxRate") != null ? new java.math.BigDecimal(raw.get("taxRate").toString()) : java.math.BigDecimal.ZERO;
                BigDecimal taxAmount = raw.get("taxAmount") != null ? new java.math.BigDecimal(raw.get("taxAmount").toString()) : total.multiply(taxRate);
                items.add(InvoiceItem.builder()
                        .description((String) raw.get("description"))
                        .quantity(quantity)
                        .unitPrice(unitPrice)
                        .taxRate(taxRate)
                        .taxAmount(taxAmount)
                        .total(total)
                        .accountId(raw.get("accountId") != null ? UUID.fromString((String) raw.get("accountId")) : null)
                        .build());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(invService.createInvoiceWithItems(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("invoiceType"), (String)b.get("customerName"),
                java.time.LocalDate.parse((String)b.get("issueDate")),
                java.time.LocalDate.parse((String)b.get("dueDate")),
                items));
    }
    @GetMapping("/{id}") public ResponseEntity<Invoice> get(@PathVariable UUID id) {
        return invService.getInvoiceById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<Invoice>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(invService.getInvoicesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(invService.getInvoicesByTenant(tenantId));
    }
    @PostMapping("/{id}/pay") public ResponseEntity<Invoice> pay(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(invService.recordPayment(id, new java.math.BigDecimal(b.get("amount").toString())));
    }
}