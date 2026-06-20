package com.reportsystem.auth.config;

import com.reportsystem.auth.domain.port.outbound.TenantRepository;
import com.reportsystem.auth.domain.port.outbound.UserRepository;
import com.reportsystem.auth.domain.service.AuthService;
import com.reportsystem.auth.domain.service.JwtService;
import com.reportsystem.auth.domain.service.TenantService;
import com.reportsystem.auth.domain.service.UserService;
import com.reportsystem.auth.domain.port.outbound.RoleRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaUserRoleRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaRolePermissionRepository;
import com.reportsystem.shared.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, accessTokenExpiration, refreshTokenExpiration);
    }

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret, accessTokenExpiration, refreshTokenExpiration);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TenantService tenantService(TenantRepository tenantRepository) {
        return new TenantService(tenantRepository);
    }

    @Bean
    public UserService userService(UserRepository userRepository, RoleRepository roleRepository,
                                   JpaUserRoleRepository jpaUserRoleRepository,
                                   JpaRolePermissionRepository jpaRolePermissionRepository,
                                   PasswordEncoder passwordEncoder) {
        return new UserService(userRepository, roleRepository, jpaUserRoleRepository, jpaRolePermissionRepository, passwordEncoder);
    }

    @Bean
    public AuthService authService(UserRepository userRepository, UserService userService,
                                   PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        return new AuthService(userRepository, userService, passwordEncoder, jwtTokenProvider);
    }
}
