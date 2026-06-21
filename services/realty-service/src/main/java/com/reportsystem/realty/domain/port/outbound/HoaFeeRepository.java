package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.HoaFee;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoaFeeRepository {
    HoaFee save(HoaFee hoaFee);
    Optional<HoaFee> findById(UUID id);
    List<HoaFee> findByTenantId(UUID tenantId);
    List<HoaFee> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<HoaFee> findByPropertyId(UUID propertyId);
}
