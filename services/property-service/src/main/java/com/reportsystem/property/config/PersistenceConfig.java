package com.reportsystem.property.config;

import com.reportsystem.property.domain.model.Property;
import com.reportsystem.property.domain.model.Unit;
import com.reportsystem.property.domain.model.Lease;
import com.reportsystem.property.domain.model.Schedule;
import com.reportsystem.property.domain.model.MaintenanceTicket;
import com.reportsystem.property.domain.port.outbound.PropertyRepository;
import com.reportsystem.property.domain.port.outbound.UnitRepository;
import com.reportsystem.property.domain.port.outbound.LeaseRepository;
import com.reportsystem.property.domain.port.outbound.ScheduleRepository;
import com.reportsystem.property.domain.port.outbound.MaintenanceRepository;
import com.reportsystem.property.infrastructure.persistence.mapper.PropertyMapper;
import com.reportsystem.property.infrastructure.persistence.mapper.UnitMapper;
import com.reportsystem.property.infrastructure.persistence.mapper.LeaseMapper;
import com.reportsystem.property.infrastructure.persistence.repository.JpaPropertyRepository;
import com.reportsystem.property.infrastructure.persistence.repository.JpaUnitRepository;
import com.reportsystem.property.infrastructure.persistence.repository.JpaLeaseRepository;
import com.reportsystem.property.infrastructure.persistence.repository.JpaScheduleRepository;
import com.reportsystem.property.infrastructure.persistence.repository.JpaMaintenanceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {

    @Bean
    public PropertyRepository propertyRepository(JpaPropertyRepository jpa, PropertyMapper mapper) {
        return new PropertyRepository() {
            @Override public Property save(Property p) { return mapper.toDomain(jpa.save(mapper.toEntity(p))); }
            @Override public java.util.Optional<Property> findById(java.util.UUID id) { return jpa.findById(id).map(mapper::toDomain); }
            @Override public java.util.List<Property> findByTenantId(java.util.UUID tenantId) { return jpa.findByTenantId(tenantId).stream().map(mapper::toDomain).toList(); }
            @Override public java.util.List<Property> findByTenantIdAndBranchId(java.util.UUID tenantId, java.util.UUID branchId) { return jpa.findByTenantIdAndBranchId(tenantId, branchId).stream().map(mapper::toDomain).toList(); }
            @Override public void deleteById(java.util.UUID id) { jpa.deleteById(id); }
        };
    }

    @Bean
    public UnitRepository unitRepository(JpaUnitRepository jpa, UnitMapper mapper) {
        return new UnitRepository() {
            @Override public Unit save(Unit u) { return mapper.toDomain(jpa.save(mapper.toEntity(u))); }
            @Override public java.util.Optional<Unit> findById(java.util.UUID id) { return jpa.findById(id).map(mapper::toDomain); }
            @Override public java.util.List<Unit> findByPropertyId(java.util.UUID propertyId) { return jpa.findByPropertyId(propertyId).stream().map(mapper::toDomain).toList(); }
            @Override public java.util.List<Unit> findByTenantId(java.util.UUID tenantId) { return jpa.findByTenantId(tenantId).stream().map(mapper::toDomain).toList(); }
            @Override public java.util.List<Unit> findByTenantIdAndBranchId(java.util.UUID tenantId, java.util.UUID branchId) { return jpa.findByTenantIdAndBranchId(tenantId, branchId).stream().map(mapper::toDomain).toList(); }
            @Override public void deleteById(java.util.UUID id) { jpa.deleteById(id); }
        };
    }

    @Bean
    public LeaseRepository leaseRepository(JpaLeaseRepository jpa, LeaseMapper mapper) {
        return new LeaseRepository() {
            @Override public Lease save(Lease l) { return mapper.toDomain(jpa.save(mapper.toEntity(l))); }
            @Override public java.util.Optional<Lease> findById(java.util.UUID id) { return jpa.findById(id).map(mapper::toDomain); }
            @Override public java.util.List<Lease> findByUnitId(java.util.UUID unitId) { return jpa.findByUnitId(unitId).stream().map(mapper::toDomain).toList(); }
            @Override public java.util.List<Lease> findByTenantId(java.util.UUID tenantId) { return jpa.findByTenantId(tenantId).stream().map(mapper::toDomain).toList(); }
            @Override public java.util.List<Lease> findByTenantIdAndBranchId(java.util.UUID tenantId, java.util.UUID branchId) { return jpa.findByTenantIdAndBranchId(tenantId, branchId).stream().map(mapper::toDomain).toList(); }
            @Override public java.util.List<Lease> findActiveByTenantId(java.util.UUID tenantId) { return jpa.findActiveByTenantId(tenantId).stream().map(mapper::toDomain).toList(); }
            @Override public boolean hasOverlappingLease(java.util.UUID unitId, java.time.LocalDate startDate, java.time.LocalDate endDate) { return jpa.hasOverlappingLease(unitId, startDate, endDate); }
        };
    }

    @Bean
    public ScheduleRepository scheduleRepository(JpaScheduleRepository jpa) {
        return new ScheduleRepository() {
            @Override public Schedule save(Schedule s) { return toDomain(jpa.save(toEntity(s))); }
            @Override public java.util.Optional<Schedule> findById(java.util.UUID id) { return jpa.findById(id).map(this::toDomain); }
            @Override public java.util.List<Schedule> findByUnitIdAndTimeRange(java.util.UUID unitId, java.time.Instant from, java.time.Instant to) {
                return jpa.findByUnitIdAndTimeRange(unitId, from, to).stream().map(this::toDomain).toList();
            }
            @Override public java.util.List<Schedule> findByTenantIdAndBranchId(java.util.UUID tenantId, java.util.UUID branchId) {
                return jpa.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
            }
            @Override public java.util.List<Schedule> findByUnitIds(java.util.Collection<java.util.UUID> unitIds) {
                return jpa.findByUnitIdIn(unitIds).stream().map(this::toDomain).toList();
            }
            @Override public void deleteById(java.util.UUID id) { jpa.deleteById(id); }
            @Override public boolean hasOverlap(java.util.UUID unitId, java.time.Instant startTime, java.time.Instant endTime) {
                return jpa.hasOverlap(unitId, startTime, endTime);
            }
            private com.reportsystem.property.infrastructure.persistence.entity.ScheduleEntity toEntity(Schedule s) {
                return com.reportsystem.property.infrastructure.persistence.entity.ScheduleEntity.builder()
                        .id(s.getId()).tenantId(s.getTenantId()).branchId(s.getBranchId()).unitId(s.getUnitId()).title(s.getTitle())
                        .description(s.getDescription()).type(s.getType()).intervalType(s.getIntervalType())
                        .startTime(s.getStartTime()).endTime(s.getEndTime())
                        .recurringRule(s.getRecurringRule()).status(s.getStatus()).createdBy(s.getCreatedBy())
                        .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
            }
            private Schedule toDomain(com.reportsystem.property.infrastructure.persistence.entity.ScheduleEntity e) {
                return Schedule.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).unitId(e.getUnitId())
                        .title(e.getTitle()).description(e.getDescription()).type(e.getType())
                        .intervalType(e.getIntervalType()).startTime(e.getStartTime()).endTime(e.getEndTime())
                        .recurringRule(e.getRecurringRule()).status(e.getStatus()).createdBy(e.getCreatedBy())
                        .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
            }
        };
    }

    @Bean
    public MaintenanceRepository maintenanceRepository(JpaMaintenanceRepository jpa) {
        return new MaintenanceRepository() {
            @Override public MaintenanceTicket save(MaintenanceTicket t) { return toDomain(jpa.save(toEntity(t))); }
            @Override public java.util.Optional<MaintenanceTicket> findById(java.util.UUID id) { return jpa.findById(id).map(this::toDomain); }
            @Override public java.util.List<MaintenanceTicket> findByUnitId(java.util.UUID unitId) { return jpa.findByUnitId(unitId).stream().map(this::toDomain).toList(); }
            @Override public java.util.List<MaintenanceTicket> findByTenantId(java.util.UUID tenantId) { return jpa.findByTenantId(tenantId).stream().map(this::toDomain).toList(); }
            @Override public java.util.List<MaintenanceTicket> findByTenantIdAndBranchId(java.util.UUID tenantId, java.util.UUID branchId) {
                return jpa.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
            }
            @Override public java.util.List<MaintenanceTicket> findByUnitIds(java.util.Collection<java.util.UUID> unitIds) {
                return jpa.findByUnitIdIn(unitIds).stream().map(this::toDomain).toList();
            }
            private com.reportsystem.property.infrastructure.persistence.entity.MaintenanceTicketEntity toEntity(MaintenanceTicket t) {
                return com.reportsystem.property.infrastructure.persistence.entity.MaintenanceTicketEntity.builder()
                        .id(t.getId()).tenantId(t.getTenantId()).branchId(t.getBranchId()).unitId(t.getUnitId())
                        .reportedBy(t.getReportedBy()).title(t.getTitle()).description(t.getDescription())
                        .priority(t.getPriority()).category(t.getCategory()).status(t.getStatus())
                        .assignedTo(t.getAssignedTo()).costEstimate(t.getCostEstimate()).actualCost(t.getActualCost())
                        .completedAt(t.getCompletedAt()).createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();
            }
            private MaintenanceTicket toDomain(com.reportsystem.property.infrastructure.persistence.entity.MaintenanceTicketEntity e) {
                return MaintenanceTicket.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).unitId(e.getUnitId())
                        .reportedBy(e.getReportedBy()).title(e.getTitle()).description(e.getDescription())
                        .priority(e.getPriority()).category(e.getCategory()).status(e.getStatus())
                        .assignedTo(e.getAssignedTo()).costEstimate(e.getCostEstimate()).actualCost(e.getActualCost())
                        .completedAt(e.getCompletedAt()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
            }
        };
    }
}
