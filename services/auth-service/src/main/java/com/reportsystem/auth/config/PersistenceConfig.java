package com.reportsystem.auth.config;

import com.reportsystem.auth.infrastructure.persistence.mapper.TenantMapper;
import com.reportsystem.auth.infrastructure.persistence.mapper.UserMapper;
import com.reportsystem.auth.infrastructure.persistence.mapper.RoleMapper;
import com.reportsystem.auth.infrastructure.persistence.mapper.BranchMapper;
import com.reportsystem.auth.infrastructure.persistence.mapper.UserBranchMapper;
import com.reportsystem.auth.infrastructure.persistence.mapper.FeatureMapper;
import com.reportsystem.auth.infrastructure.persistence.mapper.ClientFeatureMapper;
import com.reportsystem.auth.domain.port.outbound.TenantRepository;
import com.reportsystem.auth.domain.port.outbound.UserRepository;
import com.reportsystem.auth.domain.port.outbound.RoleRepository;
import com.reportsystem.auth.domain.port.outbound.BranchRepository;
import com.reportsystem.auth.domain.port.outbound.UserBranchRepository;
import com.reportsystem.auth.domain.port.outbound.FeatureRepository;
import com.reportsystem.auth.domain.port.outbound.ClientFeatureRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaTenantRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaRoleRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaBranchRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaUserBranchRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaFeatureRepository;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaClientFeatureRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {

    @Bean
    public TenantRepository tenantRepository(JpaTenantRepository jpaTenantRepository, TenantMapper tenantMapper) {
        return new TenantRepository() {
            @Override
            public com.reportsystem.auth.domain.model.Tenant save(com.reportsystem.auth.domain.model.Tenant tenant) {
                return tenantMapper.toDomain(jpaTenantRepository.save(tenantMapper.toEntity(tenant)));
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Tenant> findById(java.util.UUID id) {
                return jpaTenantRepository.findById(id).map(tenantMapper::toDomain);
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Tenant> findBySlug(String slug) {
                return jpaTenantRepository.findBySlug(slug).map(tenantMapper::toDomain);
            }

            @Override
            public boolean existsBySlug(String slug) {
                return jpaTenantRepository.existsBySlug(slug);
            }
        };
    }

    @Bean
    public UserRepository userRepository(JpaUserRepository jpaUserRepository, UserMapper userMapper) {
        return new UserRepository() {
            @Override
            public com.reportsystem.auth.domain.model.User save(com.reportsystem.auth.domain.model.User user) {
                return userMapper.toDomain(jpaUserRepository.save(userMapper.toEntity(user)));
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.User> findById(java.util.UUID id) {
                return jpaUserRepository.findById(id).map(userMapper::toDomain);
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.User> findByEmailAndTenantId(String email, java.util.UUID tenantId) {
                return jpaUserRepository.findByEmailAndTenantId(email, tenantId).map(userMapper::toDomain);
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.User> findByEmail(String email) {
                return jpaUserRepository.findByEmail(email).map(userMapper::toDomain);
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.User> findByTenantId(java.util.UUID tenantId) {
                return jpaUserRepository.findByTenantId(tenantId).stream().map(userMapper::toDomain).toList();
            }

            @Override
            public boolean existsByEmailAndTenantId(String email, java.util.UUID tenantId) {
                return jpaUserRepository.existsByEmailAndTenantId(email, tenantId);
            }
        };
    }

    @Bean
    public RoleRepository roleRepository(JpaRoleRepository jpaRoleRepository, RoleMapper roleMapper) {
        return new RoleRepository() {
            @Override
            public com.reportsystem.auth.domain.model.Role save(com.reportsystem.auth.domain.model.Role role) {
                return roleMapper.toDomain(jpaRoleRepository.save(roleMapper.toEntity(role)));
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Role> findById(java.util.UUID id) {
                return jpaRoleRepository.findById(id).map(roleMapper::toDomain);
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.Role> findByTenantId(java.util.UUID tenantId) {
                return jpaRoleRepository.findByTenantId(tenantId).stream().map(roleMapper::toDomain).toList();
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Role> findByNameAndTenantId(String name, java.util.UUID tenantId) {
                return jpaRoleRepository.findByNameAndTenantId(name, tenantId).map(roleMapper::toDomain);
            }
        };
    }

    @Bean
    public BranchRepository branchRepository(JpaBranchRepository jpaBranchRepository, BranchMapper branchMapper) {
        return new BranchRepository() {
            @Override
            public com.reportsystem.auth.domain.model.Branch save(com.reportsystem.auth.domain.model.Branch branch) {
                return branchMapper.toDomain(jpaBranchRepository.save(branchMapper.toEntity(branch)));
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Branch> findById(java.util.UUID id) {
                return jpaBranchRepository.findById(id).map(branchMapper::toDomain);
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.Branch> findByTenantId(java.util.UUID tenantId) {
                return jpaBranchRepository.findByTenantId(tenantId).stream().map(branchMapper::toDomain).toList();
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Branch> findByTenantIdAndCode(java.util.UUID tenantId, String code) {
                return jpaBranchRepository.findByTenantIdAndCode(tenantId, code).map(branchMapper::toDomain);
            }

            @Override
            public boolean existsById(java.util.UUID id) {
                return jpaBranchRepository.existsById(id);
            }

            @Override
            public void deleteById(java.util.UUID id) {
                jpaBranchRepository.deleteById(id);
            }
        };
    }

    @Bean
    public UserBranchRepository userBranchRepository(JpaUserBranchRepository jpaRepo, UserBranchMapper mapper) {
        return new UserBranchRepository() {
            @Override
            public com.reportsystem.auth.domain.model.UserBranch save(com.reportsystem.auth.domain.model.UserBranch ub) {
                return mapper.toDomain(jpaRepo.save(mapper.toEntity(ub)));
            }

            @Override
            public void delete(java.util.UUID userId, java.util.UUID branchId) {
                jpaRepo.findById(new com.reportsystem.auth.infrastructure.persistence.entity.UserBranchEntity.UserBranchId(userId, branchId))
                    .ifPresent(jpaRepo::delete);
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.UserBranch> findByUserId(java.util.UUID userId) {
                return jpaRepo.findByIdUserId(userId).stream().map(mapper::toDomain).toList();
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.UserBranch> findByBranchId(java.util.UUID branchId) {
                return jpaRepo.findByIdBranchId(branchId).stream().map(mapper::toDomain).toList();
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.UserBranch> findByTenantId(java.util.UUID tenantId) {
                // Requires a query; for now, return empty (not yet implemented)
                return java.util.List.of();
            }

            @Override
            public boolean existsByUserIdAndBranchId(java.util.UUID userId, java.util.UUID branchId) {
                return jpaRepo.existsById(new com.reportsystem.auth.infrastructure.persistence.entity.UserBranchEntity.UserBranchId(userId, branchId));
            }
        };
    }

    @Bean
    public FeatureRepository featureRepository(JpaFeatureRepository jpa, FeatureMapper mapper) {
        return new FeatureRepository() {
            @Override
            public com.reportsystem.auth.domain.model.Feature save(com.reportsystem.auth.domain.model.Feature feature) {
                return mapper.toDomain(jpa.save(mapper.toEntity(feature)));
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.Feature> findByCode(String code) {
                return jpa.findByCode(code).map(mapper::toDomain);
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.Feature> findAll() {
                return jpa.findAllByOrderByModuleAscSortOrderAsc().stream().map(mapper::toDomain).toList();
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.Feature> findByModule(String module) {
                return jpa.findByModuleOrderBySortOrder(module).stream().map(mapper::toDomain).toList();
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.Feature> findAllEnabledForClient(java.util.UUID tenantId) {
                return jpa.findAll().stream().map(mapper::toDomain).toList();
            }
        };
    }

    @Bean
    public ClientFeatureRepository clientFeatureRepository(JpaClientFeatureRepository jpa, ClientFeatureMapper mapper) {
        return new ClientFeatureRepository() {
            @Override
            public com.reportsystem.auth.domain.model.ClientFeature save(com.reportsystem.auth.domain.model.ClientFeature cf) {
                return mapper.toDomain(jpa.save(mapper.toEntity(cf)));
            }

            @Override
            public java.util.Optional<com.reportsystem.auth.domain.model.ClientFeature> findByTenantAndCode(java.util.UUID tenantId, String code) {
                return jpa.findByTenantIdAndFeatureCode(tenantId, code).map(mapper::toDomain);
            }

            @Override
            public java.util.List<com.reportsystem.auth.domain.model.ClientFeature> findByTenant(java.util.UUID tenantId) {
                return jpa.findByTenantId(tenantId).stream().map(mapper::toDomain).toList();
            }

            @Override
            public void deleteByTenantAndCode(java.util.UUID tenantId, String code) {
                jpa.deleteByTenantIdAndFeatureCode(tenantId, code);
            }

            @Override
            public java.util.Set<String> findEnabledCodesByTenant(java.util.UUID tenantId) {
                return jpa.findEnabledCodesByTenantId(tenantId);
            }
        };
    }
}
