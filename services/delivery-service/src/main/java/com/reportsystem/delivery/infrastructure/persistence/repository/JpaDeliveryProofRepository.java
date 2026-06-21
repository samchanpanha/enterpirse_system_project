package com.reportsystem.delivery.infrastructure.persistence.repository;

import com.reportsystem.delivery.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDeliveryProofRepository extends JpaRepository<DeliveryProofEntity, UUID> {
    List<DeliveryProofEntity> findByDeliveryId(UUID deliveryId);
}
