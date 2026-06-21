package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.PropertyValuation;
import com.reportsystem.realty.domain.port.outbound.PropertyValuationRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.PropertyValuationEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaPropertyValuationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaPropertyValuationAdapter implements PropertyValuationRepository {

    private final JpaPropertyValuationRepository repo;
    public JpaPropertyValuationAdapter(JpaPropertyValuationRepository repo) { this.repo = repo; }

    @Override
    public PropertyValuation save(PropertyValuation valuation) {
        PropertyValuationEntity e = new PropertyValuationEntity();
        e.setId(valuation.getId());
        e.setTenantId(valuation.getTenantId());
        e.setBranchId(valuation.getBranchId());
        e.setPropertyId(valuation.getPropertyId());
        e.setEstimatedValue(valuation.getEstimatedValue());
        e.setLowRange(valuation.getLowRange());
        e.setHighRange(valuation.getHighRange());
        e.setConfidenceScore(valuation.getConfidenceScore());
        e.setValuationDate(valuation.getValuationDate());
        e.setFactors(valuation.getFactors() != null ? valuation.getFactors() : "{}");
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<PropertyValuation> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<PropertyValuation> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<PropertyValuation> findByPropertyId(UUID propertyId) {
        return repo.findByPropertyId(propertyId).stream().map(this::toDomain).toList();
    }

    private PropertyValuation toDomain(PropertyValuationEntity e) {
        return PropertyValuation.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .propertyId(e.getPropertyId())
                .estimatedValue(e.getEstimatedValue()).lowRange(e.getLowRange())
                .highRange(e.getHighRange()).confidenceScore(e.getConfidenceScore())
                .valuationDate(e.getValuationDate()).factors(e.getFactors())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
