package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Property;
import com.reportsystem.property.domain.port.outbound.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyService")
class PropertyServiceTest {
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID BRANCH_ID = UUID.randomUUID();

    @Mock private PropertyRepository propertyRepository;
    @Captor private ArgumentCaptor<Property> propertyCaptor;

    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService(propertyRepository);
    }

    @Test
    @DisplayName("createProperty builds and persists a new property")
    void createProperty_buildsAndPersists() {
        Property saved = Property.builder()
                .id(UUID.randomUUID()).tenantId(TENANT_ID).branchId(BRANCH_ID)
                .name("Sunset Tower").type("apartment")
                .address("123 Main St").city("Phnom Penh").district("Daun Penh")
                .totalUnits(0).status("active").build();
        when(propertyRepository.save(any())).thenReturn(saved);

        Property result = propertyService.createProperty(
                TENANT_ID, BRANCH_ID, "Sunset Tower", "apartment",
                "123 Main St", "Phnom Penh", "Daun Penh");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sunset Tower");
        assertThat(result.getTotalUnits()).isZero();
        assertThat(result.getStatus()).isEqualTo("active");
        verify(propertyRepository).save(propertyCaptor.capture());
        assertThat(propertyCaptor.getValue().getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @DisplayName("getPropertyById returns property when found")
    void getPropertyById_returnsProperty() {
        UUID id = UUID.randomUUID();
        Property prop = Property.builder().id(id).tenantId(TENANT_ID).name("Test").build();
        when(propertyRepository.findById(id)).thenReturn(Optional.of(prop));

        Optional<Property> result = propertyService.getPropertyById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("getPropertyById returns empty when not found")
    void getPropertyById_returnsEmptyWhenNotFound() {
        when(propertyRepository.findById(any())).thenReturn(Optional.empty());

        Optional<Property> result = propertyService.getPropertyById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPropertiesByTenant delegates to repository")
    void getPropertiesByTenant_delegates() {
        when(propertyRepository.findByTenantId(TENANT_ID)).thenReturn(List.of());
        List<Property> result = propertyService.getPropertiesByTenant(TENANT_ID);
        assertThat(result).isEmpty();
        verify(propertyRepository).findByTenantId(TENANT_ID);
    }

    @Test
    @DisplayName("updateProperty throws when property not found")
    void updateProperty_throwsWhenNotFound() {
        when(propertyRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> propertyService.updateProperty(
                UUID.randomUUID(), null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property not found");
    }

    @Test
    @DisplayName("updateProperty updates only non-null fields")
    void updateProperty_updatesNonNullFields() {
        UUID id = UUID.randomUUID();
        Property existing = Property.builder()
                .id(id).tenantId(TENANT_ID).branchId(BRANCH_ID)
                .name("Old Name").type("apartment").address("Old Address")
                .city("City").district("District").totalUnits(5).status("active")
                .build();
        when(propertyRepository.findById(id)).thenReturn(Optional.of(existing));
        when(propertyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Property result = propertyService.updateProperty(
                id, "New Name", null, "New Address", null, null, "inactive");

        verify(propertyRepository).save(propertyCaptor.capture());
        Property saved = propertyCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("New Name");
        assertThat(saved.getAddress()).isEqualTo("New Address");
        assertThat(saved.getStatus()).isEqualTo("inactive");
        assertThat(saved.getType()).isEqualTo("apartment");
        assertThat(saved.getCity()).isEqualTo("City");
    }

    @Test
    @DisplayName("deleteProperty delegates to repository")
    void deleteProperty_delegates() {
        UUID id = UUID.randomUUID();
        propertyService.deleteProperty(id);
        verify(propertyRepository).deleteById(id);
    }
}
