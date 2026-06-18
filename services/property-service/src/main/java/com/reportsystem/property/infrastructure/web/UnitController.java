package com.reportsystem.property.infrastructure.web;

import com.reportsystem.property.domain.model.Unit;
import com.reportsystem.property.domain.service.UnitService;
import java.math.BigDecimal;
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
@RequestMapping("/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @PostMapping
    public ResponseEntity<Unit> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                       @RequestBody Map<String, Object> body) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        Unit unit = unitService.createUnit(
                UUID.fromString((String) body.get("tenantId")),
                bid,
                UUID.fromString((String) body.get("propertyId")),
                (String) body.get("label"),
                (Integer) body.get("floor"),
                (Integer) body.get("bedrooms"),
                (Integer) body.get("bathrooms"));
        return ResponseEntity.status(HttpStatus.CREATED).body(unit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unit> getById(@PathVariable UUID id) {
        return unitService.getUnitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<Unit>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(unitService.getUnitsByProperty(propertyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unit> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Unit unit = unitService.updateUnit(id,
                (String) body.get("label"),
                (Integer) body.get("floor"),
                (String) body.get("status"),
                body.get("rentAmount") != null ? new BigDecimal(body.get("rentAmount").toString()) : null);
        return ResponseEntity.ok(unit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }
}
