package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Unit;
import com.reportsystem.property.domain.port.inbound.UnitUseCase;
import com.reportsystem.property.domain.port.outbound.UnitRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UnitService implements UnitUseCase {

    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Override
    public Unit createUnit(UUID tenantId, UUID branchId, UUID propertyId, String label, Integer floor, Integer bedrooms, Integer bathrooms) {
        Unit unit = Unit.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).propertyId(propertyId)
                .label(label).floor(floor).bedrooms(bedrooms).bathrooms(bathrooms)
                .currency("USD").status("vacant").amenities("[]").images("[]")
                .createdAt(Instant.now())
                .build();
        return unitRepository.save(unit);
    }

    @Override
    public Optional<Unit> getUnitById(UUID id) {
        return unitRepository.findById(id);
    }

    @Override
    public List<Unit> getUnitsByProperty(UUID propertyId) {
        return unitRepository.findByPropertyId(propertyId);
    }

    @Override
    public Unit updateUnit(UUID id, String label, Integer floor, String status, BigDecimal rentAmount) {
        Unit existing = unitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + id));
        Unit updated = Unit.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).propertyId(existing.getPropertyId())
                .label(label != null ? label : existing.getLabel())
                .floor(floor != null ? floor : existing.getFloor())
                .bedrooms(existing.getBedrooms()).bathrooms(existing.getBathrooms())
                .areaSqm(existing.getAreaSqm())
                .rentAmount(rentAmount != null ? rentAmount : existing.getRentAmount())
                .depositAmount(existing.getDepositAmount()).currency(existing.getCurrency())
                .status(status != null ? status : existing.getStatus())
                .type(existing.getType()).amenities(existing.getAmenities()).images(existing.getImages())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return unitRepository.save(updated);
    }

    @Override
    public void deleteUnit(UUID id) {
        unitRepository.deleteById(id);
    }
}
