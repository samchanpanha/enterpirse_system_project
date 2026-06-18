package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.Outlet;
import com.reportsystem.restaurant.domain.port.outbound.OutletRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.OutletEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaOutletRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaOutletAdapter implements OutletRepository {

    private final JpaOutletRepository repo;
    public JpaOutletAdapter(JpaOutletRepository repo) { this.repo = repo; }

    @Override
    public Outlet save(Outlet outlet) {
        OutletEntity e = new OutletEntity();
        e.setId(outlet.getId());
        e.setTenantId(outlet.getTenantId());
        e.setBranchId(outlet.getBranchId());
        e.setName(outlet.getName());
        e.setAddress(outlet.getAddress());
        e.setPhone(outlet.getPhone());
        e.setEmail(outlet.getEmail());
        e.setTaxNumber(outlet.getTaxNumber());
        e.setType(outlet.getType());
        e.setCurrency(outlet.getCurrency());
        e.setSettings(outlet.getSettings() != null ? outlet.getSettings() : "{}");
        e.setActive(outlet.isActive());
        e.setCreatedAt(Instant.now());
        OutletEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Outlet> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Outlet> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Outlet> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Outlet toDomain(OutletEntity e) {
        return Outlet.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).name(e.getName())
                .address(e.getAddress()).phone(e.getPhone()).email(e.getEmail())
                .taxNumber(e.getTaxNumber()).type(e.getType()).currency(e.getCurrency())
                .settings(e.getSettings()).active(e.isActive())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
