package com.reportsystem.shared.tenant;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_SLUG = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantSlug(String slug) {
        CURRENT_TENANT_SLUG.set(slug);
    }

    public static String getTenantSlug() {
        return CURRENT_TENANT_SLUG.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_TENANT_SLUG.remove();
    }
}
