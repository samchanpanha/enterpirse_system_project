package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class User {
    private final UUID id;
    private final UUID tenantId;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
    private boolean active;
    private Instant lastLoginAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public void updateProfile(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void markLoggedIn() {
        this.lastLoginAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }
}
