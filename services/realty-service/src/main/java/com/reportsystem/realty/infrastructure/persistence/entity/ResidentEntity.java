package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "residents")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ResidentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "property_id")
    private UUID propertyId;
    @Column(nullable = false)
    private String name;
    @Column(name = "name_kh")
    private String nameKh;
    private String phone;
    private String email;
    @Column(name = "id_number")
    private String idNumber;
    @Column(name = "move_in_date")
    private LocalDate moveInDate;
    @Column(name = "move_out_date")
    private LocalDate moveOutDate;
    private String status;
    @Column(name = "emergency_contact")
    private String emergencyContact;
    @Column(name = "emergency_phone")
    private String emergencyPhone;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
