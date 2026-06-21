package com.reportsystem.realty.domain.model;

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
public class HoaFee {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID propertyId;
    private UUID residentId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String period;
    private String status;
    private Instant paidAt;
    private String paymentRef;
    private BigDecimal lateFee;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}
