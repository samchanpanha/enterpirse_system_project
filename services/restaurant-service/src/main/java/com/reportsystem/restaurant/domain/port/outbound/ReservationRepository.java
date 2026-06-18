package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.Reservation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
    List<Reservation> findByOutletIdAndReservationTimeBetween(UUID outletId, Instant from, Instant to);
    List<Reservation> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}
