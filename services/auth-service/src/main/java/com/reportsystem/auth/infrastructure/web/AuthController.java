package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.Tenant;
import com.reportsystem.auth.domain.model.User;
import com.reportsystem.auth.domain.model.UserBranch;
import com.reportsystem.auth.domain.service.AuthService;
import com.reportsystem.auth.domain.service.TenantService;
import com.reportsystem.auth.domain.service.UserBranchService;
import com.reportsystem.auth.domain.service.UserService;
import com.reportsystem.auth.infrastructure.web.dto.LoginRequest;
import com.reportsystem.auth.infrastructure.web.dto.LoginResponse;
import com.reportsystem.auth.infrastructure.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TenantService tenantService;
    private final UserService userService;
    private final UserBranchService userBranchService;

    public AuthController(AuthService authService, TenantService tenantService, UserService userService,
                          UserBranchService userBranchService) {
        this.authService = authService;
        this.tenantService = tenantService;
        this.userService = userService;
        this.userBranchService = userBranchService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Tenant tenant = tenantService.getTenantBySlug(request.tenantSlug())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + request.tenantSlug()));

        User user = authService.register(tenant.getId(), request.email(), request.password(),
                request.firstName(), request.lastName());

        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(
                accessToken, refreshToken, "Bearer", 3600,
                new LoginResponse.UserInfo(
                        user.getId().toString(), user.getEmail(),
                        user.getFirstName(), user.getLastName(),
                        tenant.getId().toString())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.email(), request.password())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        userService.getUserById(user.getId()).ifPresent(u -> {
            User updated = User.builder()
                    .id(u.getId())
                    .tenantId(u.getTenantId())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .phone(u.getPhone())
                    .locale(u.getLocale())
                    .active(u.isActive())
                    .lastLoginAt(java.time.Instant.now())
                    .createdAt(u.getCreatedAt())
                    .updatedAt(java.time.Instant.now())
                    .build();
        });

        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        return ResponseEntity.ok()
                .header("X-Auth-Deprecation", "true")
                .header("X-Auth-Migration", "Use Keycloak OIDC: http://localhost:8180/realms/demo-corp")
                .body(new LoginResponse(
                        accessToken, refreshToken, "Bearer", 3600,
                        new LoginResponse.UserInfo(
                                user.getId().toString(), user.getEmail(),
                                user.getFirstName(), user.getLastName(),
                                user.getTenantId().toString())));
    }

    /**
     * SSO bridge: accepts a Keycloak access token (validated by the gateway),
     * returns our legacy JWT format for backward compatibility.
     * Auto-provisions the user in auth_db on first login (JIT provisioning).
     * On first-time provisioning, also auto-assigns the user to their default
     * branch (from the X-Branch-Id header) so the gateway's BranchContextFilter
     * can validate their access.
     * Headers set by OidcTokenValidator:
     *   X-Tenant-Id, X-User-Id, X-Tenant-Slug, X-Branch-Id, X-User-Email, X-User-Name
     */
    @PostMapping("/sso-login")
    public ResponseEntity<?> ssoLogin(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-Tenant-Slug", required = false) String tenantSlug,
            @RequestHeader(value = "X-Branch-Id", required = false) String defaultBranchId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        if (userId == null || tenantId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized",
                "message", "SSO login requires valid Keycloak token (X-User-Id and X-Tenant-Id headers)"
            ));
        }
        // Auto-provision if user doesn't exist
        UUID userUuid = UUID.fromString(userId);
        UUID tenantUuid = UUID.fromString(tenantId);
        User user = userService.getUserById(userUuid).orElse(null);
        if (user == null && userEmail != null) {
            user = userService.getUserByEmail(tenantUuid, userEmail).orElse(null);
        }
        boolean isNewUser = (user == null);
        if (isNewUser) {
            // First-time SSO login — provision the user
            String[] nameParts = (userName != null ? userName : "SSO User").split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            user = authService.register(tenantUuid,
                userEmail != null ? userEmail : userId + "@keycloak.local",
                UUID.randomUUID().toString(), // random password (won't be used; Keycloak handles auth)
                firstName, lastName);
        }
        // Auto-assign to default branch on first login (or on every login if no assignments exist)
        if (defaultBranchId != null && !defaultBranchId.isBlank()) {
            UUID branchUuid = UUID.fromString(defaultBranchId);
            try {
                var existingBranches = userBranchService.getUserBranches(user.getId());
                if (existingBranches.isEmpty()) {
                    userBranchService.assign(user.getId(), branchUuid, "USER", true);
                }
            } catch (Exception e) {
                // Assignment may already exist (e.g. on subsequent logins); ignore
            }
        }
        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);
        return ResponseEntity.ok(new LoginResponse(
                accessToken, refreshToken, "Bearer", 3600,
                new LoginResponse.UserInfo(
                        user.getId().toString(), user.getEmail(),
                        user.getFirstName(), user.getLastName(),
                        user.getTenantId().toString())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }
        String accessToken = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", accessToken, "tokenType", "Bearer", "expiresIn", 3600));
    }
}
