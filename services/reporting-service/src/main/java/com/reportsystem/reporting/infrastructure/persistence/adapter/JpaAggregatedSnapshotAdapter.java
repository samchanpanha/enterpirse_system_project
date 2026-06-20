package com.reportsystem.reporting.infrastructure.persistence.adapter;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.outbound.*;
import com.reportsystem.reporting.infrastructure.persistence.entity.*;
import com.reportsystem.reporting.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaAggregatedSnapshotAdapter implements AggregatedSnapshotRepository {
    private final JpaAggregatedSnapshotRepository repo;
    public JpaAggregatedSnapshotAdapter(JpaAggregatedSnapshotRepository r) { repo = r; }
    @Override public AggregatedSnapshot save(AggregatedSnapshot s) {
        AggregatedSnapshotEntity e = new AggregatedSnapshotEntity(); e.setId(s.getId()); e.setTenantId(s.getTenantId());
        e.setSnapshotType(s.getSnapshotType()); e.setSnapshotDate(s.getSnapshotDate()); e.setData(s.getData());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<AggregatedSnapshot> findByTenantIdAndSnapshotTypeAndSnapshotDate(UUID t, String type, LocalDate d) { return repo.findByTenantIdAndSnapshotTypeAndSnapshotDate(t, type, d).map(this::toDomain); }
    @Override public List<AggregatedSnapshot> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<AggregatedSnapshot> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    private AggregatedSnapshot toDomain(AggregatedSnapshotEntity e) { return AggregatedSnapshot.builder().id(e.getId()).tenantId(e.getTenantId()).snapshotType(e.getSnapshotType()).snapshotDate(e.getSnapshotDate()).data(e.getData()).createdAt(e.getCreatedAt()).build(); }
}
