package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.Feature;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for the features catalog. Implementations: JpaFeatureRepository.
 */
public interface FeatureRepository {
    Feature save(Feature feature);
    Optional<Feature> findByCode(String code);
    List<Feature> findAll();
    List<Feature> findByModule(String module);
    List<Feature> findAllEnabledForClient(java.util.UUID tenantId);
}
