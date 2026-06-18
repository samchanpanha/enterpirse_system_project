package com.reportsystem.finance.infrastructure.web;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/journal-entries")
public class JournalEntryController {
    private final AccountingService acctService;
    public JournalEntryController(AccountingService a) { acctService = a; }
    @PostMapping public ResponseEntity<JournalEntry> post(@RequestBody Map<String, Object> b) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> linesRaw = (List<Map<String, Object>>) b.get("lines");
        List<JournalEntryLine> lines = linesRaw.stream().map(l -> JournalEntryLine.builder()
                .accountId(UUID.fromString((String)l.get("accountId")))
                .debit(l.get("debit") != null ? new java.math.BigDecimal(l.get("debit").toString()) : java.math.BigDecimal.ZERO)
                .credit(l.get("credit") != null ? new java.math.BigDecimal(l.get("credit").toString()) : java.math.BigDecimal.ZERO)
                .description((String)l.get("description")).build()).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(acctService.postJournalEntry(
                UUID.fromString((String)b.get("tenantId")),
                b.get("branchId") != null ? UUID.fromString((String)b.get("branchId")) : UUID.fromString((String)b.get("tenantId")),
                java.time.LocalDate.parse((String)b.get("entryDate")), (String)b.get("description"),
                (String)b.get("referenceType"),
                b.get("referenceId") != null ? UUID.fromString((String)b.get("referenceId")) : null,
                b.get("createdBy") != null ? UUID.fromString((String)b.get("createdBy")) : null, lines));
    }
    @GetMapping("/{id}") public ResponseEntity<JournalEntry> get(@PathVariable UUID id) {
        return acctService.getJournalEntryById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<JournalEntry>> getByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(acctService.getJournalEntriesByTenant(tenantId));
    }
}