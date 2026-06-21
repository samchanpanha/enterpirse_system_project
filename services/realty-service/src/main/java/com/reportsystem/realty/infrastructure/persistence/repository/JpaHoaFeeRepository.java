package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.HoaFeeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHoaFeeRepository extends JpaRepository<HoaFeeEntity, UUID> {
    List<HoaFeeEntity> findByTenantId(UUID tenantId);
    List<HoaFeeEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<HoaFeeEntity> findByPropertyId(UUID propertyId);
}
