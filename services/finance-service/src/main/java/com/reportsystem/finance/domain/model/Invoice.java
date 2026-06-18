package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class Invoice {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String invoiceNumber;
    private String invoiceType;
    private String sourceType;
    private UUID sourceId;
    private String customerName;
    private String customerTin;
    private java.time.LocalDate issueDate;
    private java.time.LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String status;
    private String currency;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}