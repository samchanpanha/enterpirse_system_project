package com.reportsystem.property.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leases")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "unit_id", nullable = false)
    private UUID unitId;
    @Column(name = "tenant_name", nullable = false)
    private String tenantName;
    @Column(name = "tenant_phone")
    private String tenantPhone;
    @Column(name = "tenant_email")
    private String tenantEmail;
    @Column(name = "id_type")
    private String idType;
    @Column(name = "id_number")
    private String idNumber;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "rent_amount", nullable = false)
    private BigDecimal rentAmount;
    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;
    @Column(name = "payment_day")
    private Integer paymentDay;
    private String status;
    @JdbcTypeCode(SqlTypes.JSON)

    @Column(columnDefinition = "jsonb default '{}'")
    private String documents;
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
