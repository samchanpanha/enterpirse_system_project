package com.reportsystem.delivery.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "drivers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "branch_id")
    private UUID branchId;
    @Column(nullable = false)
    private String name;
    private String phone;
    private String email;
    @Column(name = "license_number")
    private String licenseNumber;
    @Column(name = "vehicle_type")
    private String vehicleType;
    @Column(name = "vehicle_plate")
    private String vehiclePlate;
    private String status;
    private BigDecimal rating;
    @Column(name = "total_deliveries")
    private Integer totalDeliveries;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "current_lat")
    private BigDecimal currentLat;
    @Column(name = "current_lng")
    private BigDecimal currentLng;
    @Column(name = "last_location_at")
    private Instant lastLocationAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
