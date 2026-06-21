package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "hoa_fees")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HoaFeeEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "property_id")
    private UUID propertyId;
    @Column(name = "resident_id")
    private UUID residentId;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    private String period;
    private String status;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "payment_ref", length = 100)
    private String paymentRef;
    @Column(name = "late_fee", precision = 12, scale = 2)
    private BigDecimal lateFee;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
