package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Parcel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParcelService {
    Parcel createParcel(UUID tenantId, UUID branchId, UUID residentId, String carrier);
    Optional<Parcel> getParcelById(UUID id);
    List<Parcel> getParcelsByTenant(UUID tenantId);
    List<Parcel> getParcelsByTenantAndBranch(UUID tenantId, UUID branchId);
    Parcel updateParcel(Parcel parcel);
}
