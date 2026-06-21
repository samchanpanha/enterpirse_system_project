package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.AmenityBooking;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmenityBookingRepository {
    AmenityBooking save(AmenityBooking booking);
    Optional<AmenityBooking> findById(UUID id);
    List<AmenityBooking> findByTenantId(UUID tenantId);
    List<AmenityBooking> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<AmenityBooking> findByResidentId(UUID residentId);
}
