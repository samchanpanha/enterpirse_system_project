package com.reportsystem.auth.domain.port.inbound;

import com.reportsystem.auth.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface AuthUseCase {
    User register(UUID tenantId, String email, String password, String firstName, String lastName);
    Optional<User> login(String email, String password);
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String refreshAccessToken(String refreshToken);
}
