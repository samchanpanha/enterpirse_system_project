package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.ClientFeature;
import com.reportsystem.auth.domain.model.Feature;
import com.reportsystem.auth.domain.service.FeatureService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Feature management endpoints.
 *
 * - GET    /api/features                       → list catalog
 * - GET    /api/features/{code}                → single feature
 * - GET    /api/features/enabled               → enabled features for current tenant
 * - GET    /api/features/tree                 → module → features tree with enabled flags
 * - GET    /api/features/check/{code}         → is {code} enabled for this tenant?
 * - POST   /api/features/{code}/enable         → enable for tenant
 * - POST   /api/features/{code}/disable        → disable for tenant
 */
@RestController
@RequestMapping("/features")
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @GetMapping
    public ResponseEntity<List<Feature>> listCatalog() {
        return ResponseEntity.ok(featureService.listCatalog());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Map<String, Object>>> tree(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ResponseEntity.ok(featureService.featureTreeFor(UUID.fromString(tenantId)));
    }

    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Boolean>> enabled(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ResponseEntity.ok(featureService.enabledFeaturesFor(UUID.fromString(tenantId)));
    }

    @GetMapping("/check/{code}")
    public ResponseEntity<Map<String, Object>> check(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String code) {
        boolean enabled = featureService.isEnabled(UUID.fromString(tenantId), code);
        return ResponseEntity.ok(Map.of("code", code, "enabled", enabled));
    }

    @PostMapping("/{code}/enable")
    public ResponseEntity<ClientFeature> enable(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String code) {
        UUID enabledBy = userId != null ? UUID.fromString(userId) : null;
        return ResponseEntity.ok(featureService.enable(UUID.fromString(tenantId), code, enabledBy));
    }

    @PostMapping("/{code}/disable")
    public ResponseEntity<ClientFeature> disable(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String code) {
        return ResponseEntity.ok(featureService.disable(UUID.fromString(tenantId), code));
    }
}
