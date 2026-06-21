package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.PropertyValuation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValuationService {
    PropertyValuation createValuation(UUID tenantId, UUID branchId, UUID propertyId, java.math.BigDecimal estimatedValue);
    Optional<PropertyValuation> getValuationById(UUID id);
    List<PropertyValuation> getValuationsByTenant(UUID tenantId);
    List<PropertyValuation> getValuationsByProperty(UUID propertyId);
}
