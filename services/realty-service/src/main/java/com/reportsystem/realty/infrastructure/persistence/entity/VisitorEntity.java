package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "visitors")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VisitorEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "property_id")
    private UUID propertyId;
    @Column(nullable = false)
    private String name;
    private String phone;
    @Column(name = "id_number")
    private String idNumber;
    @Column(name = "vehicle_plate")
    private String vehiclePlate;
    private String purpose;
    @Column(name = "check_in")
    private Instant checkIn;
    @Column(name = "check_out")
    private Instant checkOut;
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;
    private String status;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
