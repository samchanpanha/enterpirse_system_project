package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);
    Optional<User> findByEmail(String email);
    List<User> findByTenantId(UUID tenantId);
    boolean existsByEmailAndTenantId(String email, UUID tenantId);
}
