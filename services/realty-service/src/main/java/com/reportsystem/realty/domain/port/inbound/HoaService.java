package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.HoaFee;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoaService {
    HoaFee createHoaFee(UUID tenantId, UUID branchId, UUID propertyId, UUID residentId, java.math.BigDecimal amount, java.time.LocalDate dueDate);
    Optional<HoaFee> getHoaFeeById(UUID id);
    List<HoaFee> getHoaFeesByTenant(UUID tenantId);
    List<HoaFee> getHoaFeesByTenantAndBranch(UUID tenantId, UUID branchId);
    List<HoaFee> getHoaFeesByProperty(UUID propertyId);
    HoaFee updateHoaFee(HoaFee hoaFee);
}
