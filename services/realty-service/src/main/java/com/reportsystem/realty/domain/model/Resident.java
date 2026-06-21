package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Resident {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID propertyId;
    private String name;
    private String nameKh;
    private String phone;
    private String email;
    private String idNumber;
    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private String status;
    private String emergencyContact;
    private String emergencyPhone;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}
