package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "invoices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "invoice_number") private String invoiceNumber;
    @Column(name = "invoice_type") private String invoiceType;
    @Column(name = "source_type") private String sourceType;
    @Column(name = "source_id") private UUID sourceId;
    @Column(name = "customer_name") private String customerName;
    @Column(name = "customer_tin") private String customerTin;
    @Column(name = "issue_date") private LocalDate issueDate;
    @Column(name = "due_date") private LocalDate dueDate;
    private BigDecimal subtotal; private BigDecimal discount; private BigDecimal taxAmount;
    private BigDecimal total; @Column(name = "amount_paid") private BigDecimal amountPaid;
    @Column(name = "balance_due") private BigDecimal balanceDue;
    private String status; private String currency; private String notes;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}