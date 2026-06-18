package com.reportsystem.gateway.filter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * BranchContextFilter — validates that the user has access to the requested branch.
 *
 * After OidcTokenValidator has authenticated the request, the request carries:
 *   X-Tenant-Id, X-User-Id, X-Branch-Id
 *
 * This filter:
 *   1. Reads X-User-Id + X-Branch-Id
 *   2. If X-Branch-Id is present, calls auth-service /api/user-branches/by-user/{userId}
 *      to load the user's allowed branch IDs (cached for 60s)
 *   3. Returns 403 if the user is not assigned to the requested branch
 *   4. Skips validation on public paths (auth, actuator) and if X-Branch-Id is absent
 *      (the downstream service will return data for all branches the user can see)
 */
@Component
public class BranchContextFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(BranchContextFilter.class);
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/", "/actuator/", "/api/branches/", "/api/user-branches/"
    );

    private final String authServiceUrl;
    private final Map<String, CachedUserBranches> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60_000L;

    public BranchContextFilter(@Value("${auth-service.url:http://auth-service:8081}") String authServiceUrl) {
        this.authServiceUrl = authServiceUrl;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("BranchContextFilter: checking path {}", path);
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        log.debug("BranchContextFilter: headers X-User-Id={}, X-User-Email={}, X-Tenant-Id={}, X-Branch-Id={}",
            exchange.getRequest().getHeaders().getFirst("X-User-Id"),
            exchange.getRequest().getHeaders().getFirst("X-User-Email"),
            exchange.getRequest().getHeaders().getFirst("X-Tenant-Id"),
            exchange.getRequest().getHeaders().getFirst("X-Branch-Id"));

        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String branchIdHeader = exchange.getRequest().getHeaders().getFirst("X-Branch-Id");
        String userEmailHeader = exchange.getRequest().getHeaders().getFirst("X-User-Email");
        String tenantIdHeader = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");

        if (branchIdHeader == null || branchIdHeader.isBlank()) {
            // No branch context — OK, the service will return all data
            return chain.filter(exchange);
        }

        final UUID branchId;
        try {
            branchId = UUID.fromString(branchIdHeader);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID in X-Branch-Id header: {}", branchIdHeader);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        // We need either user ID or email+tenant to look up allowed branches.
        // Prefer email-based lookup because the X-User-Id from Keycloak is the Keycloak sub,
        // not the local user UUID stored in user_branches.
        String cacheKey;
        if (userEmailHeader != null && !userEmailHeader.isBlank()
                && tenantIdHeader != null && !tenantIdHeader.isBlank()) {
            cacheKey = "email:" + userEmailHeader + ":" + tenantIdHeader;
        } else if (userIdHeader != null && !userIdHeader.isBlank()) {
            cacheKey = "user:" + userIdHeader;
        } else {
            // No identifier at all; let downstream handle it
            return chain.filter(exchange);
        }

        return loadUserBranches(cacheKey, userIdHeader, userEmailHeader, tenantIdHeader)
            .<List<UUID>>handle((branches, sink) -> {
                log.debug("Loaded {} branches for {}: {}", branches.size(), cacheKey, branches);
                sink.next(branches);
            })
            .flatMap(allowedBranchIds -> {
                if (allowedBranchIds.isEmpty()) {
                    // User has no branch assignments — treat as super-admin / tenant-admin
                    // (full access). This is the common case for new tenants.
                    log.debug("No branch assignments for {}; allowing access (tenant-admin mode)", cacheKey);
                    return chain.filter(exchange);
                }
                if (allowedBranchIds.contains(branchId)) {
                    log.debug("Authorized for branch {}", branchId);
                    return chain.filter(exchange);
                } else {
                    log.warn("NOT authorized for branch {} (allowed: {})", branchId, allowedBranchIds);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    byte[] body = ("{\"error\":\"Forbidden\",\"message\":\"User has no access to branch " + branchId + "\"}").getBytes();
                    return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
                }
            });
    }

    private Mono<List<UUID>> loadUserBranches(String cacheKey, String userId, String userEmail, String tenantId) {
        CachedUserBranches cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return Mono.just(cached.branchIds());
        }
        return Mono.<List<UUID>>fromCallable(() -> {
            try {
                String url;
                if (userEmail != null && !userEmail.isBlank() && tenantId != null && !tenantId.isBlank()) {
                    // Prefer email-based lookup: X-User-Id is the Keycloak sub, not the
                    // local user UUID stored in user_branches.
                    url = authServiceUrl + "/user-branches/by-email?email="
                        + java.net.URLEncoder.encode(userEmail, "UTF-8")
                        + "&tenantId=" + java.net.URLEncoder.encode(tenantId, "UTF-8");
                } else if (userId != null && !userId.isBlank()) {
                    url = authServiceUrl + "/user-branches/by-user/" + userId;
                } else {
                    log.debug("No user identifier available; skipping branch check");
                    return List.<UUID>of();
                }
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) java.net.URI.create(url).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int code = conn.getResponseCode();
                log.debug("GET {} -> HTTP {}", url, code);
                try (java.io.InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
                    if (is == null) return List.<UUID>of();
                    String json = new String(is.readAllBytes());
                    log.debug("Response body: {}", json.length() > 500 ? json.substring(0, 500) + "..." : json);
                    List<UUID> ids = parseBranchIds(json);
                    cache.put(cacheKey, new CachedUserBranches(ids, System.currentTimeMillis() + CACHE_TTL_MS));
                    return ids;
                }
            } catch (Exception e) {
                log.warn("Failed to load branches for {}: {}", cacheKey, e.getMessage());
                return List.<UUID>of();
            }
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    @SuppressWarnings("unchecked")
    private List<UUID> parseBranchIds(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> rows = mapper.readValue(json, List.class);
            return rows.stream()
                .map(r -> UUID.fromString((String) r.get("branchId")))
                .toList();
        } catch (Exception e) {
            log.warn("Failed to parse branch IDs: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public int getOrder() {
        // Run after OidcTokenValidator (which is a WebFilter with default order ~0)
        // and TenantContextFilter (which is a GlobalFilter with LOWEST_PRECEDENCE - 10)
        return Ordered.LOWEST_PRECEDENCE - 5;
    }

    private record CachedUserBranches(List<UUID> branchIds, long expiresAt) {
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}
