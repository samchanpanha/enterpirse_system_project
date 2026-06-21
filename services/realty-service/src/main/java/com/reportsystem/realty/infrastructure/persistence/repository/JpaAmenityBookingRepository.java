package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.AmenityBookingEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAmenityBookingRepository extends JpaRepository<AmenityBookingEntity, UUID> {
    List<AmenityBookingEntity> findByTenantId(UUID tenantId);
    List<AmenityBookingEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<AmenityBookingEntity> findByResidentId(UUID residentId);
}
