package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Role;
import com.reportsystem.auth.domain.model.User;
import com.reportsystem.auth.domain.port.inbound.UserUseCase;
import com.reportsystem.auth.domain.port.outbound.RoleRepository;
import com.reportsystem.auth.domain.port.outbound.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.deactivate();
        userRepository.save(user);
    }

    @Override
    public void assignRoles(UUID userId, List<UUID> roleIds) {
    }

    @Override
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
