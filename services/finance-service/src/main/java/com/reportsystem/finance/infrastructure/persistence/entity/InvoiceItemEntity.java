package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "invoice_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "invoice_id", nullable = false) private UUID invoiceId;
    private String description;
    private BigDecimal quantity; @Column(name = "unit_price") private BigDecimal unitPrice;
    @Column(name = "tax_rate") private BigDecimal taxRate;
    @Column(name = "tax_amount") private BigDecimal taxAmount;
    private BigDecimal total; @Column(name = "account_id") private UUID accountId;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
}