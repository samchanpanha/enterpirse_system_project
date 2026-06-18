package com.reportsystem.shared.tenant;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Request-scoped helper for the current branch.
 * Reads X-Branch-Id set by the gateway OidcTokenValidator.
 * Falls back to X-Tenant-Id's first branch if not set.
 */
public final class BranchContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private BranchContext() {}

    public static void set(UUID branchId) {
        CURRENT.set(branchId);
    }

    public static UUID get() {
        UUID cached = CURRENT.get();
        if (cached != null) return cached;
        // Fallback: read from request headers
        UUID fromHeader = readFromHeader();
        if (fromHeader != null) {
            CURRENT.set(fromHeader);
        }
        return fromHeader;
    }

    public static void clear() {
        CURRENT.remove();
    }

    private static UUID readFromHeader() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            String header = attrs.getRequest().getHeader("X-Branch-Id");
            if (header == null || header.isBlank()) return null;
            return UUID.fromString(header);
        } catch (Exception e) {
            return null;
        }
    }
}
