package com.reportsystem.restaurant.domain.service;

import com.reportsystem.restaurant.domain.model.Reservation;
import com.reportsystem.restaurant.domain.port.inbound.ReservationService;
import com.reportsystem.restaurant.domain.port.outbound.ReservationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation createReservation(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, String guestName, String guestPhone, int guestCount, Instant reservationTime) {
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).outletId(outletId).tableId(tableId)
                .guestName(guestName).guestPhone(guestPhone).guestCount(guestCount)
                .reservationTime(reservationTime).durationMinutes(120).status("confirmed")
                .createdAt(Instant.now()).build();
        return reservationRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> getReservationById(UUID id) {
        return reservationRepository.findById(id);
    }

    @Override
    public List<Reservation> getReservationsByOutlet(UUID outletId, Instant date) {
        Instant end = date.plusSeconds(86400);
        return reservationRepository.findByOutletIdAndReservationTimeBetween(outletId, date, end);
    }

    @Override
    public Reservation updateStatus(UUID id, String status) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));
        Reservation updated = Reservation.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).outletId(existing.getOutletId())
                .tableId(existing.getTableId()).customerId(existing.getCustomerId())
                .guestName(existing.getGuestName()).guestPhone(existing.getGuestPhone())
                .guestCount(existing.getGuestCount()).reservationTime(existing.getReservationTime())
                .durationMinutes(existing.getDurationMinutes()).status(status)
                .notes(existing.getNotes()).createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return reservationRepository.save(updated);
    }

    @Override
    public void cancelReservation(UUID id) {
        updateStatus(id, "cancelled");
    }
}
