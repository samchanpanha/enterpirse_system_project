package com.reportsystem.restaurant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "tables")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "outlet_id", nullable = false)
    private UUID outletId;
    @Column(nullable = false)
    private String label;
    private int capacity;
    private String floor;
    private String section;
    @Column(name = "pos_x")
    private Double posX;
    @Column(name = "pos_y")
    private Double posY;
    private String status;
    @Column(name = "qr_code_url")
    private String qrCodeUrl;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
