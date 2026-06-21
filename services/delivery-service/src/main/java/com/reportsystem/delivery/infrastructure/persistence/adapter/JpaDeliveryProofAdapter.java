package com.reportsystem.delivery.infrastructure.persistence.adapter;

import com.reportsystem.delivery.domain.model.DeliveryProof;
import com.reportsystem.delivery.domain.port.outbound.DeliveryProofRepository;
import com.reportsystem.delivery.infrastructure.persistence.entity.DeliveryProofEntity;
import com.reportsystem.delivery.infrastructure.persistence.repository.JpaDeliveryProofRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaDeliveryProofAdapter implements DeliveryProofRepository {

    private final JpaDeliveryProofRepository repo;
    public JpaDeliveryProofAdapter(JpaDeliveryProofRepository repo) { this.repo = repo; }

    @Override
    public DeliveryProof save(DeliveryProof proof) {
        DeliveryProofEntity e = toEntity(proof);
        DeliveryProofEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<DeliveryProof> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<DeliveryProof> findByDeliveryId(UUID deliveryId) {
        return repo.findByDeliveryId(deliveryId).stream().map(this::toDomain).toList();
    }

    private DeliveryProofEntity toEntity(DeliveryProof p) {
        DeliveryProofEntity e = new DeliveryProofEntity();
        e.setId(p.getId());
        e.setDeliveryId(p.getDeliveryId());
        e.setProofType(p.getProofType());
        e.setImageUrl(p.getImageUrl());
        e.setSignatureData(p.getSignatureData());
        e.setNotes(p.getNotes());
        e.setCapturedAt(p.getCapturedAt());
        e.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt() : Instant.now());
        return e;
    }

    private DeliveryProof toDomain(DeliveryProofEntity e) {
        return DeliveryProof.builder()
                .id(e.getId()).deliveryId(e.getDeliveryId())
                .proofType(e.getProofType()).imageUrl(e.getImageUrl())
                .signatureData(e.getSignatureData()).notes(e.getNotes())
                .capturedAt(e.getCapturedAt()).createdAt(e.getCreatedAt())
                .build();
    }
}
