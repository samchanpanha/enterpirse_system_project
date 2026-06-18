package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Branch;
import com.reportsystem.auth.domain.port.outbound.BranchRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getBranchesByTenant(UUID tenantId) {
        return branchRepository.findByTenantId(tenantId);
    }

    public Optional<Branch> getBranchById(UUID id) {
        return branchRepository.findById(id);
    }

    public Branch createBranch(Branch branch) {
        if (branch.getCode() == null || branch.getCode().isBlank()) {
            throw new IllegalArgumentException("Branch code is required");
        }
        if (branch.getName() == null || branch.getName().isBlank()) {
            throw new IllegalArgumentException("Branch name is required");
        }
        if (branchRepository.findByTenantIdAndCode(branch.getTenantId(), branch.getCode()).isPresent()) {
            throw new IllegalArgumentException("Branch code already exists: " + branch.getCode());
        }
        if (branch.getId() == null) {
            branch.setId(UUID.randomUUID());
        }
        if (branch.getCreatedAt() == null) {
            branch.setCreatedAt(Instant.now());
        }
        if (branch.getBranchType() == null) {
            branch.setBranchType("STORE");
        }
        if (branch.getSettings() == null) {
            branch.setSettings("{}");
        }
        // If this is the first branch, make it default
        List<Branch> existing = branchRepository.findByTenantId(branch.getTenantId());
        if (existing.isEmpty()) {
            branch.setDefault(true);
        }
        return branchRepository.save(branch);
    }

    public Branch updateBranch(UUID id, Branch updates) {
        Branch existing = branchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + id));
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getNameKh() != null) existing.setNameKh(updates.getNameKh());
        if (updates.getBranchType() != null) existing.setBranchType(updates.getBranchType());
        if (updates.getParentId() != null) existing.setParentId(updates.getParentId());
        if (updates.getAddress() != null) existing.setAddress(updates.getAddress());
        if (updates.getCity() != null) existing.setCity(updates.getCity());
        if (updates.getDistrict() != null) existing.setDistrict(updates.getDistrict());
        if (updates.getProvince() != null) existing.setProvince(updates.getProvince());
        if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getTaxRate() != null) existing.setTaxRate(updates.getTaxRate());
        if (updates.getLogoUrl() != null) existing.setLogoUrl(updates.getLogoUrl());
        if (updates.getSettings() != null) existing.setSettings(updates.getSettings());
        if (updates.getOpenedAt() != null) existing.setOpenedAt(updates.getOpenedAt());
        if (updates.getClosedAt() != null) existing.setClosedAt(updates.getClosedAt());
        existing.setActive(updates.isActive());
        existing.setDefault(updates.isDefault());
        existing.setUpdatedAt(Instant.now());
        return branchRepository.save(existing);
    }

    public void deleteBranch(UUID id) {
        Branch branch = branchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + id));
        if (branch.isDefault()) {
            throw new IllegalStateException("Cannot delete the default branch");
        }
        branchRepository.deleteById(id);
    }
}
