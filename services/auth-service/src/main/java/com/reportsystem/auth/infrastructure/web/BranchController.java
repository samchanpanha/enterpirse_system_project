package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.Branch;
import com.reportsystem.auth.domain.service.BranchService;
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
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<List<Branch>> listByTenant(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ResponseEntity.ok(branchService.getBranchesByTenant(UUID.fromString(tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getById(@PathVariable UUID id) {
        return branchService.getBranchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Branch> create(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody Map<String, Object> body) {
        Branch branch = Branch.builder()
                .tenantId(UUID.fromString(tenantId))
                .code((String) body.get("code"))
                .name((String) body.get("name"))
                .nameKh((String) body.get("nameKh"))
                .branchType((String) body.getOrDefault("branchType", "STORE"))
                .address((String) body.get("address"))
                .city((String) body.get("city"))
                .district((String) body.get("district"))
                .province((String) body.get("province"))
                .phone((String) body.get("phone"))
                .email((String) body.get("email"))
                .taxRate(body.get("taxRate") != null ? new java.math.BigDecimal(body.get("taxRate").toString()) : null)
                .active(body.get("active") != null ? (Boolean) body.get("active") : true)
                .isDefault(Boolean.TRUE.equals(body.get("isDefault")))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(branch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Branch> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Branch updates = Branch.builder()
                .name((String) body.get("name"))
                .nameKh((String) body.get("nameKh"))
                .branchType((String) body.get("branchType"))
                .address((String) body.get("address"))
                .city((String) body.get("city"))
                .district((String) body.get("district"))
                .province((String) body.get("province"))
                .phone((String) body.get("phone"))
                .email((String) body.get("email"))
                .taxRate(body.get("taxRate") != null ? new java.math.BigDecimal(body.get("taxRate").toString()) : null)
                .logoUrl((String) body.get("logoUrl"))
                .settings((String) body.get("settings"))
                .active(body.get("active") != null ? (Boolean) body.get("active") : true)
                .isDefault(Boolean.TRUE.equals(body.get("isDefault")))
                .build();
        return ResponseEntity.ok(branchService.updateBranch(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
