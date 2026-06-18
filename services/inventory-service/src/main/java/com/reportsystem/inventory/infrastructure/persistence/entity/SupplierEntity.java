package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "suppliers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SupplierEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String name; @Column(name = "contact_person") private String contactPerson;
    private String phone; private String email; private String address;
    @Column(name = "tax_number") private String taxNumber;
    @Column(name = "payment_terms") private String paymentTerms;
    private String currency; @Column(name = "is_active") private boolean active;
    private String notes;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}