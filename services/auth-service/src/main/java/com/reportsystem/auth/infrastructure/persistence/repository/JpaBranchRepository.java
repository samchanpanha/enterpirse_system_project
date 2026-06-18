package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.BranchEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBranchRepository extends JpaRepository<BranchEntity, UUID> {
    List<BranchEntity> findByTenantId(UUID tenantId);
    Optional<BranchEntity> findByTenantIdAndCode(UUID tenantId, String code);
}
