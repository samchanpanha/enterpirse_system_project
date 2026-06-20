package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Unit;
import com.reportsystem.property.domain.port.outbound.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnitService")
class UnitServiceTest {
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final UUID PROPERTY_ID = UUID.randomUUID();

    @Mock private UnitRepository unitRepository;
    @Captor private ArgumentCaptor<Unit> unitCaptor;

    private UnitService unitService;

    @BeforeEach
    void setUp() {
        unitService = new UnitService(unitRepository);
    }

    @Test
    @DisplayName("createUnit builds and persists a new unit with defaults")
    void createUnit_buildsAndPersists() {
        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Unit result = unitService.createUnit(
                TENANT_ID, BRANCH_ID, PROPERTY_ID, "A-101",
                1, 2, 1, new BigDecimal("500.00"));

        assertThat(result.getLabel()).isEqualTo("A-101");
        assertThat(result.getRentAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo("vacant");
        assertThat(result.getAmenities()).isEqualTo("[]");
        verify(unitRepository).save(unitCaptor.capture());
        assertThat(unitCaptor.getValue().getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @DisplayName("createUnit defaults rentAmount to zero when null")
    void createUnit_defaultsRentToZero() {
        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Unit result = unitService.createUnit(
                TENANT_ID, BRANCH_ID, PROPERTY_ID, "B-202",
                2, 3, 2, null);

        assertThat(result.getRentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createUnit marks unit as vacant by default")
    void createUnit_marksVacant() {
        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Unit result = unitService.createUnit(
                TENANT_ID, BRANCH_ID, PROPERTY_ID, "C-303",
                3, 1, 1, BigDecimal.TEN);

        assertThat(result.getStatus()).isEqualTo("vacant");
    }

    @Test
    @DisplayName("getUnitById returns unit when found")
    void getUnitById_returnsUnit() {
        UUID id = UUID.randomUUID();
        Unit unit = Unit.builder().id(id).label("A-1").build();
        when(unitRepository.findById(id)).thenReturn(Optional.of(unit));

        Optional<Unit> result = unitService.getUnitById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getLabel()).isEqualTo("A-1");
    }

    @Test
    @DisplayName("getUnitsByProperty delegates to repository")
    void getUnitsByProperty_delegates() {
        when(unitRepository.findByPropertyId(PROPERTY_ID)).thenReturn(List.of());
        List<Unit> result = unitService.getUnitsByProperty(PROPERTY_ID);
        assertThat(result).isEmpty();
        verify(unitRepository).findByPropertyId(PROPERTY_ID);
    }

    @Test
    @DisplayName("updateUnit throws when unit not found")
    void updateUnit_throwsWhenNotFound() {
        when(unitRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> unitService.updateUnit(
                UUID.randomUUID(), null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unit not found");
    }

    @Test
    @DisplayName("updateUnit preserves existing fields for null parameters")
    void updateUnit_preservesExistingFields() {
        UUID id = UUID.randomUUID();
        Unit existing = Unit.builder()
                .id(id).tenantId(TENANT_ID).branchId(BRANCH_ID).propertyId(PROPERTY_ID)
                .label("Old Label").floor(1).bedrooms(2).bathrooms(1)
                .rentAmount(new BigDecimal("500")).status("vacant")
                .amenities("[]").images("[]")
                .build();
        when(unitRepository.findById(id)).thenReturn(Optional.of(existing));
        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        unitService.updateUnit(id, "New Label", null, "occupied", null);

        verify(unitRepository).save(unitCaptor.capture());
        Unit saved = unitCaptor.getValue();
        assertThat(saved.getLabel()).isEqualTo("New Label");
        assertThat(saved.getStatus()).isEqualTo("occupied");
        assertThat(saved.getFloor()).isEqualTo(1);
        assertThat(saved.getRentAmount()).isEqualByComparingTo("500");
    }
}
