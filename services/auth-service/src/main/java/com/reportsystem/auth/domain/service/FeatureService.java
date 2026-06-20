package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.ClientFeature;
import com.reportsystem.auth.domain.model.Feature;
import com.reportsystem.auth.domain.port.outbound.ClientFeatureRepository;
import com.reportsystem.auth.domain.port.outbound.FeatureRepository;
import com.reportsystem.auth.domain.port.outbound.TenantRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * FeatureService — combines the features catalog (FeatureRepository) with
 * per-client enablement (ClientFeatureRepository) and the tenant's tier to
 * answer "is this code enabled for this tenant?".
 *
 * Resolution algorithm:
 *   1. Look up tenant — if missing, deny.
 *   2. Look up the feature in the catalog — if missing, deny.
 *   3. If feature.deprecated, deny.
 *   4. If the client is on a tier below the feature's required tier, deny.
 *   5. If there's an explicit ClientFeature record, return its `enabled` flag.
 *   6. Otherwise, default-on for the tenant's tier:
 *        - BASIC     → BASIC features are on
 *        - PRO       → BASIC + PRO features are on
 *        - ENTERPRISE → all features are on
 */
@Service
public class FeatureService {

    private final FeatureRepository featureRepo;
    private final ClientFeatureRepository clientFeatureRepo;
    private final TenantRepository tenantRepo;

    public FeatureService(FeatureRepository featureRepo,
                          ClientFeatureRepository clientFeatureRepo,
                          TenantRepository tenantRepo) {
        this.featureRepo = featureRepo;
        this.clientFeatureRepo = clientFeatureRepo;
        this.tenantRepo = tenantRepo;
    }

    public boolean isEnabled(UUID tenantId, String featureCode) {
        if (tenantId == null || featureCode == null || featureCode.isBlank()) {
            return false;
        }
        var tenantOpt = tenantRepo.findById(tenantId);
        if (tenantOpt.isEmpty()) { return false; }
        var tenant = tenantOpt.get();

        var featureOpt = featureRepo.findByCode(featureCode);
        if (featureOpt.isEmpty()) { return false; }
        var feature = featureOpt.get();
        if (feature.isDeprecated()) { return false; }

        if (!tierMeets(getTierRank(tenant.getTier()), getTierRank(feature.getTierRequired()))) {
            return false;
        }

        // Explicit override?
        var cfOpt = clientFeatureRepo.findByTenantAndCode(tenantId, featureCode);
        if (cfOpt.isPresent()) {
            var cf = cfOpt.get();
            if (cf.getExpiresAt() != null && cf.getExpiresAt().isBefore(Instant.now())) {
                return false;
            }
            return cf.isEnabled();
        }

        // Default: on if the client's tier is high enough
        return true;
    }

    public Map<String, Boolean> enabledFeaturesFor(UUID tenantId) {
        var tenantOpt = tenantRepo.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            return Map.of();
        }
        var tenant = tenantOpt.get();
        int tenantTier = getTierRank(tenant.getTier());

        List<Feature> allFeatures = featureRepo.findAll();
        List<ClientFeature> clientFeatures = clientFeatureRepo.findByTenant(tenantId);
        Map<String, ClientFeature> cfMap = clientFeatures.stream()
                .collect(Collectors.toMap(ClientFeature::getFeatureCode, cf -> cf));

        Map<String, Boolean> result = new HashMap<>();
        for (Feature f : allFeatures) {
            if (f.isDeprecated()) {
                result.put(f.getCode(), false);
                continue;
            }
            if (!tierMeets(tenantTier, getTierRank(f.getTierRequired()))) {
                result.put(f.getCode(), false);
                continue;
            }

            ClientFeature cf = cfMap.get(f.getCode());
            if (cf != null) {
                if (cf.getExpiresAt() != null && cf.getExpiresAt().isBefore(Instant.now())) {
                    result.put(f.getCode(), false);
                } else {
                    result.put(f.getCode(), cf.isEnabled());
                }
            } else {
                result.put(f.getCode(), true);
            }
        }
        return result;
    }

    public List<Feature> listCatalog() {
        return featureRepo.findAll();
    }

    public List<Map<String, Object>> featureTreeFor(UUID tenantId) {
        var tenantOpt = tenantRepo.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            return List.of();
        }
        var tenant = tenantOpt.get();
        int tenantTier = getTierRank(tenant.getTier());

        List<Feature> allFeatures = featureRepo.findAll();
        Set<String> enabled = clientFeatureRepo.findEnabledCodesByTenant(tenantId);
        List<ClientFeature> clientFeatures = clientFeatureRepo.findByTenant(tenantId);
        Map<String, ClientFeature> cfMap = clientFeatures.stream()
                .collect(Collectors.toMap(ClientFeature::getFeatureCode, cf -> cf));

        Map<String, List<Feature>> byModule = new HashMap<>();
        for (Feature f : allFeatures) {
            byModule.computeIfAbsent(f.getModule(), k -> new ArrayList<>()).add(f);
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (var entry : byModule.entrySet()) {
            Map<String, Object> moduleNode = new HashMap<>();
            moduleNode.put("module", entry.getKey());
            moduleNode.put("features", entry.getValue().stream().map(f -> {
                Map<String, Object> node = new HashMap<>();
                node.put("code", f.getCode());
                node.put("name", f.getName());
                node.put("description", f.getDescription());
                node.put("tierRequired", f.getTierRequired());

                // Calculate enabled flag with single-pass logic (no more DB calls)
                boolean isEnabled;
                if (f.isDeprecated()) {
                    isEnabled = false;
                } else if (!tierMeets(tenantTier, getTierRank(f.getTierRequired()))) {
                    isEnabled = false;
                } else {
                    ClientFeature cf = cfMap.get(f.getCode());
                    if (cf != null) {
                        if (cf.getExpiresAt() != null && cf.getExpiresAt().isBefore(Instant.now())) {
                            isEnabled = false;
                        } else {
                            isEnabled = cf.isEnabled();
                        }
                    } else {
                        isEnabled = true;
                    }
                }

                node.put("enabled", isEnabled);
                node.put("explicitlyEnabled", enabled.contains(f.getCode()));
                return node;
            }).toList());
            tree.add(moduleNode);
        }
        return tree;
    }

    public ClientFeature enable(UUID tenantId, String featureCode, UUID enabledBy) {
        ClientFeature cf = clientFeatureRepo.findByTenantAndCode(tenantId, featureCode)
                .orElse(ClientFeature.builder()
                        .tenantId(tenantId)
                        .featureCode(featureCode)
                        .enabled(false)
                        .enabledAt(Instant.now())
                        .enabledBy(enabledBy)
                        .build());
        cf = ClientFeature.builder()
                .id(cf.getId())
                .tenantId(cf.getTenantId())
                .featureCode(cf.getFeatureCode())
                .enabled(true)
                .enabledAt(Instant.now())
                .enabledBy(enabledBy)
                .expiresAt(cf.getExpiresAt())
                .notes(cf.getNotes())
                .build();
        return clientFeatureRepo.save(cf);
    }

    public ClientFeature disable(UUID tenantId, String featureCode) {
        return clientFeatureRepo.findByTenantAndCode(tenantId, featureCode)
                .map(cf -> {
                    var disabled = ClientFeature.builder()
                            .id(cf.getId())
                            .tenantId(cf.getTenantId())
                            .featureCode(cf.getFeatureCode())
                            .enabled(false)
                            .enabledAt(cf.getEnabledAt())
                            .enabledBy(cf.getEnabledBy())
                            .expiresAt(cf.getExpiresAt())
                            .notes(cf.getNotes())
                            .build();
                    return clientFeatureRepo.save(disabled);
                })
                .orElse(null);
    }

    private static int getTierRank(String tier) {
        if (tier == null) { return 0; }
        return switch (tier) {
            case "BASIC" -> 1;
            case "PRO" -> 2;
            case "ENTERPRISE" -> 3;
            default -> 0;
        };
    }

    private static boolean tierMeets(int actual, int required) {
        return actual >= required;
    }
}