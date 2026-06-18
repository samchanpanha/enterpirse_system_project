package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.RestaurantTable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableRepository {
    RestaurantTable save(RestaurantTable table);
    Optional<RestaurantTable> findById(UUID id);
    List<RestaurantTable> findByOutletId(UUID outletId);
    List<RestaurantTable> findByStatus(String status);
}
