package com.reportsystem.property.infrastructure.web;

import com.reportsystem.property.domain.model.Property;
import com.reportsystem.property.domain.service.PropertyService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<Property> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                            @RequestBody Map<String, String> body) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        Property property = propertyService.createProperty(
                UUID.fromString(body.get("tenantId")),
                bid,
                body.get("name"), body.get("type"),
                body.get("address"), body.get("city"), body.get("district"));
        return ResponseEntity.status(HttpStatus.CREATED).body(property);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable UUID id) {
        return propertyService.getPropertyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Property>> getByTenant(@PathVariable UUID tenantId,
                                                      @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(propertyService.getPropertiesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(propertyService.getPropertiesByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Property property = propertyService.updateProperty(id, body.get("name"), body.get("type"),
                body.get("address"), body.get("city"), body.get("district"), body.get("status"));
        return ResponseEntity.ok(property);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}
