package com.reportsystem.restaurant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "customers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "outlet_id")
    private UUID outletId;
    @Column(nullable = false)
    private String name;
    private String phone;
    private String email;
    private LocalDate birthday;
    @Column(name = "is_vip")
    private boolean vip;
    private String notes;
    @Column(name = "total_visits")
    private int totalVisits;
    @Column(name = "total_spent")
    private BigDecimal totalSpent;
    @Column(name = "last_visit_at")
    private Instant lastVisitAt;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
