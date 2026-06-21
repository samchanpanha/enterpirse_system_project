package com.reportsystem.realty.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Listing {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID agentId;
    private String title;
    private String titleKh;
    private String description;
    private String descriptionKh;
    private String propertyType;
    private String listingType;
    private String status;
    private BigDecimal price;
    private String currency;
    private BigDecimal areaSqm;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer floors;
    private Integer yearBuilt;
    private String address;
    private String city;
    private String district;
    private String province;
    private BigDecimal lat;
    private BigDecimal lng;
    private String features;
    private boolean featured;
    private boolean published;
    private int viewCount;
    private final Instant createdAt;
    private Instant updatedAt;
}
