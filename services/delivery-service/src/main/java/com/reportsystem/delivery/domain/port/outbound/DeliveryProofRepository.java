package com.reportsystem.delivery.domain.port.outbound;

import com.reportsystem.delivery.domain.model.DeliveryProof;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryProofRepository {
    DeliveryProof save(DeliveryProof proof);
    Optional<DeliveryProof> findById(UUID id);
    List<DeliveryProof> findByDeliveryId(UUID deliveryId);
}
