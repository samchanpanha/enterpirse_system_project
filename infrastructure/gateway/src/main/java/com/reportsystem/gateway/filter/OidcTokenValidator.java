package com.reportsystem.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

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
        "/api/auth/login", "/api/auth/register", "/api/auth/refresh"
    );

    private final String keycloakUrl;
    private final java.util.Properties realmTenantMapping;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedJwks> jwksCache = new ConcurrentHashMap<>();
    private static final long JWKS_TTL_MS = 5 * 60 * 1000L;

    public OidcTokenValidator(
        @Value("${keycloak.url:http://localhost:8180}") String keycloakUrl,
        @Value("${keycloak.realm-tenant-mapping:}") String mapping
    ) {
        this.keycloakUrl = keycloakUrl;
        this.realmTenantMapping = new java.util.Properties();
        if (mapping != null && !mapping.isBlank()) {
            // Format: "demo-corp=00000000-0000-0000-0000-000000000001,acme=..."
            for (String pair : mapping.split(",")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    realmTenantMapping.setProperty(kv[0].trim(), kv[1].trim());
                }
            }
        }
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
        try {
            String realm = extractRealmFromToken(token);
            String kid = extractKidFromToken(token);
            log.debug("Token claims: realm={}, kid={}", realm, kid);
            if (realm == null) {
                log.warn("Token has no realm in issuer claim");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            if (kid == null) {
                log.warn("Token has no kid in header");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            PublicKey publicKey = getRealmPublicKey(realm, kid);
            Jws<Claims> jws = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            String issuer = claims.getIssuer();
            // Accept both internal (keycloak:8080) and external (localhost:8180) issuer URLs
            if (issuer == null || (!issuer.equals(keycloakUrl + "/realms/" + realm) && !issuer.endsWith("/realms/" + realm))) {
                log.warn("Issuer mismatch: expected to end with /realms/{} but was {}", realm, issuer);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String tenantId = claims.get("tenantId", String.class);
            String userId = claims.getSubject();
            String branchId = claims.get("defaultBranchId", String.class);

            // Fallback: derive tenantId from realm mapping if not in token
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = realmTenantMapping.getProperty(realm);
            }

            if (tenantId == null || tenantId.isBlank()) {
                log.warn("Token has no tenantId claim and realm '{}' not in mapping", realm);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Allow ?branchId=X in the query string to override the default branch
            // (used by the frontend branch selector to scope requests to a branch)
            String queryBranchId = exchange.getRequest().getQueryParams().getFirst("branchId");
            if (queryBranchId != null && !queryBranchId.isBlank()) {
                branchId = queryBranchId;
            }

            exchange.getRequest().mutate()
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-Branch-Id", branchId != null ? branchId : "")
                .header("X-Tenant-Slug", realm)
                .header("X-User-Email", claims.get("email", String.class) != null ? claims.get("email", String.class) : "")
                .header("X-User-Name", claims.get("name", String.class) != null ? claims.get("name", String.class) : "")
                .build();

            log.debug("Authenticated user {} for tenant {} branch {} (realm {})", userId, tenantId, branchId, realm);
            return chain.filter(exchange);

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

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String extractRealmFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            payload += "=".repeat((4 - payload.length() % 4) % 4);
            String json = new String(Base64.getUrlDecoder().decode(payload));
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

    private String extractKidFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 1) return null;
            String header = parts[0];
            int padding = (4 - header.length() % 4) % 4;
            header += "=".repeat(padding);
            String json = new String(Base64.getUrlDecoder().decode(header));
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(json, Map.class);
            return (String) claims.get("kid");
        } catch (Exception e) {
            log.warn("Failed to extract kid: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private PublicKey getRealmPublicKey(String realm, String kid) throws Exception {
        CachedJwks cached = jwksCache.get(realm);
        if (cached != null && !cached.isExpired() && cached.jwks().containsKey(kid)) {
            return cached.jwks().get(kid);
        }

        // Refresh JWKS
        String jwksUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        String response = httpGet(jwksUrl);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) body.get("keys");

        Map<String, PublicKey> newJwks = new ConcurrentHashMap<>();
        for (Map<String, Object> key : keys) {
            String keyKid = (String) key.get("kid");
            String kty = (String) key.get("kty");
            if (!"RSA".equals(kty)) continue;
            String n = (String) key.get("n");
            String e = (String) key.get("e");
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(spec);
            newJwks.put(keyKid, pk);
        }

        jwksCache.put(realm, new CachedJwks(newJwks, System.currentTimeMillis() + JWKS_TTL_MS));
        if (!newJwks.containsKey(kid)) {
            throw new IllegalStateException("kid not found in JWKS: " + kid);
        }
        return newJwks.get(kid);
    }

    private String httpGet(String url) throws Exception {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) java.net.URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        try (java.io.InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes());
        }
    }

    private record CachedJwks(Map<String, PublicKey> jwks, long expiresAt) {
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}
