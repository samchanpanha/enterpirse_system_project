package com.reportsystem.inventory.infrastructure.web;

import com.reportsystem.inventory.domain.model.StockTransfer;
import com.reportsystem.inventory.domain.port.inbound.StockTransferService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
public class StockTransferController {

    private final StockTransferService transferService;

    public StockTransferController(StockTransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<StockTransfer> create(
            @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
            @RequestBody Map<String, Object> body) {
        UUID fromBranchId = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        if (body.get("fromBranchId") != null) {
            fromBranchId = UUID.fromString((String) body.get("fromBranchId"));
        }
        UUID toBranchId = UUID.fromString((String) body.get("toBranchId"));
        UUID fromWarehouseId = body.get("fromWarehouseId") != null ? UUID.fromString((String) body.get("fromWarehouseId")) : null;
        UUID toWarehouseId = body.get("toWarehouseId") != null ? UUID.fromString((String) body.get("toWarehouseId")) : null;
        UUID tenantId = UUID.fromString((String) body.get("tenantId"));
        String notes = (String) body.get("notes");
        UUID createdBy = body.get("createdBy") != null ? UUID.fromString((String) body.get("createdBy")) : null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) body.get("items");
        List<StockTransferService.ItemDraft> items = rawItems.stream()
            .map(m -> new StockTransferService.ItemDraft(
                UUID.fromString((String) m.get("productId")),
                new java.math.BigDecimal(m.get("quantity").toString()),
                m.get("unitCost") != null ? new java.math.BigDecimal(m.get("unitCost").toString()) : null
            ))
            .toList();

        StockTransfer transfer = transferService.createDraft(tenantId, fromBranchId, toBranchId,
            fromWarehouseId, toWarehouseId, items, notes, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<StockTransfer> ship(@PathVariable UUID id,
                                              @RequestBody(required = false) Map<String, Object> body) {
        UUID shippedBy = body != null && body.get("shippedBy") != null
            ? UUID.fromString((String) body.get("shippedBy")) : null;
        return ResponseEntity.ok(transferService.ship(id, shippedBy));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<StockTransfer> receive(@PathVariable UUID id,
                                                  @RequestBody(required = false) Map<String, Object> body) {
        UUID receivedBy = body != null && body.get("receivedBy") != null
            ? UUID.fromString((String) body.get("receivedBy")) : null;
        List<StockTransferService.ReceiveLine> lines = null;
        if (body != null && body.get("lines") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("lines");
            lines = raw.stream().map(m -> new StockTransferService.ReceiveLine(
                UUID.fromString((String) m.get("productId")),
                m.get("receivedQuantity") != null ? new java.math.BigDecimal(m.get("receivedQuantity").toString()) : null,
                (String) m.get("discrepancyNotes")
            )).toList();
        }
        return ResponseEntity.ok(transferService.receive(id, receivedBy, lines));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<StockTransfer> cancel(@PathVariable UUID id,
                                                @RequestBody(required = false) Map<String, Object> body) {
        UUID cancelledBy = body != null && body.get("cancelledBy") != null
            ? UUID.fromString((String) body.get("cancelledBy")) : null;
        String reason = body != null ? (String) body.get("reason") : null;
        return ResponseEntity.ok(transferService.cancel(id, cancelledBy, reason));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransfer> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<StockTransfer>> list(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(transferService.listByTenant(tenantId, status));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<List<StockTransfer>> listByBranch(
            @RequestParam UUID tenantId,
            @PathVariable UUID branchId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(transferService.listByBranch(tenantId, branchId, status));
    }

    @GetMapping("/incoming/{branchId}")
    public ResponseEntity<List<StockTransfer>> incoming(@PathVariable UUID branchId) {
        return ResponseEntity.ok(transferService.listIncoming(branchId));
    }
}
