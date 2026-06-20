package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.User;
import com.reportsystem.auth.domain.port.inbound.AuthUseCase;
import com.reportsystem.auth.domain.port.outbound.UserRepository;
import com.reportsystem.shared.security.JwtTokenProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public User register(UUID tenantId, String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .active(true)
                .locale("km")
                .createdAt(java.time.Instant.now())
                .build();
        return userRepository.save(user);
    }

    @Override
    public Optional<User> login(String email, String password) {
        // Global login: look up by email across all tenants, then verify password.
        // Spring Data JPA's findByEmailAndTenantId with null tenantId never matches,
        // so iterate via findByEmail (added to repository) for cross-tenant lookup.
        return userRepository.findByEmail(email.toLowerCase().trim())
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }

    @Override
    public String generateAccessToken(User user) {
        List<String> roles = userService.getUserRoles(user.getId()).stream()
                .map(role -> role.getName())
                .toList();
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getTenantId(), user.getEmail(), roles);
    }

    @Override
    public String generateRefreshToken(User user) {
        return jwtTokenProvider.generateRefreshToken(user.getId());
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        try {
            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            List<String> roles = userService.getUserRoles(userId).stream()
                    .map(role -> role.getName())
                    .toList();
            return jwtTokenProvider.generateAccessToken(user.getId(), user.getTenantId(), user.getEmail(), roles);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
    }
}
