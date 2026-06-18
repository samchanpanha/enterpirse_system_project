package com.reportsystem.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "payment_transactions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "transaction_id") private String transactionId;
    @Column(name = "invoice_id") private UUID invoiceId;
    private BigDecimal amount; private String currency;
    private String gateway; @Column(name = "gateway_ref") private String gatewayRef;
    private String method; private String status;
    @Column(name = "customer_name") private String customerName;
    @Column(name = "customer_phone") private String customerPhone;
    @Column(name = "source_type") private String sourceType;
    @Column(name = "source_id") private UUID sourceId;
    @Column(columnDefinition = "jsonb default '{}'") private String metadata;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}