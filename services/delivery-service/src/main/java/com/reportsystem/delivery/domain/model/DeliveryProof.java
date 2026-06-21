package com.reportsystem.delivery.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryProof {
    private final UUID id;
    private final UUID deliveryId;
    private String proofType;
    private String imageUrl;
    private String signatureData;
    private String notes;
    private Instant capturedAt;
    private final Instant createdAt;
}
