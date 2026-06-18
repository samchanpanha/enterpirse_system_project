package com.reportsystem.property.domain.port.inbound;

import com.reportsystem.property.domain.model.MaintenanceTicket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceUseCase {
    MaintenanceTicket createTicket(UUID tenantId, UUID branchId, UUID unitId, String title, String description, String priority, String category);
    Optional<MaintenanceTicket> getTicketById(UUID id);
    List<MaintenanceTicket> getTicketsByUnit(UUID unitId);
    List<MaintenanceTicket> getTicketsByProperty(UUID propertyId);
    List<MaintenanceTicket> getTicketsByTenant(UUID tenantId);
    List<MaintenanceTicket> getTicketsByTenantAndBranch(UUID tenantId, UUID branchId);
    MaintenanceTicket updateStatus(UUID id, String status);
    MaintenanceTicket assignTicket(UUID id, String assignedTo);
    MaintenanceTicket completeTicket(UUID id, java.math.BigDecimal actualCost);
}
