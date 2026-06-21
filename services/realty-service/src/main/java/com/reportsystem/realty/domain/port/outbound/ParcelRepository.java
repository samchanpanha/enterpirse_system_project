package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Parcel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParcelRepository {
    Parcel save(Parcel parcel);
    Optional<Parcel> findById(UUID id);
    List<Parcel> findByTenantId(UUID tenantId);
    List<Parcel> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}
