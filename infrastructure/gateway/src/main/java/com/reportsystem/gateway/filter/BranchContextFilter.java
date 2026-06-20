package com.reportsystem.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.core.ParameterizedTypeReference;

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
    private final Map<String, CachedUserBranches> cache;
    private static final long CACHE_TTL_MS = 60_000L; // 60s cache as per docs
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    public BranchContextFilter(@Value("${auth-service.url:http://auth-service:8081}") String authServiceUrl,
                               WebClient webClient) {
        this.authServiceUrl = authServiceUrl;
        this.webClient = webClient;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("BranchContextFilter: checking path {}", path);
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String branchIdHeader = exchange.getRequest().getHeaders().getFirst("X-Branch-Id");
        String userEmailHeader = exchange.getRequest().getHeaders().getFirst("X-User-Email");
        String tenantIdHeader = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");

        log.debug("BranchContextFilter: headers X-User-Id={}, X-User-Email={}, X-Tenant-Id={}, X-Branch-Id={}",
            userIdHeader, userEmailHeader, tenantIdHeader, branchIdHeader);

        if (branchIdHeader == null || branchIdHeader.isBlank()) {
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

        String cacheKey;
        if (userEmailHeader != null && !userEmailHeader.isBlank()
                && tenantIdHeader != null && !tenantIdHeader.isBlank()) {
            cacheKey = "email:" + userEmailHeader + ":" + tenantIdHeader;
        } else if (userIdHeader != null && !userIdHeader.isBlank()) {
            cacheKey = "user:" + userIdHeader;
        } else {
            return chain.filter(exchange);
        }

        return loadUserBranches(cacheKey, userIdHeader, userEmailHeader, tenantIdHeader)
            .flatMap(allowedBranchIds -> {
                log.debug("Loaded {} branches for {}: {}", allowedBranchIds.size(), cacheKey, allowedBranchIds);
                if (allowedBranchIds.isEmpty()) {
                    // User has no branch assignments — treat as super-admin / tenant-admin
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
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
                }
            });
    }

    private Mono<List<UUID>> loadUserBranches(String cacheKey, String userId, String userEmail, String tenantId) {
        CachedUserBranches cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return Mono.just(cached.branchIds());
        }
        
        if (userEmail == null || userEmail.isBlank() || tenantId == null || tenantId.isBlank()) {
            if (userId == null || userId.isBlank()) {
                return Mono.just(List.of());
            }
            return fetchBranchesByUserId(userId)
                .doOnNext(ids -> cache.put(cacheKey, new CachedUserBranches(ids, System.currentTimeMillis() + CACHE_TTL_MS)))
                .onErrorResume(e -> {
                    log.warn("Failed to load branches for user {}: {}", userId, e.getMessage());
                    return Mono.just(List.of());
                });
        }
        
        return fetchBranchesByEmail(userEmail, tenantId)
            .doOnNext(ids -> cache.put(cacheKey, new CachedUserBranches(ids, System.currentTimeMillis() + CACHE_TTL_MS)))
            .onErrorResume(e -> {
                log.warn("Failed to load branches for email {}: {}", userEmail, e.getMessage());
                return Mono.just(List.of());
            });
    }

    private Mono<List<UUID>> fetchBranchesByEmail(String email, String tenantId) {
        String url = authServiceUrl + "/user-branches/by-email?email=" + email + "&tenantId=" + tenantId;
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
            .timeout(Duration.ofSeconds(2))
            .retryWhen(Retry.backoff(2, Duration.ofMillis(100))
                .maxBackoff(Duration.ofMillis(500))
                .filter(this::isRecoverableAuthFailure))
            .map(this::parseBranchIds)
            .onErrorReturn(List.of());
    }

    private Mono<List<UUID>> fetchBranchesByUserId(String userId) {
        String url = authServiceUrl + "/user-branches/by-user/" + userId;
        return webClient.get()
            .uri(url.startsWith("http") ? url : "http://" + url)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
            .timeout(Duration.ofSeconds(2))
            .retryWhen(Retry.backoff(2, Duration.ofMillis(100))
                .maxBackoff(Duration.ofMillis(500))
                .filter(this::isRecoverableAuthFailure))
            .map(this::parseBranchIds)
            .onErrorReturn(List.of());
    }

    private List<UUID> parseBranchIds(List<Map<String, Object>> rows) {
        if (rows == null) return List.of();
        return rows.stream()
            .map(r -> UUID.fromString((String) r.get("branchId")))
            .toList();
    }

    private boolean isRecoverableAuthFailure(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 5;
    }

    private record CachedUserBranches(List<UUID> branchIds, long expiresAt) {
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}