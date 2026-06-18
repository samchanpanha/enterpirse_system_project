package com.reportsystem.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_branches")
@Getter
@Setter
@NoArgsConstructor
public class UserBranchEntity {

    @EmbeddedId
    private UserBranchId id;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserBranchEntity(UUID userId, UUID branchId, String role, boolean isDefault) {
        this.id = new UserBranchId(userId, branchId);
        this.role = role;
        this.isDefault = isDefault;
        this.createdAt = Instant.now();
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBranchId implements Serializable {
        @Column(name = "user_id")
        private UUID userId;
        @Column(name = "branch_id")
        private UUID branchId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserBranchId that)) return false;
            return Objects.equals(userId, that.userId) && Objects.equals(branchId, that.branchId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, branchId);
        }
    }
}
