package com.reportsystem.property.infrastructure.persistence.mapper;

import com.reportsystem.property.domain.model.Lease;
import com.reportsystem.property.infrastructure.persistence.entity.LeaseEntity;
import org.springframework.stereotype.Component;

@Component
public class LeaseMapper {
    public LeaseEntity toEntity(Lease domain) {
        return LeaseEntity.builder().id(domain.getId()).tenantId(domain.getTenantId()).branchId(domain.getBranchId())
                .unitId(domain.getUnitId()).tenantName(domain.getTenantName())
                .tenantPhone(domain.getTenantPhone()).tenantEmail(domain.getTenantEmail())
                .idType(domain.getIdType()).idNumber(domain.getIdNumber())
                .startDate(domain.getStartDate()).endDate(domain.getEndDate())
                .rentAmount(domain.getRentAmount()).depositAmount(domain.getDepositAmount())
                .paymentDay(domain.getPaymentDay()).status(domain.getStatus())
                .documents(domain.getDocuments()).notes(domain.getNotes())
                .createdAt(domain.getCreatedAt()).updatedAt(domain.getUpdatedAt()).build();
    }
    public Lease toDomain(LeaseEntity entity) {
        return Lease.builder().id(entity.getId()).tenantId(entity.getTenantId()).branchId(entity.getBranchId())
                .unitId(entity.getUnitId()).tenantName(entity.getTenantName())
                .tenantPhone(entity.getTenantPhone()).tenantEmail(entity.getTenantEmail())
                .idType(entity.getIdType()).idNumber(entity.getIdNumber())
                .startDate(entity.getStartDate()).endDate(entity.getEndDate())
                .rentAmount(entity.getRentAmount()).depositAmount(entity.getDepositAmount())
                .paymentDay(entity.getPaymentDay()).status(entity.getStatus())
                .documents(entity.getDocuments()).notes(entity.getNotes())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}
