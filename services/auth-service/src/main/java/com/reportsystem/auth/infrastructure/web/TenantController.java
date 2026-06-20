package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.Tenant;
import com.reportsystem.auth.domain.service.TenantService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Map<String, String> request) {
        Tenant tenant = tenantService.createTenant(
                request.get("name"),
                request.get("slug"),
                request.get("domain"));
        return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> listTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenant(@PathVariable UUID id) {
        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<Tenant> getTenantBySlug(@PathVariable String slug) {
        return tenantService.getTenantBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        Tenant tenant = tenantService.updateTenant(
                id,
                request.get("name"),
                request.get("domain"),
                request.get("logoUrl"));
        return ResponseEntity.ok(tenant);
    }

    @PatchMapping("/{id}/tier")
    public ResponseEntity<Tenant> updateTier(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        Tenant tenant = tenantService.updateTenantTier(id, request.get("tier"));
        return ResponseEntity.ok(tenant);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateTenant(@PathVariable UUID id) {
        tenantService.activateTenant(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateTenant(@PathVariable UUID id) {
        tenantService.deactivateTenant(id);
        return ResponseEntity.ok().build();
    }
}
