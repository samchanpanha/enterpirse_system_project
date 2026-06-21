package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Visitor;
import com.reportsystem.realty.domain.port.outbound.VisitorRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.VisitorEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaVisitorRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaVisitorAdapter implements VisitorRepository {

    private final JpaVisitorRepository repo;
    public JpaVisitorAdapter(JpaVisitorRepository repo) { this.repo = repo; }

    @Override
    public Visitor save(Visitor visitor) {
        VisitorEntity e = new VisitorEntity();
        e.setId(visitor.getId());
        e.setTenantId(visitor.getTenantId());
        e.setBranchId(visitor.getBranchId());
        e.setPropertyId(visitor.getPropertyId());
        e.setName(visitor.getName());
        e.setPhone(visitor.getPhone());
        e.setIdNumber(visitor.getIdNumber());
        e.setVehiclePlate(visitor.getVehiclePlate());
        e.setPurpose(visitor.getPurpose());
        e.setCheckIn(visitor.getCheckIn());
        e.setCheckOut(visitor.getCheckOut());
        e.setQrCode(visitor.getQrCode());
        e.setStatus(visitor.getStatus() != null ? visitor.getStatus() : "checked_in");
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Visitor> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Visitor> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Visitor> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Visitor> findByPropertyId(UUID propertyId) {
        return repo.findByPropertyId(propertyId).stream().map(this::toDomain).toList();
    }

    private Visitor toDomain(VisitorEntity e) {
        return Visitor.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .propertyId(e.getPropertyId()).name(e.getName()).phone(e.getPhone())
                .idNumber(e.getIdNumber()).vehiclePlate(e.getVehiclePlate())
                .purpose(e.getPurpose()).checkIn(e.getCheckIn()).checkOut(e.getCheckOut())
                .qrCode(e.getQrCode()).status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
