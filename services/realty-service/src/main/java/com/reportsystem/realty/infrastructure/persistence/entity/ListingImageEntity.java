package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "listing_images")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ListingImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;
    @Column(name = "is_primary")
    private boolean primary;
    @Column(name = "sort_order")
    private int sortOrder;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
