package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Resident;
import com.reportsystem.realty.domain.port.outbound.ResidentRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.ResidentEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaResidentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaResidentAdapter implements ResidentRepository {

    private final JpaResidentRepository repo;
    public JpaResidentAdapter(JpaResidentRepository repo) { this.repo = repo; }

    @Override
    public Resident save(Resident resident) {
        ResidentEntity e = new ResidentEntity();
        e.setId(resident.getId());
        e.setTenantId(resident.getTenantId());
        e.setBranchId(resident.getBranchId());
        e.setPropertyId(resident.getPropertyId());
        e.setName(resident.getName());
        e.setNameKh(resident.getNameKh());
        e.setPhone(resident.getPhone());
        e.setEmail(resident.getEmail());
        e.setIdNumber(resident.getIdNumber());
        e.setMoveInDate(resident.getMoveInDate());
        e.setMoveOutDate(resident.getMoveOutDate());
        e.setStatus(resident.getStatus() != null ? resident.getStatus() : "active");
        e.setEmergencyContact(resident.getEmergencyContact());
        e.setEmergencyPhone(resident.getEmergencyPhone());
        e.setNotes(resident.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Resident> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Resident> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Resident> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Resident> findByPropertyId(UUID propertyId) {
        return repo.findByPropertyId(propertyId).stream().map(this::toDomain).toList();
    }

    private Resident toDomain(ResidentEntity e) {
        return Resident.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .propertyId(e.getPropertyId()).name(e.getName()).nameKh(e.getNameKh())
                .phone(e.getPhone()).email(e.getEmail()).idNumber(e.getIdNumber())
                .moveInDate(e.getMoveInDate()).moveOutDate(e.getMoveOutDate())
                .status(e.getStatus()).emergencyContact(e.getEmergencyContact())
                .emergencyPhone(e.getEmergencyPhone()).notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
