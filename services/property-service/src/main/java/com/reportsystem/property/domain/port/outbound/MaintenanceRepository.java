package com.reportsystem.property.domain.port.outbound;

import com.reportsystem.property.domain.model.MaintenanceTicket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceRepository {
    MaintenanceTicket save(MaintenanceTicket ticket);
    Optional<MaintenanceTicket> findById(UUID id);
    List<MaintenanceTicket> findByUnitId(UUID unitId);
    List<MaintenanceTicket> findByTenantId(UUID tenantId);
    List<MaintenanceTicket> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<MaintenanceTicket> findByUnitIds(java.util.Collection<UUID> unitIds);
}
