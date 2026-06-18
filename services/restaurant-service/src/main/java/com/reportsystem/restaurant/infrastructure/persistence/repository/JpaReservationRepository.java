package com.reportsystem.restaurant.infrastructure.persistence.repository;

import com.reportsystem.restaurant.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByOutletIdAndReservationTimeBetween(UUID outletId, java.time.Instant from, java.time.Instant to);
    List<ReservationEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}
