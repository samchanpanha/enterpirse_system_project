package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.User;
import com.reportsystem.auth.domain.port.outbound.UserRepository;
import com.reportsystem.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    private static final String JWT_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long ACCESS_TOKEN_EXPIRATION = 3_600_000L;
    private static final long REFRESH_TOKEN_EXPIRATION = 604_800_000L;

    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(JWT_SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
        authService = new AuthService(userRepository, userService, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("register hashes password, lowercases email, and persists user")
    void register_hashesPasswordAndPersists() {
        when(userRepository.existsByEmailAndTenantId("Alice@Demo.com", TENANT_ID)).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(TENANT_ID, "Alice@Demo.com", "Password123!", "Alice", "Smith");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        assertThat(persisted.getEmail()).isEqualTo("alice@demo.com"); // lowercased + trimmed
        assertThat(persisted.getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(persisted.isActive()).isTrue();
        assertThat(persisted.getLocale()).isEqualTo("km");
        assertThat(persisted.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(persisted.getId()).isNotNull();
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    @DisplayName("register rejects duplicate email within same tenant")
    void register_duplicateEmailThrows() {
        when(userRepository.existsByEmailAndTenantId("alice@demo.com", TENANT_ID)).thenReturn(true);

        assertThatThrownBy(() ->
            authService.register(TENANT_ID, "alice@demo.com", "Password123!", "Alice", "Smith"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login returns user when password matches")
    void login_succeeds() {
        User existing = User.builder()
            .id(UUID.randomUUID())
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("$2a$10$hashed")
            .firstName("Alice")
            .lastName("Smith")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();
        when(userRepository.findByEmail("alice@demo.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("Password123!", "$2a$10$hashed")).thenReturn(true);

        Optional<User> result = authService.login("alice@demo.com", "Password123!");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@demo.com");
    }

    @Test
    @DisplayName("login is case-insensitive on email")
    void login_isCaseInsensitive() {
        User existing = User.builder()
            .id(UUID.randomUUID())
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("$2a$10$hashed")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();
        when(userRepository.findByEmail("alice@demo.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("Password123!", "$2a$10$hashed")).thenReturn(true);

        Optional<User> result = authService.login("ALICE@DEMO.COM", "Password123!");

        assertThat(result).isPresent();
        verify(userRepository).findByEmail("alice@demo.com");
    }

    @Test
    @DisplayName("login returns empty when password is wrong")
    void login_wrongPasswordReturnsEmpty() {
        User existing = User.builder()
            .id(UUID.randomUUID())
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("$2a$10$hashed")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();
        when(userRepository.findByEmail("alice@demo.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashed")).thenReturn(false);

        Optional<User> result = authService.login("alice@demo.com", "WrongPassword");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("login returns empty when user not found")
    void login_userNotFoundReturnsEmpty() {
        when(userRepository.findByEmail("nobody@demo.com")).thenReturn(Optional.empty());

        Optional<User> result = authService.login("nobody@demo.com", "Password123!");

        assertThat(result).isEmpty();
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("generateAccessToken produces a valid JWT with correct claims")
    void generateAccessToken_hasCorrectClaims() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("x")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();

        when(userService.getUserRoles(userId)).thenReturn(java.util.List.of());

        String token = authService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getTenantIdFromToken(token)).isEqualTo(TENANT_ID);
        assertThat(jwtTokenProvider.getRolesFromToken(token)).isEmpty();
    }

    @Test
    @DisplayName("generateRefreshToken is valid and yields same user id")
    void generateRefreshToken_isValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("x")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();

        String refresh = authService.generateRefreshToken(user);

        assertThat(jwtTokenProvider.isTokenValid(refresh)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(refresh)).isEqualTo(userId);
    }

    @Test
    @DisplayName("refreshAccessToken issues a new access token for a valid refresh")
    void refreshAccessToken_succeeds() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("x")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();
        String refresh = authService.generateRefreshToken(user);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userService.getUserRoles(userId)).thenReturn(java.util.List.of());

        String newAccess = authService.refreshAccessToken(refresh);

        assertThat(jwtTokenProvider.isTokenValid(newAccess)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(newAccess)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getTenantIdFromToken(newAccess)).isEqualTo(TENANT_ID);
    }

    @Test
    @DisplayName("refreshAccessToken fails for invalid token")
    void refreshAccessToken_invalidTokenThrows() {
        assertThatThrownBy(() -> authService.refreshAccessToken("not-a-real-jwt"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("refreshAccessToken wraps the underlying user-not-found in a generic error")
    void refreshAccessToken_userNotFoundThrows() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .tenantId(TENANT_ID)
            .email("alice@demo.com")
            .passwordHash("x")
            .active(true)
            .createdAt(java.time.Instant.now())
            .build();
        String refresh = authService.generateRefreshToken(user);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // The implementation hides "user not found" behind a generic refresh error
        // to avoid leaking account-existence information.
        assertThatThrownBy(() -> authService.refreshAccessToken(refresh))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or expired refresh token");
    }
}
