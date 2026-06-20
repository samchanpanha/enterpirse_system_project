package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Lease;
import com.reportsystem.property.domain.port.outbound.LeaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaseService")
class LeaseServiceTest {
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final UUID UNIT_ID = UUID.randomUUID();

    @Mock private LeaseRepository leaseRepository;
    @Captor private ArgumentCaptor<Lease> leaseCaptor;

    private LeaseService leaseService;

    @BeforeEach
    void setUp() {
        leaseService = new LeaseService(leaseRepository);
    }

    @Test
    @DisplayName("createLease builds and persists a new lease")
    void createLease_buildsAndPersists() {
        when(leaseRepository.hasOverlappingLease(any(), any(), any())).thenReturn(false);
        when(leaseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lease result = leaseService.createLease(
                TENANT_ID, BRANCH_ID, UNIT_ID, "John Doe", "+85512345678",
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                new BigDecimal("1000"), new BigDecimal("500"));

        assertThat(result.getTenantName()).isEqualTo("John Doe");
        assertThat(result.getRentAmount()).isEqualByComparingTo("1000");
        assertThat(result.getStatus()).isEqualTo("active");
        assertThat(result.getPaymentDay()).isEqualTo(1);
        verify(leaseRepository).save(leaseCaptor.capture());
        assertThat(leaseCaptor.getValue().getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @DisplayName("createLease throws on overlapping lease")
    void createLease_throwsOnOverlap() {
        when(leaseRepository.hasOverlappingLease(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> leaseService.createLease(
                TENANT_ID, BRANCH_ID, UNIT_ID, "Jane Doe", null,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("800"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Overlapping lease");
        verify(leaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("getLeaseById returns lease when found")
    void getLeaseById_returnsLease() {
        UUID id = UUID.randomUUID();
        Lease lease = Lease.builder().id(id).tenantName("Test Tenant").build();
        when(leaseRepository.findById(id)).thenReturn(Optional.of(lease));

        Optional<Lease> result = leaseService.getLeaseById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getTenantName()).isEqualTo("Test Tenant");
    }

    @Test
    @DisplayName("getLeasesByUnit delegates to repository")
    void getLeasesByUnit_delegates() {
        when(leaseRepository.findByUnitId(UNIT_ID)).thenReturn(List.of());
        List<Lease> result = leaseService.getLeasesByUnit(UNIT_ID);
        assertThat(result).isEmpty();
        verify(leaseRepository).findByUnitId(UNIT_ID);
    }

    @Test
    @DisplayName("getActiveLeasesByTenant delegates to repository")
    void getActiveLeasesByTenant_delegates() {
        when(leaseRepository.findActiveByTenantId(TENANT_ID)).thenReturn(List.of());
        List<Lease> result = leaseService.getActiveLeasesByTenant(TENANT_ID);
        assertThat(result).isEmpty();
        verify(leaseRepository).findActiveByTenantId(TENANT_ID);
    }

    @Test
    @DisplayName("terminateLease sets status to terminated")
    void terminateLease_setsStatusTerminated() {
        UUID id = UUID.randomUUID();
        Lease existing = Lease.builder()
                .id(id).tenantId(TENANT_ID).unitId(UNIT_ID)
                .tenantName("John").status("active").documents("[]")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2027, 1, 1))
                .build();
        when(leaseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(leaseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lease result = leaseService.terminateLease(id);

        assertThat(result.getStatus()).isEqualTo("terminated");
    }

    @Test
    @DisplayName("terminateLease throws when lease not found")
    void terminateLease_throwsWhenNotFound() {
        when(leaseRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> leaseService.terminateLease(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lease not found");
    }

    @Test
    @DisplayName("renewLease sets new dates and activates")
    void renewLease_setsNewDatesAndActivates() {
        UUID id = UUID.randomUUID();
        Lease existing = Lease.builder()
                .id(id).tenantId(TENANT_ID).unitId(UNIT_ID)
                .tenantName("John").status("active").documents("[]")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .rentAmount(new BigDecimal("1000"))
                .build();
        when(leaseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(leaseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lease result = leaseService.renewLease(
                id, LocalDate.of(2027, 12, 31), new BigDecimal("1200"));

        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2027, 12, 31));
        assertThat(result.getRentAmount()).isEqualByComparingTo("1200");
        assertThat(result.getStatus()).isEqualTo("active");
    }
}
