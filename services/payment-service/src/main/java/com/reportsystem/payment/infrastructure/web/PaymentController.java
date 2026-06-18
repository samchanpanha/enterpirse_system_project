package com.reportsystem.payment.infrastructure.web;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final ReconciliationService recService;
    public PaymentController(PaymentService p, ReconciliationService r) { paymentService = p; recService = r; }

    @PostMapping("/process")
    public ResponseEntity<Transaction> process(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        Transaction tx = paymentService.createTransaction(
                UUID.fromString((String)b.get("tenantId")),
                bid,
                b.get("invoiceId") != null ? UUID.fromString((String)b.get("invoiceId")) : null,
                new java.math.BigDecimal(b.get("amount").toString()),
                (String)b.getOrDefault("currency", "USD"),
                (String)b.get("gateway"), (String)b.get("method"),
                (String)b.get("customerName"), (String)b.get("customerPhone"),
                (String)b.get("sourceType"),
                b.get("sourceId") != null ? UUID.fromString((String)b.get("sourceId")) : null);
        return ResponseEntity.ok(paymentService.processPayment(tx.getId()));
    }

    @GetMapping("/{id}") public ResponseEntity<Transaction> get(@PathVariable UUID id) {
        return paymentService.getTransactionById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<Transaction>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(paymentService.getTransactionsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(paymentService.getTransactionsByTenant(tenantId));
    }

    @PostMapping("/{id}/refund") public ResponseEntity<Transaction> refund(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(paymentService.refundTransaction(id,
                new java.math.BigDecimal(b.get("amount").toString()),
                (String)b.get("reason"),
                b.get("createdBy") != null ? UUID.fromString((String)b.get("createdBy")) : null));
    }

    @PostMapping("/reconciliation/start") public ResponseEntity<ReconciliationRecord> startRec(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.ok(recService.startReconciliation(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("gateway"),
                java.time.LocalDate.parse((String)b.get("statementDate"))));
    }

    @PostMapping("/reconciliation/{id}/complete") public ResponseEntity<ReconciliationRecord> completeRec(@PathVariable UUID id) {
        return ResponseEntity.ok(recService.completeReconciliation(id));
    }
}
