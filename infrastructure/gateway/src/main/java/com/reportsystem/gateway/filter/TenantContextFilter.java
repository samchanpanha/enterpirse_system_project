package com.reportsystem.gateway.filter;

import com.reportsystem.shared.tenant.TenantContext;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TenantContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).contextWrite(ctx -> {
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(UUID.fromString(tenantId));
            }
            String tenantSlug = exchange.getRequest().getHeaders().getFirst("X-Tenant-Slug");
            if (tenantSlug != null && !tenantSlug.isBlank()) {
                TenantContext.setTenantSlug(tenantSlug);
            }
            return ctx;
        }).then(Mono.fromRunnable(TenantContext::clear));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
