package com.reportsystem.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
    private static final long ACCESS_EXPIRATION = 3_600_000L;       // 1h
    private static final long REFRESH_EXPIRATION = 604_800_000L;      // 7d

    private JwtTokenProvider jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtTokenProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("generateAccessToken produces a valid JWT with expected claims")
    void generateAccessToken_claims() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String token = jwt.generateAccessToken(userId, tenantId, "alice@demo.com", List.of("ADMIN", "USER"));

        assertThat(jwt.isTokenValid(token)).isTrue();
        assertThat(jwt.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwt.getTenantIdFromToken(token)).isEqualTo(tenantId);
        assertThat(jwt.getRolesFromToken(token)).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("generateRefreshToken is a distinct token from access tokens")
    void generateRefreshToken_distinct() {
        UUID userId = UUID.randomUUID();
        String access = jwt.generateAccessToken(userId, UUID.randomUUID(), "x@x", List.of());
        String refresh = jwt.generateRefreshToken(userId);

        assertThat(access).isNotEqualTo(refresh);
        assertThat(jwt.isTokenValid(refresh)).isTrue();
        assertThat(jwt.getUserIdFromToken(refresh)).isEqualTo(userId);
    }

    @Test
    @DisplayName("token with wrong secret is rejected")
    void wrongSecret_rejected() {
        JwtTokenProvider other = new JwtTokenProvider(
            "different-secret-key-that-is-also-at-least-256-bits-long", ACCESS_EXPIRATION, REFRESH_EXPIRATION);
        String token = jwt.generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), "x", List.of());

        assertThat(other.isTokenValid(token)).isFalse();
        assertThatThrownBy(() -> other.getUserIdFromToken(token))
            .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    @DisplayName("expired token is rejected")
    void expiredToken_rejected() {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L, 1L);
        String token = shortLived.generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), "x", List.of());
        // Wait for expiration
        try { Thread.sleep(50); } catch (InterruptedException e) { /* ignore */ }

        assertThat(shortLived.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("garbage token is rejected")
    void garbageToken_rejected() {
        assertThat(jwt.isTokenValid("not-a-jwt")).isFalse();
        assertThat(jwt.isTokenValid("a.b.c")).isFalse();
        assertThat(jwt.isTokenValid("")).isFalse();
    }
}
