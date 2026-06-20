package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.domain.model.Role;
import com.reportsystem.auth.domain.model.User;
import com.reportsystem.auth.domain.port.inbound.UserUseCase;
import com.reportsystem.auth.domain.port.outbound.RoleRepository;
import com.reportsystem.auth.domain.port.outbound.UserRepository;
import com.reportsystem.auth.infrastructure.persistence.entity.RolePermissionEntity;
import com.reportsystem.auth.infrastructure.persistence.entity.UserRoleEntity;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaRolePermissionRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaUserRoleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JpaUserRoleRepository userRoleRepository;
    private final JpaRolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       JpaUserRoleRepository userRoleRepository,
                       JpaRolePermissionRepository rolePermissionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(UUID tenantId, String email, String password, String firstName, String lastName, String phone) {
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new IllegalArgumentException("Email already exists for this tenant: " + email);
        }
        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .locale("km")
                .active(true)
                .createdAt(Instant.now())
                .build();
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(UUID tenantId, String email) {
        return userRepository.findByEmailAndTenantId(email.toLowerCase().trim(), tenantId);
    }

    @Override
    public List<User> getUsersByTenant(UUID tenantId) {
        return userRepository.findByTenantId(tenantId);
    }

    @Override
    public User updateUser(UUID id, String firstName, String lastName, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.updateProfile(firstName, lastName, phone);
        return userRepository.save(user);
    }

    @Override
    public User updateUserEmail(UUID id, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.updateEmail(email.toLowerCase().trim());
        return userRepository.save(user);
    }

    public User updateUserPassword(UUID id, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.updatePassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.deactivate();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(UUID userId, List<UUID> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        userRoleRepository.deleteByIdUserId(userId);
        for (UUID roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
            if (!role.getTenantId().equals(user.getTenantId())) {
                throw new IllegalArgumentException("Role belongs to a different tenant");
            }
            userRoleRepository.save(new UserRoleEntity(userId, roleId));
        }
    }

    public List<Role> getUserRoles(UUID userId) {
        List<UUID> roleIds = userRoleRepository.findByIdUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        return roleIds.stream()
                .map(roleRepository::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    public List<UUID> getUserPermissionIds(UUID userId) {
        List<UUID> roleIds = userRoleRepository.findByIdUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        return roleIds.stream()
                .flatMap(roleId -> rolePermissionRepository.findByIdRoleId(roleId).stream())
                .map(RolePermissionEntity::getPermissionId)
                .distinct()
                .toList();
    }

    @Override
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
