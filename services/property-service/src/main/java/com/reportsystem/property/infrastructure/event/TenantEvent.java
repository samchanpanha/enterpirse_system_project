package com.reportsystem.property.infrastructure.event;

import java.util.UUID;

public record TenantEvent(
    UUID tenantId,
    UUID unitId,
    UUID leaseId,
    String tenantName,
    String eventType
) {
    public static TenantEvent movedIn(UUID tenantId, UUID unitId, UUID leaseId, String tenantName) {
        return new TenantEvent(tenantId, unitId, leaseId, tenantName, "MOVED_IN");
    }

    public static TenantEvent movedOut(UUID tenantId, UUID unitId, UUID leaseId, String tenantName) {
        return new TenantEvent(tenantId, unitId, leaseId, tenantName, "MOVED_OUT");
    }
}
