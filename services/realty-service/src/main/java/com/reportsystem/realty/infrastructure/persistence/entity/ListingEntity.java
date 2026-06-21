package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "listings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ListingEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "agent_id")
    private UUID agentId;
    @Column(nullable = false)
    private String title;
    @Column(name = "title_kh")
    private String titleKh;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;
    @Column(name = "property_type")
    private String propertyType;
    @Column(name = "listing_type")
    private String listingType;
    private String status;
    @Column(precision = 15, scale = 2)
    private BigDecimal price;
    private String currency;
    @Column(name = "area_sqm", precision = 12, scale = 2)
    private BigDecimal areaSqm;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer floors;
    @Column(name = "year_built")
    private Integer yearBuilt;
    @Column(columnDefinition = "TEXT")
    private String address;
    private String city;
    private String district;
    private String province;
    @Column(precision = 10, scale = 7)
    private BigDecimal lat;
    @Column(precision = 10, scale = 7)
    private BigDecimal lng;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb default '[]'")
    private String features;
    @Column(name = "is_featured")
    private boolean featured;
    @Column(name = "is_published")
    private boolean published;
    @Column(name = "view_count")
    private int viewCount;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
