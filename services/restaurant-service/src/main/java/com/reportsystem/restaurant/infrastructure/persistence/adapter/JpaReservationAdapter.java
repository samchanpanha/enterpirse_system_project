package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.Reservation;
import com.reportsystem.restaurant.domain.port.outbound.ReservationRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.ReservationEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaReservationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaReservationAdapter implements ReservationRepository {

    private final JpaReservationRepository repo;
    public JpaReservationAdapter(JpaReservationRepository repo) { this.repo = repo; }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity e = new ReservationEntity();
        e.setId(reservation.getId());
        e.setTenantId(reservation.getTenantId());
        e.setBranchId(reservation.getBranchId());
        e.setOutletId(reservation.getOutletId());
        e.setTableId(reservation.getTableId());
        e.setCustomerId(reservation.getCustomerId());
        e.setGuestName(reservation.getGuestName());
        e.setGuestPhone(reservation.getGuestPhone());
        e.setGuestCount(reservation.getGuestCount());
        e.setReservationTime(reservation.getReservationTime());
        e.setDurationMinutes(reservation.getDurationMinutes());
        e.setStatus(reservation.getStatus());
        e.setNotes(reservation.getNotes());
        e.setCreatedAt(Instant.now());
        ReservationEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByOutletIdAndReservationTimeBetween(UUID outletId, Instant from, Instant to) {
        return repo.findByOutletIdAndReservationTimeBetween(outletId, from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Reservation> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Reservation toDomain(ReservationEntity e) {
        return Reservation.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).outletId(e.getOutletId())
                .tableId(e.getTableId()).customerId(e.getCustomerId())
                .guestName(e.getGuestName()).guestPhone(e.getGuestPhone())
                .guestCount(e.getGuestCount()).reservationTime(e.getReservationTime())
                .durationMinutes(e.getDurationMinutes()).status(e.getStatus())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
