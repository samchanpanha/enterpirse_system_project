package com.reportsystem.delivery.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "fleet_vehicles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FleetVehicleEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "branch_id")
    private UUID branchId;
    @Column(nullable = false)
    private String name;
    @Column(name = "plate_number")
    private String plateNumber;
    @Column(name = "vehicle_type")
    private String vehicleType;
    private String status;
    @Column(name = "capacity_kg")
    private BigDecimal capacityKg;
    @Column(name = "fuel_type")
    private String fuelType;
    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;
    @Column(name = "last_maintenance_at")
    private LocalDate lastMaintenanceAt;
    @Column(name = "next_maintenance_at")
    private LocalDate nextMaintenanceAt;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
