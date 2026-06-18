package com.reportsystem.payment.infrastructure.persistence.repository;

import com.reportsystem.payment.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaGatewayLogRepository extends JpaRepository<GatewayLogEntity, UUID> {}
