package com.reportsystem.auth.infrastructure.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Lightweight Keycloak Admin REST client. Used by AdminRealmController to create
 * new realms (one per tenant) and provision initial admin users.
 *
 * Uses the master realm's admin-cli client with password grant to get an access
 * token, then calls the Keycloak Admin API.
 */
@Component
public class KeycloakAdminClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);

    private final String keycloakUrl;
    private final String adminUsername;
    private final String adminPassword;
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile String adminToken;
    private volatile long adminTokenExpiresAt = 0;

    public KeycloakAdminClient(
            @Value("${keycloak.url:http://localhost:8180}") String keycloakUrl,
            @Value("${keycloak.admin.username:admin}") String adminUsername,
            @Value("${keycloak.admin.password:admin}") String adminPassword) {
        this.keycloakUrl = keycloakUrl.replaceAll("/$", "");
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /**
     * Create a new realm with the given name (typically the tenant slug) and
     * provision a bootstrap admin user plus the three report-system clients
     * (web, api, cli) with tenantId/defaultBranchId/realm-roles mappers.
     *
     * Idempotent: returns true if the realm was created, false if it already existed.
     * When the realm already exists no secrets are generated.
     */
    public RealmBootstrapResult createRealm(String realmName, String displayName, String tenantId,
                                             String adminEmail, String adminPassword,
                                             String defaultBranchId) throws Exception {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms";

        String webSecret = generateSecret();
        String apiSecret = generateSecret();
        String cliSecret = generateSecret();

        String payload = loadRealmTemplate()
            .replace("{{realmName}}", realmName)
            .replace("{{displayName}}", displayName != null ? displayName : realmName)
            .replace("{{tenantId}}", tenantId)
            .replace("{{adminEmail}}", adminEmail != null ? adminEmail : "admin@" + realmName + ".local")
            .replace("{{adminPassword}}", adminPassword != null ? adminPassword : generatePassword())
            .replace("{{defaultBranchId}}", defaultBranchId != null ? defaultBranchId : "")
            .replace("{{clientSecretWeb}}", webSecret)
            .replace("{{clientSecretApi}}", apiSecret)
            .replace("{{clientSecretCli}}", cliSecret);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(payload))
            .build();
        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        if (resp.statusCode() == 201) {
            log.info("Created Keycloak realm {} with bootstrap admin and clients", realmName);
            return new RealmBootstrapResult(true, webSecret, apiSecret, cliSecret);
        }
        if (resp.statusCode() == 409) {
            log.info("Keycloak realm {} already exists", realmName);
            return new RealmBootstrapResult(false, null, null, null);
        }
        throw new RuntimeException("Failed to create realm " + realmName + ": HTTP " + resp.statusCode() + " - " + resp.body());
    }

    private String loadRealmTemplate() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/keycloak/realm-template.json")) {
            if (is == null) {
                throw new IllegalStateException("Realm template not found on classpath: /keycloak/realm-template.json");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String generateSecret() {
        return UUID.randomUUID().toString();
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record RealmBootstrapResult(boolean created, String webClientSecret,
                                       String apiClientSecret, String cliClientSecret) {
    }

    /**
     * Create a new user in the given realm.
     */
    public String createUser(String realmName, String email, String firstName, String lastName,
                              String tenantId, String defaultBranchId) throws Exception {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realmName + "/users";

        ObjectNode body = mapper.createObjectNode();
        body.put("username", email);
        body.put("email", email);
        body.put("firstName", firstName != null ? firstName : "");
        body.put("lastName", lastName != null ? lastName : "");
        body.put("enabled", true);
        body.put("emailVerified", true);
        ObjectNode attrs = body.putObject("attributes");
        attrs.putArray("tenantId").add(tenantId);
        if (defaultBranchId != null) {
            attrs.putArray("defaultBranchId").add(defaultBranchId);
        }

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(body.toString()))
            .build();
        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        if (resp.statusCode() != 201) {
            throw new RuntimeException("Failed to create user " + email + " in realm " + realmName
                + ": HTTP " + resp.statusCode() + " - " + resp.body());
        }
        String location = resp.headers().firstValue("Location").orElse("");
        String userId = location.substring(location.lastIndexOf('/') + 1);
        log.info("Created Keycloak user {} in realm {} ({})", email, realmName, userId);
        return userId;
    }

    /**
     * Set a user's password.
     */
    public void setUserPassword(String realmName, String userId, String password, boolean temporary) throws Exception {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realmName + "/users/" + userId + "/reset-password";

        ObjectNode body = mapper.createObjectNode();
        body.put("type", "password");
        body.put("value", password);
        body.put("temporary", temporary);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .PUT(BodyPublishers.ofString(body.toString()))
            .build();
        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        if (resp.statusCode() != 204) {
            throw new RuntimeException("Failed to set password for user " + userId
                + ": HTTP " + resp.statusCode() + " - " + resp.body());
        }
    }

    /**
     * Get the list of client UUIDs for a realm (e.g. to attach protocol mappers).
     */
    public Map<String, String> listClients(String realmName) throws Exception {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realmName + "/clients";
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Failed to list clients in realm " + realmName
                + ": HTTP " + resp.statusCode());
        }
        Map<String, String> result = new java.util.HashMap<>();
        for (JsonNode c : mapper.readTree(resp.body())) {
            result.put(c.get("clientId").asText(), c.get("id").asText());
        }
        return result;
    }

    /**
     * Acquire a master-realm admin access token (caches for 5 minutes).
     */
    private String getAdminToken() throws Exception {
        long now = System.currentTimeMillis();
        if (adminToken != null && now < adminTokenExpiresAt) {
            return adminToken;
        }
        String url = keycloakUrl + "/realms/master/protocol/openid-connect/token";
        String form = "grant_type=password"
            + "&client_id=admin-cli"
            + "&username=" + java.net.URLEncoder.encode(adminUsername, "UTF-8")
            + "&password=" + java.net.URLEncoder.encode(adminPassword, "UTF-8");

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(BodyPublishers.ofString(form))
            .build();
        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Failed to get Keycloak admin token: HTTP " + resp.statusCode() + " - " + resp.body());
        }
        JsonNode json = mapper.readTree(resp.body());
        adminToken = json.get("access_token").asText();
        long expiresIn = json.get("expires_in").asLong();
        adminTokenExpiresAt = now + (expiresIn - 60) * 1000L;
        return adminToken;
    }

    /**
     * Test that the Keycloak admin API is reachable with the configured credentials.
     * Returns true if successful, false otherwise.
     */
    public boolean healthCheck() {
        try {
            String token = getAdminToken();
            return token != null && !token.isBlank();
        } catch (Exception e) {
            log.warn("Keycloak admin health check failed: {}", e.getMessage());
            return false;
        }
    }
}
