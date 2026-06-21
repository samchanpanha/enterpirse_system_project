package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "packages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ParcelEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "resident_id")
    private UUID residentId;
    private String carrier;
    @Column(name = "tracking_number")
    private String trackingNumber;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String status;
    @Column(name = "received_at")
    private Instant receivedAt;
    @Column(name = "picked_up_at")
    private Instant pickedUpAt;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
