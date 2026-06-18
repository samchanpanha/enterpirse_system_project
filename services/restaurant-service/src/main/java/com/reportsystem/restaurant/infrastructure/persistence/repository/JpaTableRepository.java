package com.reportsystem.restaurant.infrastructure.persistence.repository;

import com.reportsystem.restaurant.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTableRepository extends JpaRepository<TableEntity, UUID> {
    List<TableEntity> findByOutletId(UUID outletId);
}
