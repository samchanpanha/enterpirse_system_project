package com.reportsystem.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportsystem.shared.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.core.ParameterizedTypeReference;

/**
 * OIDC token validator for Keycloak JWTs.
 *
 * - Extracts bearer token from Authorization header
 * - Fetches JWKS from Keycloak for the realm (cached 5min)
 * - Validates signature, expiration, issuer
 * - Extracts tenant_id, branch_ids, user_id, roles from claims
 * - Forwards as X-Tenant-Id, X-Branch-Id, X-User-Id, X-User-Roles headers
 *
 * Public paths (/api/auth/*) bypass validation.
 */
@Component
public class OidcTokenValidator implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(OidcTokenValidator.class);
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/actuator"
    );

    private final String keycloakUrl;
    private final java.util.Properties realmTenantMapping;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedJwks> jwksCache = new ConcurrentHashMap<>();
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;
    private static final long JWKS_TTL_MS = 5 * 60 * 1000L;

    private final Map<String, CachedValidation> validationCache;

    private record CachedValidation(Claims claims, String realm, String tenantId, String branchId, long expiresAt) {
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }

    public OidcTokenValidator(
        @Value("${keycloak.url:http://localhost:8180}") String keycloakUrl,
        @Value("${keycloak.realm-tenant-mapping:}") String mapping,
        JwtTokenProvider jwtTokenProvider,
        WebClient webClient
    ) {
        this.keycloakUrl = keycloakUrl;
        this.jwtTokenProvider = jwtTokenProvider;
        this.webClient = webClient;
        this.realmTenantMapping = new java.util.Properties();
        if (mapping != null && !mapping.isBlank()) {
            for (String pair : mapping.split(",")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    realmTenantMapping.setProperty(kv[0].trim(), kv[1].trim());
                }
            }
        }
        this.validationCache = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, CachedValidation> eldest) {
                return size() > 10000;  // 10K entries max
            }
        };
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // Check validation cache (keyed by SHA-256 of token)
        String cacheKey = sha256(token);
        synchronized (validationCache) {
            CachedValidation cached = validationCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                log.debug("JWT cache hit for token hash {}", cacheKey.substring(0, 12));
                String branchId = cached.branchId();
                String existingBranchId = exchange.getRequest().getHeaders().getFirst("X-Branch-Id");
                String queryBranchId = exchange.getRequest().getQueryParams().getFirst("branchId");
                if (existingBranchId != null && !existingBranchId.isBlank()) {
                    branchId = existingBranchId;
                } else if (queryBranchId != null && !queryBranchId.isBlank()) {
                    branchId = queryBranchId;
                }
                return setHeadersAndContinue(exchange, chain, cached.claims(), cached.realm(), cached.tenantId(), branchId);
            }
        }

        try {
            // Parse header once to extract alg, kid, and realm
            String[] parts = token.split("\\.");
            if (parts.length < 2) throw new JwtException("Malformed token");
            String headerJson = decodeBase64Json(parts[0]);
            @SuppressWarnings("unchecked")
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String alg = (String) header.get("alg");
            String kid = (String) header.get("kid");

            // HMAC token → legacy path (much faster than RSA)
            if (alg != null && alg.startsWith("HS")) {
                return validateLegacyToken(exchange, chain, token);
            }

            String realm = extractRealm(parts[1]);
            log.debug("Token: alg={}, realm={}, kid={}", alg, realm, kid);
            if (realm == null || kid == null) {
                return validateLegacyToken(exchange, chain, token);
            }

            return getRealmPublicKey(realm, kid)
                .flatMap(publicKey -> validateOidcToken(exchange, chain, token, cacheKey, realm, publicKey))
                .onErrorResume(JwtException.class, e -> jwtValidationFailed(exchange, e))
                .onErrorResume(IllegalStateException.class, e -> jwtValidationFailed(exchange, e))
                .onErrorResume(Exception.class, e -> tokenValidationError(exchange, e));

        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Mono<Void> setHeadersAndContinue(ServerWebExchange exchange, WebFilterChain chain,
                                              Claims claims, String realm, String tenantId, String branchId) {
        String userId = claims.getSubject();
        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(exchange.getRequest().mutate()
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-Branch-Id", branchId != null ? branchId : "")
                .header("X-Tenant-Slug", realm)
                .header("X-User-Email", claims.get("email", String.class) != null ? claims.get("email", String.class) : "")
                .header("X-User-Name", claims.get("name", String.class) != null ? claims.get("name", String.class) : "")
                .build())
            .build();
        log.debug("Authenticated user {} for tenant {} branch {} (realm {})", userId, tenantId, branchId, realm);
        return chain.filter(mutatedExchange);
    }

    private Mono<Void> validateOidcToken(ServerWebExchange exchange, WebFilterChain chain,
                                         String token, String cacheKey, String realm, PublicKey publicKey) {
        Jws<Claims> jws = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token);

        Claims claims = jws.getPayload();
        String issuer = claims.getIssuer();
        if (issuer == null || (!issuer.equals(keycloakUrl + "/realms/" + realm) && !issuer.endsWith("/realms/" + realm))) {
            throw new JwtException("Issuer mismatch: expected to end with /realms/" + realm + " but was " + issuer);
        }

        String tenantId = claims.get("tenantId", String.class);
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = realmTenantMapping.getProperty(realm);
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new JwtException("Token has no tenantId claim and realm '" + realm + "' not in mapping");
        }

        String branchId = claims.get("defaultBranchId", String.class);
        String existingBranchId = exchange.getRequest().getHeaders().getFirst("X-Branch-Id");
        String queryBranchId = exchange.getRequest().getQueryParams().getFirst("branchId");
        if (existingBranchId != null && !existingBranchId.isBlank()) {
            branchId = existingBranchId;
        } else if (queryBranchId != null && !queryBranchId.isBlank()) {
            branchId = queryBranchId;
        }

        long exp = claims.getExpiration() != null ? claims.getExpiration().getTime() : System.currentTimeMillis() + 3600_000L;
        synchronized (validationCache) {
            validationCache.put(cacheKey, new CachedValidation(claims, realm, tenantId, branchId, exp));
        }

        return setHeadersAndContinue(exchange, chain, claims, realm, tenantId, branchId);
    }

    private Mono<Void> validateLegacyToken(ServerWebExchange exchange, WebFilterChain chain, String token) {
        try {
            Claims claims = jwtTokenProvider.validateToken(token);
            String tenantId = claims.get("tenantId", String.class);
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);

            if (tenantId == null || tenantId.isBlank()) {
                log.warn("Legacy JWT has no tenantId claim");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Allow ?branchId=X in the query string or X-Branch-Id header to set the branch context
            String existingBranchId = exchange.getRequest().getHeaders().getFirst("X-Branch-Id");
            String queryBranchId = exchange.getRequest().getQueryParams().getFirst("branchId");
            String branchId = "";
            if (existingBranchId != null && !existingBranchId.isBlank()) {
                branchId = existingBranchId;
            } else if (queryBranchId != null && !queryBranchId.isBlank()) {
                branchId = queryBranchId;
            }

            ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                    .header("X-Tenant-Id", tenantId)
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-Branch-Id", branchId)
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-User-Name", email != null ? email : "")
                    .build())
                .build();

            log.debug("Authenticated legacy user {} for tenant {}", userId, tenantId);
            return chain.filter(mutatedExchange);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Legacy JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String decodeBase64Json(String part) {
        int padding = (4 - part.length() % 4) % 4;
        String padded = part + "=".repeat(padding);
        return new String(Base64.getUrlDecoder().decode(padded));
    }

    private String extractRealm(String payloadPart) {
        try {
            String json = decodeBase64Json(payloadPart);
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(json, Map.class);
            String issuer = (String) claims.get("iss");
            if (issuer == null) return null;
            int realmStart = issuer.lastIndexOf("/realms/") + 8;
            if (realmStart < 8) return null;
            int realmEnd = issuer.indexOf("/", realmStart);
            return realmEnd < 0 ? issuer.substring(realmStart) : issuer.substring(realmStart, realmEnd);
        } catch (Exception e) {
            log.warn("Failed to extract realm: {}", e.getMessage());
            return null;
        }
    }

    private String sha256(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return token;  // fallback
        }
    }

    @SuppressWarnings("unchecked")
    private Mono<PublicKey> getRealmPublicKey(String realm, String kid) {
        CachedJwks cached = jwksCache.get(realm);
        if (cached != null && !cached.isExpired() && cached.jwks().containsKey(kid)) {
            return Mono.just(cached.jwks().get(kid));
        }

        String jwksUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        return fetchJwks(jwksUrl)
            .map(body -> {
                List<Map<String, Object>> keys = (List<Map<String, Object>>) body.get("keys");
                Map<String, PublicKey> newJwks = new ConcurrentHashMap<>();
                if (keys != null) {
                    for (Map<String, Object> key : keys) {
                        String keyKid = (String) key.get("kid");
                        String kty = (String) key.get("kty");
                        if (!"RSA".equals(kty) || keyKid == null) continue;
                        String n = (String) key.get("n");
                        String e = (String) key.get("e");
                        if (n == null || e == null) continue;
                        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
                        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
                        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                        PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(spec);
                        newJwks.put(keyKid, pk);
                    }
                }

                jwksCache.put(realm, new CachedJwks(newJwks, System.currentTimeMillis() + JWKS_TTL_MS));
                if (!newJwks.containsKey(kid)) {
                    throw new IllegalStateException("kid not found in JWKS: " + kid);
                }
                return newJwks.get(kid);
            });
    }

    private Mono<Map<String, Object>> fetchJwks(String url) {
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<>() {})
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
                .maxBackoff(Duration.ofSeconds(1))
                .filter(OidcTokenValidator::isRecoverableJwksFailure));
    }

    private static boolean isRecoverableJwksFailure(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            HttpStatusCode status = responseException.getStatusCode();
            return status.is5xxServerError() || status.value() == 429;
        }
        return true;
    }

    private record CachedJwks(Map<String, PublicKey> jwks, long expiresAt) {
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }

    private Mono<Void> jwtValidationFailed(ServerWebExchange exchange, Throwable throwable) {
        log.warn("JWT validation failed: {}", throwable.getMessage(), throwable);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> tokenValidationError(ServerWebExchange exchange, Throwable throwable) {
        log.error("Token validation error: {}", throwable.getMessage(), throwable);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
