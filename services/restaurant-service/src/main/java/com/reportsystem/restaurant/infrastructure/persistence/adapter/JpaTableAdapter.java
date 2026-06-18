package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.RestaurantTable;
import com.reportsystem.restaurant.domain.port.outbound.TableRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.TableEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaTableRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaTableAdapter implements TableRepository {

    private final JpaTableRepository repo;
    public JpaTableAdapter(JpaTableRepository repo) { this.repo = repo; }

    @Override
    public RestaurantTable save(RestaurantTable table) {
        TableEntity e = new TableEntity();
        e.setId(table.getId());
        e.setTenantId(table.getTenantId());
        e.setOutletId(table.getOutletId());
        e.setLabel(table.getLabel());
        e.setCapacity(table.getCapacity());
        e.setFloor(table.getFloor());
        e.setSection(table.getSection());
        e.setPosX(table.getPosX());
        e.setPosY(table.getPosY());
        e.setStatus(table.getStatus() != null ? table.getStatus() : "available");
        e.setQrCodeUrl(table.getQrCodeUrl());
        e.setCreatedAt(Instant.now());
        TableEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<RestaurantTable> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<RestaurantTable> findByOutletId(UUID outletId) {
        return repo.findByOutletId(outletId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<RestaurantTable> findByStatus(String status) {
        return List.of(); // simplified; expand if needed
    }

    private RestaurantTable toDomain(TableEntity e) {
        return RestaurantTable.builder()
                .id(e.getId()).tenantId(e.getTenantId()).outletId(e.getOutletId())
                .label(e.getLabel()).capacity(e.getCapacity()).floor(e.getFloor())
                .section(e.getSection()).posX(e.getPosX()).posY(e.getPosY())
                .status(e.getStatus()).qrCodeUrl(e.getQrCodeUrl())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
