package com.reportsystem.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "gateway_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GatewayLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "transaction_id") private UUID transactionId;
    private String gateway; @Column(name = "request_body", columnDefinition = "TEXT") private String requestBody;
    @Column(name = "response_body", columnDefinition = "TEXT") private String responseBody;
    @Column(name = "http_status") private Integer httpStatus;
    @Column(name = "duration_ms") private Integer durationMs;
    private Boolean success; @Column(name = "error_message") private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}