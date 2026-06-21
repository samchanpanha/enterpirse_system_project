package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Parcel;
import com.reportsystem.realty.domain.port.inbound.ParcelService;
import com.reportsystem.realty.domain.port.outbound.ParcelRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;

    public ParcelServiceImpl(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Override
    public Parcel createParcel(UUID tenantId, UUID branchId, UUID residentId, String carrier) {
        Parcel parcel = Parcel.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .residentId(residentId).carrier(carrier).status("received")
                .createdAt(Instant.now()).build();
        return parcelRepository.save(parcel);
    }

    @Override
    public Optional<Parcel> getParcelById(UUID id) {
        return parcelRepository.findById(id);
    }

    @Override
    public List<Parcel> getParcelsByTenant(UUID tenantId) {
        return parcelRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Parcel> getParcelsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return parcelRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Parcel updateParcel(Parcel parcel) {
        return parcelRepository.save(parcel);
    }
}
