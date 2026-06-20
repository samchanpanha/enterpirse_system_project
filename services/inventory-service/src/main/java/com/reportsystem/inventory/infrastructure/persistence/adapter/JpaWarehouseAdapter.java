package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaWarehouseAdapter implements WarehouseRepository {
    private final JpaWarehouseRepository repo;
    public JpaWarehouseAdapter(JpaWarehouseRepository repo) { this.repo = repo; }
    @Override public Warehouse save(Warehouse w) {
        WarehouseEntity e = new WarehouseEntity(); e.setId(w.getId()); e.setTenantId(w.getTenantId()); e.setBranchId(w.getBranchId());
        e.setName(w.getName()); e.setType(w.getType()); e.setLocation(w.getLocation()); e.setActive(w.isActive()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }
    @Override public Optional<Warehouse> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Warehouse> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    private Warehouse toDomain(WarehouseEntity e) { return Warehouse.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).name(e.getName()).type(e.getType()).location(e.getLocation()).active(e.isActive()).createdAt(e.getCreatedAt()).build(); }
}