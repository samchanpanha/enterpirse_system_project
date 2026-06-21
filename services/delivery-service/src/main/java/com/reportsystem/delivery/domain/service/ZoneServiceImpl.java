package com.reportsystem.delivery.domain.service;

import com.reportsystem.delivery.domain.model.DeliveryZone;
import com.reportsystem.delivery.domain.port.inbound.ZoneService;
import com.reportsystem.delivery.domain.port.outbound.DeliveryZoneRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ZoneServiceImpl implements ZoneService {

    private final DeliveryZoneRepository zoneRepository;

    public ZoneServiceImpl(DeliveryZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @Override
    public DeliveryZone createZone(UUID tenantId, UUID branchId, String name, String nameKh) {
        DeliveryZone zone = DeliveryZone.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .name(name).nameKh(nameKh).boundaries("{}")
                .baseFee(BigDecimal.ZERO).perKmFee(BigDecimal.ZERO)
                .minFee(BigDecimal.ZERO).estimatedMinutes(0)
                .isActive(true).createdAt(Instant.now()).build();
        return zoneRepository.save(zone);
    }

    @Override
    public Optional<DeliveryZone> getZoneById(UUID id) {
        return zoneRepository.findById(id);
    }

    @Override
    public List<DeliveryZone> getZonesByTenant(UUID tenantId) {
        return zoneRepository.findByTenantId(tenantId);
    }

    @Override
    public List<DeliveryZone> getZonesByTenantAndBranch(UUID tenantId, UUID branchId) {
        return zoneRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public DeliveryZone updateStatus(UUID id, boolean isActive) {
        DeliveryZone existing = zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DeliveryZone not found: " + id));
        DeliveryZone updated = DeliveryZone.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                .name(existing.getName()).nameKh(existing.getNameKh()).boundaries(existing.getBoundaries())
                .baseFee(existing.getBaseFee()).perKmFee(existing.getPerKmFee())
                .minFee(existing.getMinFee()).maxFee(existing.getMaxFee())
                .estimatedMinutes(existing.getEstimatedMinutes()).isActive(isActive)
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return zoneRepository.save(updated);
    }
}
