package com.reportsystem.property.domain.port.inbound;

import com.reportsystem.property.domain.model.Unit;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.UUID;

public interface UnitUseCase {
    Unit createUnit(UUID tenantId, UUID branchId, UUID propertyId, String label, Integer floor, Integer bedrooms, Integer bathrooms, BigDecimal rentAmount);
    Optional<Unit> getUnitById(UUID id);
    List<Unit> getUnitsByProperty(UUID propertyId);
    Unit updateUnit(UUID id, String label, Integer floor, String status, BigDecimal rentAmount);
    void deleteUnit(UUID id);
}
