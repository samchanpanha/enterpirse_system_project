package com.reportsystem.restaurant.domain.port.inbound;

import com.reportsystem.restaurant.domain.model.Reservation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {
    Reservation createReservation(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, String guestName, String guestPhone, int guestCount, Instant reservationTime);
    Optional<Reservation> getReservationById(UUID id);
    List<Reservation> getReservationsByOutlet(UUID outletId, Instant date);
    Reservation updateStatus(UUID id, String status);
    void cancelReservation(UUID id);
}
