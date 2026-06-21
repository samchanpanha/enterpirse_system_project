package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "amenity_bookings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AmenityBookingEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "resident_id")
    private UUID residentId;
    @Column(name = "amenity_type", nullable = false)
    private String amenityType;
    @Column(name = "booked_date", nullable = false)
    private LocalDate bookedDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    private Integer guests;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
