package com.reportsystem.finance.infrastructure.web;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountingService acctService;
    public AccountController(AccountingService a) { acctService = a; }
    @PostMapping public ResponseEntity<Account> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(acctService.createAccount(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("code"), (String)b.get("name"),
                (String)b.get("type"), b.get("parentId") != null ? UUID.fromString((String)b.get("parentId")) : null));
    }
    @GetMapping("/{id}") public ResponseEntity<Account> get(@PathVariable UUID id) {
        return acctService.getAccountById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<Account>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(acctService.getAccountsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(acctService.getAccountsByTenant(tenantId));
    }
}