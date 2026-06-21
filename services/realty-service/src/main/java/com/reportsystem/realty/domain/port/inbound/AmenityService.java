package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.AmenityBooking;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmenityService {
    AmenityBooking createBooking(UUID tenantId, UUID branchId, UUID residentId, String amenityType, java.time.LocalDate bookedDate);
    Optional<AmenityBooking> getBookingById(UUID id);
    List<AmenityBooking> getBookingsByTenant(UUID tenantId);
    List<AmenityBooking> getBookingsByTenantAndBranch(UUID tenantId, UUID branchId);
    List<AmenityBooking> getBookingsByResident(UUID residentId);
    AmenityBooking updateBooking(AmenityBooking booking);
}
