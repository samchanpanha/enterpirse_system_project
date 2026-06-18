package com.reportsystem.restaurant.domain.service;

import com.reportsystem.restaurant.domain.model.Outlet;
import com.reportsystem.restaurant.domain.port.inbound.OutletService;
import com.reportsystem.restaurant.domain.port.outbound.OutletRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OutletServiceImpl implements OutletService {

    private final OutletRepository outletRepository;

    public OutletServiceImpl(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @Override
    public Outlet createOutlet(UUID tenantId, UUID branchId, String name) {
        Outlet outlet = Outlet.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name(name)
                .currency("KHR").active(true)
                .createdAt(Instant.now()).build();
        return outletRepository.save(outlet);
    }

    @Override
    public Optional<Outlet> getOutletById(UUID id) {
        return outletRepository.findById(id);
    }

    @Override
    public List<Outlet> getOutletsByTenant(UUID tenantId) {
        return outletRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Outlet> getOutletsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return outletRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }
}
