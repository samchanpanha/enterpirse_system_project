package com.reportsystem.restaurant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "reservations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "outlet_id", nullable = false)
    private UUID outletId;
    @Column(name = "table_id")
    private UUID tableId;
    @Column(name = "customer_id")
    private UUID customerId;
    @Column(name = "guest_name")
    private String guestName;
    @Column(name = "guest_phone")
    private String guestPhone;
    @Column(name = "guest_count", nullable = false)
    private int guestCount;
    @Column(name = "reservation_time", nullable = false)
    private Instant reservationTime;
    @Column(name = "duration_minutes")
    private int durationMinutes;
    private String status;
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
