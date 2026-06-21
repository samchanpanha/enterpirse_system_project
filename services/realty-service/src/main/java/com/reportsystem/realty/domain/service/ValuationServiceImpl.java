package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.PropertyValuation;
import com.reportsystem.realty.domain.port.inbound.ValuationService;
import com.reportsystem.realty.domain.port.outbound.PropertyValuationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ValuationServiceImpl implements ValuationService {

    private final PropertyValuationRepository valuationRepository;

    public ValuationServiceImpl(PropertyValuationRepository valuationRepository) {
        this.valuationRepository = valuationRepository;
    }

    @Override
    public PropertyValuation createValuation(UUID tenantId, UUID branchId, UUID propertyId, BigDecimal estimatedValue) {
        PropertyValuation valuation = PropertyValuation.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .propertyId(propertyId).estimatedValue(estimatedValue)
                .createdAt(Instant.now()).build();
        return valuationRepository.save(valuation);
    }

    @Override
    public Optional<PropertyValuation> getValuationById(UUID id) {
        return valuationRepository.findById(id);
    }

    @Override
    public List<PropertyValuation> getValuationsByTenant(UUID tenantId) {
        return valuationRepository.findByTenantId(tenantId);
    }

    @Override
    public List<PropertyValuation> getValuationsByProperty(UUID propertyId) {
        return valuationRepository.findByPropertyId(propertyId);
    }
}
