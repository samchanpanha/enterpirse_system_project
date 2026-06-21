package com.reportsystem.payment.infrastructure.web;

import com.reportsystem.payment.domain.model.PaymentGatewayConfig;
import com.reportsystem.payment.domain.port.inbound.PaymentGatewayConfigService;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gateway-configs")
public class PaymentGatewayConfigController {

    private final PaymentGatewayConfigService service;
    public PaymentGatewayConfigController(PaymentGatewayConfigService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<PaymentGatewayConfig> create(@RequestBody Map<String, Object> b) {
        PaymentGatewayConfig config = PaymentGatewayConfig.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.fromString((String) b.get("tenantId")))
                .gateway((String) b.get("gateway"))
                .config(b.get("config") != null ? b.get("config").toString() : "{}")
                .active(b.get("active") == null || (boolean) b.get("active"))
                .sandbox(b.get("sandbox") != null && (boolean) b.get("sandbox"))
                .createdAt(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(config));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentGatewayConfig> get(@PathVariable UUID id) {
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<PaymentGatewayConfig>> getByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(service.getByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentGatewayConfig> update(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        PaymentGatewayConfig config = PaymentGatewayConfig.builder()
                .gateway((String) b.get("gateway"))
                .config(b.get("config") != null ? b.get("config").toString() : null)
                .active(b.get("active") == null || (boolean) b.get("active"))
                .sandbox(b.get("sandbox") != null && (boolean) b.get("sandbox"))
                .build();
        return ResponseEntity.ok(service.update(id, config));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
