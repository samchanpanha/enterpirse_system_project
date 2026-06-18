package com.reportsystem.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class Supplier {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxNumber;
    private String paymentTerms;
    private String currency;
    private boolean active;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}
