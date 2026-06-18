package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String nameKh;
    private String branchType;
    private UUID parentId;
    private String address;
    private String city;
    private String district;
    private String province;
    private String phone;
    private String email;
    private String timezone;
    private String locale;
    private String currency;
    private java.math.BigDecimal taxRate;
    private String logoUrl;
    private String settings;
    private boolean active;
    private boolean isDefault;
    private LocalDate openedAt;
    private LocalDate closedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
