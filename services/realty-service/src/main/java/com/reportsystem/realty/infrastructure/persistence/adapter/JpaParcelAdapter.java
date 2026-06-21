package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Parcel;
import com.reportsystem.realty.domain.port.outbound.ParcelRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.ParcelEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaParcelRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaParcelAdapter implements ParcelRepository {

    private final JpaParcelRepository repo;
    public JpaParcelAdapter(JpaParcelRepository repo) { this.repo = repo; }

    @Override
    public Parcel save(Parcel parcel) {
        ParcelEntity e = new ParcelEntity();
        e.setId(parcel.getId());
        e.setTenantId(parcel.getTenantId());
        e.setBranchId(parcel.getBranchId());
        e.setResidentId(parcel.getResidentId());
        e.setCarrier(parcel.getCarrier());
        e.setTrackingNumber(parcel.getTrackingNumber());
        e.setDescription(parcel.getDescription());
        e.setStatus(parcel.getStatus() != null ? parcel.getStatus() : "received");
        e.setReceivedAt(parcel.getReceivedAt());
        e.setPickedUpAt(parcel.getPickedUpAt());
        e.setNotes(parcel.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Parcel> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Parcel> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Parcel> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Parcel toDomain(ParcelEntity e) {
        return Parcel.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .residentId(e.getResidentId()).carrier(e.getCarrier())
                .trackingNumber(e.getTrackingNumber()).description(e.getDescription())
                .status(e.getStatus()).receivedAt(e.getReceivedAt())
                .pickedUpAt(e.getPickedUpAt()).notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
