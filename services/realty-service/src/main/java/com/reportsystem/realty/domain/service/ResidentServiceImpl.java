package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Resident;
import com.reportsystem.realty.domain.port.inbound.ResidentService;
import com.reportsystem.realty.domain.port.outbound.ResidentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;

    public ResidentServiceImpl(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    @Override
    public Resident createResident(UUID tenantId, UUID branchId, UUID propertyId, String name) {
        Resident resident = Resident.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .propertyId(propertyId).name(name).status("active")
                .createdAt(Instant.now()).build();
        return residentRepository.save(resident);
    }

    @Override
    public Optional<Resident> getResidentById(UUID id) {
        return residentRepository.findById(id);
    }

    @Override
    public List<Resident> getResidentsByTenant(UUID tenantId) {
        return residentRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Resident> getResidentsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return residentRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<Resident> getResidentsByProperty(UUID propertyId) {
        return residentRepository.findByPropertyId(propertyId);
    }

    @Override
    public Resident updateResident(Resident resident) {
        return residentRepository.save(resident);
    }
}
