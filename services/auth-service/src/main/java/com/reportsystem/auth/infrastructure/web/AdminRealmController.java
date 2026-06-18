package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.infrastructure.keycloak.KeycloakAdminClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for super-admin operations like creating a new tenant + Keycloak realm
 * in a single transaction.
 *
 * In production this should be locked down to a super-admin role (e.g. via an
 * X-Super-Admin header or a specific realm). For MVP, the endpoints are open.
 */
@RestController
@RequestMapping("/admin")
public class AdminRealmController {

    private static final Logger log = LoggerFactory.getLogger(AdminRealmController.class);

    private final KeycloakAdminClient keycloakAdminClient;

    public AdminRealmController(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    /**
     * Create a new Keycloak realm for a tenant and provision the bootstrap admin
     * user plus the report-system clients (web/api/cli) with tenant mappers.
     *
     * Body:
     *   {
     *     "realmName": "acme-corp",
     *     "displayName": "ACME Corp",
     *     "tenantId": "00000000-0000-0000-0000-000000000099",
     *     "adminEmail": "admin@acme.com",
     *     "adminPassword": "ChangeMe123!",
     *     "defaultBranchId": "00000000-0000-0000-0000-000000000010"
     *   }
     */
    @PostMapping("/realms")
    public ResponseEntity<?> createRealm(@RequestBody Map<String, String> body) {
        String realmName = body.get("realmName");
        String displayName = body.get("displayName");
        String tenantId = body.get("tenantId");
        String adminEmail = body.get("adminEmail");
        String adminPassword = body.get("adminPassword");
        String defaultBranchId = body.get("defaultBranchId");
        if (realmName == null || tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Bad Request",
                "message", "realmName and tenantId are required"
            ));
        }
        try {
            var result = keycloakAdminClient.createRealm(realmName, displayName, tenantId,
                adminEmail, adminPassword, defaultBranchId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("realmName", realmName);
            response.put("tenantId", tenantId);
            response.put("created", result.created());
            response.put("adminEmail", adminEmail);
            response.put("message", result.created() ? "Realm created with bootstrap admin and clients" : "Realm already existed");
            if (result.created()) {
                response.put("clientSecrets", Map.of(
                    "report-system-web", result.webClientSecret(),
                    "report-system-api", result.apiClientSecret(),
                    "report-system-cli", result.cliClientSecret()
                ));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create realm {}", realmName, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Create a user in a Keycloak realm (provisions a new tenant admin).
     */
    @PostMapping("/realms/{realmName}/users")
    public ResponseEntity<?> createUser(
            @org.springframework.web.bind.annotation.PathVariable String realmName,
            @RequestBody Map<String, String> body) {
        String email = body.get("email");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String tenantId = body.get("tenantId");
        String defaultBranchId = body.get("defaultBranchId");
        String password = body.getOrDefault("password", "Demo123!");
        if (email == null || tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Bad Request",
                "message", "email and tenantId are required"
            ));
        }
        try {
            String userId = keycloakAdminClient.createUser(realmName, email, firstName, lastName,
                tenantId, defaultBranchId);
            keycloakAdminClient.setUserPassword(realmName, userId, password, false);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email,
                "realm", realmName
            ));
        } catch (Exception e) {
            log.error("Failed to create user {} in realm {}", email, realmName, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Health check for the Keycloak admin API.
     */
    @GetMapping("/keycloak/health")
    public ResponseEntity<?> health() {
        boolean ok = keycloakAdminClient.healthCheck();
        return ResponseEntity.ok(Map.of(
            "keycloak", ok ? "reachable" : "unreachable"
        ));
    }
}
