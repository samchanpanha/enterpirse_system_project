package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Visitor {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID propertyId;
    private String name;
    private String phone;
    private String idNumber;
    private String vehiclePlate;
    private String purpose;
    private Instant checkIn;
    private Instant checkOut;
    private String qrCode;
    private String status;
    private final Instant createdAt;
}
