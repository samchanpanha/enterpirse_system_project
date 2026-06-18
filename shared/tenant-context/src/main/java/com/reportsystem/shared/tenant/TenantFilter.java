package com.reportsystem.shared.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    static final String TENANT_ID_HEADER = "X-Tenant-Id";
    static final String TENANT_SLUG_HEADER = "X-Tenant-Slug";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String tenantId = request.getHeader(TENANT_ID_HEADER);
            String tenantSlug = request.getHeader(TENANT_SLUG_HEADER);

            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(UUID.fromString(tenantId));
            }
            if (tenantSlug != null && !tenantSlug.isBlank()) {
                TenantContext.setTenantSlug(tenantSlug);
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
