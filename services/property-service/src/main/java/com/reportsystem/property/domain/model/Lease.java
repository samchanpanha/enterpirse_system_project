package com.reportsystem.property.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Lease {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID unitId;
    private String tenantName;
    private String tenantPhone;
    private String tenantEmail;
    private String idType;
    private String idNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private Integer paymentDay;
    private String status;
    private String documents;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    public void activate() { this.status = "active"; }
    public void terminate() { this.status = "terminated"; }
    public void expire() { this.status = "expired"; }
}
