package com.reportsystem.payment.infrastructure.persistence.repository;

import com.reportsystem.payment.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRefundRepository extends JpaRepository<RefundEntity, UUID> { List<RefundEntity> findByTransactionId(UUID id); }
