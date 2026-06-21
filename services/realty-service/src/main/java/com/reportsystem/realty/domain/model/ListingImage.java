package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ListingImage {
    private final UUID id;
    private final UUID listingId;
    private String imageUrl;
    private boolean primary;
    private int sortOrder;
    private final Instant createdAt;
}
