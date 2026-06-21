package com.reportsystem.delivery.infrastructure.persistence.adapter;

import com.reportsystem.delivery.domain.model.DeliveryZone;
import com.reportsystem.delivery.domain.port.outbound.DeliveryZoneRepository;
import com.reportsystem.delivery.infrastructure.persistence.entity.DeliveryZoneEntity;
import com.reportsystem.delivery.infrastructure.persistence.repository.JpaDeliveryZoneRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaDeliveryZoneAdapter implements DeliveryZoneRepository {

    private final JpaDeliveryZoneRepository repo;
    public JpaDeliveryZoneAdapter(JpaDeliveryZoneRepository repo) { this.repo = repo; }

    @Override
    public DeliveryZone save(DeliveryZone zone) {
        DeliveryZoneEntity e = toEntity(zone);
        DeliveryZoneEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<DeliveryZone> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<DeliveryZone> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DeliveryZone> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private DeliveryZoneEntity toEntity(DeliveryZone z) {
        DeliveryZoneEntity e = new DeliveryZoneEntity();
        e.setId(z.getId());
        e.setTenantId(z.getTenantId());
        e.setBranchId(z.getBranchId());
        e.setName(z.getName());
        e.setNameKh(z.getNameKh());
        e.setBoundaries(z.getBoundaries() != null ? z.getBoundaries() : "{}");
        e.setBaseFee(z.getBaseFee());
        e.setPerKmFee(z.getPerKmFee());
        e.setMinFee(z.getMinFee());
        e.setMaxFee(z.getMaxFee());
        e.setEstimatedMinutes(z.getEstimatedMinutes());
        e.setActive(z.isActive());
        e.setCreatedAt(z.getCreatedAt());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private DeliveryZone toDomain(DeliveryZoneEntity e) {
        return DeliveryZone.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .name(e.getName()).nameKh(e.getNameKh()).boundaries(e.getBoundaries())
                .baseFee(e.getBaseFee()).perKmFee(e.getPerKmFee())
                .minFee(e.getMinFee()).maxFee(e.getMaxFee())
                .estimatedMinutes(e.getEstimatedMinutes()).isActive(e.isActive())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
