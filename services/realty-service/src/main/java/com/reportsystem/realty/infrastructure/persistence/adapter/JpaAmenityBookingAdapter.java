package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.AmenityBooking;
import com.reportsystem.realty.domain.port.outbound.AmenityBookingRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.AmenityBookingEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaAmenityBookingRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaAmenityBookingAdapter implements AmenityBookingRepository {

    private final JpaAmenityBookingRepository repo;
    public JpaAmenityBookingAdapter(JpaAmenityBookingRepository repo) { this.repo = repo; }

    @Override
    public AmenityBooking save(AmenityBooking booking) {
        AmenityBookingEntity e = new AmenityBookingEntity();
        e.setId(booking.getId());
        e.setTenantId(booking.getTenantId());
        e.setBranchId(booking.getBranchId());
        e.setResidentId(booking.getResidentId());
        e.setAmenityType(booking.getAmenityType());
        e.setBookedDate(booking.getBookedDate());
        e.setStartTime(booking.getStartTime());
        e.setEndTime(booking.getEndTime());
        e.setGuests(booking.getGuests());
        e.setStatus(booking.getStatus() != null ? booking.getStatus() : "confirmed");
        e.setNotes(booking.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<AmenityBooking> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<AmenityBooking> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AmenityBooking> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AmenityBooking> findByResidentId(UUID residentId) {
        return repo.findByResidentId(residentId).stream().map(this::toDomain).toList();
    }

    private AmenityBooking toDomain(AmenityBookingEntity e) {
        return AmenityBooking.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .residentId(e.getResidentId()).amenityType(e.getAmenityType())
                .bookedDate(e.getBookedDate()).startTime(e.getStartTime())
                .endTime(e.getEndTime()).guests(e.getGuests() != null ? e.getGuests() : 1)
                .status(e.getStatus()).notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
