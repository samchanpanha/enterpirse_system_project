package com.reportsystem.auth.domain.port.inbound;

import com.reportsystem.auth.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserUseCase {
    User createUser(UUID tenantId, String email, String password, String firstName, String lastName, String phone);
    Optional<User> getUserById(UUID id);
    Optional<User> getUserByEmail(UUID tenantId, String email);
    List<User> getUsersByTenant(UUID tenantId);
    User updateUser(UUID id, String firstName, String lastName, String phone);
    User updateUserEmail(UUID id, String email);
    void deactivateUser(UUID id);
    void assignRoles(UUID userId, List<UUID> roleIds);
    boolean validatePassword(User user, String rawPassword);
}
