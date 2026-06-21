package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.PropertyValuation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyValuationRepository {
    PropertyValuation save(PropertyValuation valuation);
    Optional<PropertyValuation> findById(UUID id);
    List<PropertyValuation> findByTenantId(UUID tenantId);
    List<PropertyValuation> findByPropertyId(UUID propertyId);
}
