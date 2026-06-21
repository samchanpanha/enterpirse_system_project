package com.reportsystem.delivery.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "delivery_proofs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryProofEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;
    @Column(name = "proof_type")
    private String proofType;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "signature_data")
    private String signatureData;
    private String notes;
    @Column(name = "captured_at")
    private Instant capturedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
