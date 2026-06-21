package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.AmenityBooking;
import com.reportsystem.realty.domain.port.inbound.AmenityService;
import com.reportsystem.realty.domain.port.outbound.AmenityBookingRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AmenityServiceImpl implements AmenityService {

    private final AmenityBookingRepository bookingRepository;

    public AmenityServiceImpl(AmenityBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public AmenityBooking createBooking(UUID tenantId, UUID branchId, UUID residentId, String amenityType, LocalDate bookedDate) {
        AmenityBooking booking = AmenityBooking.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .residentId(residentId).amenityType(amenityType)
                .bookedDate(bookedDate).guests(1).status("confirmed")
                .createdAt(Instant.now()).build();
        return bookingRepository.save(booking);
    }

    @Override
    public Optional<AmenityBooking> getBookingById(UUID id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<AmenityBooking> getBookingsByTenant(UUID tenantId) {
        return bookingRepository.findByTenantId(tenantId);
    }

    @Override
    public List<AmenityBooking> getBookingsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return bookingRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<AmenityBooking> getBookingsByResident(UUID residentId) {
        return bookingRepository.findByResidentId(residentId);
    }

    @Override
    public AmenityBooking updateBooking(AmenityBooking booking) {
        return bookingRepository.save(booking);
    }
}
