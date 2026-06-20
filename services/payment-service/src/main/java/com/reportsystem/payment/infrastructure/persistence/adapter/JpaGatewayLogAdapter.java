package com.reportsystem.payment.infrastructure.persistence.adapter;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.outbound.*;
import com.reportsystem.payment.infrastructure.persistence.entity.*;
import com.reportsystem.payment.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaGatewayLogAdapter implements GatewayLogRepository {
    private final JpaGatewayLogRepository repo;
    public JpaGatewayLogAdapter(JpaGatewayLogRepository r) { repo = r; }
    @Override public GatewayLog save(GatewayLog l) {
        GatewayLogEntity e = new GatewayLogEntity(); e.setId(l.getId()); e.setTransactionId(l.getTransactionId());
        e.setBranchId(l.getBranchId()); e.setGateway(l.getGateway()); e.setRequestBody(l.getRequestBody());
        e.setResponseBody(l.getResponseBody()); e.setHttpStatus(l.getHttpStatus()); e.setDurationMs(l.getDurationMs());
        e.setSuccess(l.getSuccess()); e.setErrorMessage(l.getErrorMessage()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    private GatewayLog toDomain(GatewayLogEntity e) { return GatewayLog.builder().id(e.getId()).transactionId(e.getTransactionId()).branchId(e.getBranchId()).gateway(e.getGateway()).requestBody(e.getRequestBody()).responseBody(e.getResponseBody()).httpStatus(e.getHttpStatus()).durationMs(e.getDurationMs()).success(e.getSuccess()).errorMessage(e.getErrorMessage()).createdAt(e.getCreatedAt()).build(); }
}