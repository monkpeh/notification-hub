package com.notifyhub.integrity;

import com.notifyhub.campaign.CampaignEntity;
import com.notifyhub.campaign.CampaignRepository;
import com.notifyhub.config.TemplateConfigEntity;
import com.notifyhub.config.TemplateConfigRepository;
import com.notifyhub.security.TenantContext;
import com.notifyhub.template.TemplateEntity;
import com.notifyhub.template.TemplateRepository;
import com.notifyhub.window.CommWindowEntity;
import com.notifyhub.window.CommWindowRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntegrityCheckService {

    private static final Map<String, String> SEVERITY_BY_CHECK_TYPE = Map.of(
        "ORPHAN_PARENT_REF", "HIGH",
        "PARENT_POINTS_TO_CHILD", "HIGH",
        "MISSING_ACTIVE_CONFIG", "HIGH",
        "IVR_MISSING_CONTACT_FLOW", "HIGH",
        "CHILD_COUNT_ANOMALY", "LOW",
        "SHADOWED_CHILD_TEMPLATE", "HIGH",
        "CAMPAIGN_TARGET_OVERLAP", "MEDIUM"
    );

    private record ViolationKey(String checkType, String tableName, Long recordId) {}

    private final TemplateRepository templateRepository;
    private final TemplateConfigRepository templateConfigRepository;
    private final CommWindowRepository commWindowRepository;
    private final CampaignRepository campaignRepository;
    private final IntegrityViolationRepository integrityViolationRepository;

    public IntegrityCheckService(TemplateRepository templateRepository,
                                  TemplateConfigRepository templateConfigRepository,
                                  CommWindowRepository commWindowRepository,
                                  CampaignRepository campaignRepository,
                                  IntegrityViolationRepository integrityViolationRepository) {
        this.templateRepository = templateRepository;
        this.templateConfigRepository = templateConfigRepository;
        this.commWindowRepository = commWindowRepository;
        this.campaignRepository = campaignRepository;
        this.integrityViolationRepository = integrityViolationRepository;
    }

    public List<IntegrityViolationEntity> runScan() {
        String targetSchema = TenantContext.get();

        List<TemplateEntity> templates = templateRepository.findAll();
        List<TemplateConfigEntity> configs = templateConfigRepository.findAll();
        List<CommWindowEntity> windows = commWindowRepository.findAll();
        List<CampaignEntity> campaigns = campaignRepository.findAll();

        Map<Long, TemplateEntity> templateById = templates.stream()
            .collect(Collectors.toMap(TemplateEntity::getId, t -> t));

        Set<ViolationKey> detected = new LinkedHashSet<>();
        Map<ViolationKey, String> detailsByKey = new LinkedHashMap<>();

        for (TemplateEntity t : templates) {
            if (!t.isParent() && t.getParentTemplateId() != null && !templateById.containsKey(t.getParentTemplateId())) {
                record(detected, detailsByKey, "ORPHAN_PARENT_REF", "template", t.getId(),
                    "parentTemplateId " + t.getParentTemplateId() + " does not reference an existing template");
            }
        }

        for (TemplateEntity t : templates) {
            if (!t.isParent() && t.getParentTemplateId() != null) {
                TemplateEntity parent = templateById.get(t.getParentTemplateId());
                if (parent != null && !parent.isParent()) {
                    record(detected, detailsByKey, "PARENT_POINTS_TO_CHILD", "template", t.getId(),
                        "parentTemplateId " + t.getParentTemplateId() + " is itself a child - max hierarchy depth is 1");
                }
            }
        }

        Map<Long, List<TemplateConfigEntity>> configsByTemplate = configs.stream()
            .collect(Collectors.groupingBy(TemplateConfigEntity::getTemplateId));
        for (TemplateEntity t : templates) {
            if (!t.isParent()) {
                boolean hasActive = configsByTemplate.getOrDefault(t.getId(), List.of()).stream()
                    .anyMatch(TemplateConfigEntity::isUseActive);
                if (!hasActive) {
                    record(detected, detailsByKey, "MISSING_ACTIVE_CONFIG", "template", t.getId(),
                        "child template has no active template_config row - it cannot deliver on any channel");
                }
            }
        }

        for (TemplateConfigEntity c : configs) {
            if ("IVR".equals(c.getCommunicationMedium()) && (c.getContactFlowId() == null || c.getContactFlowId().isBlank())) {
                record(detected, detailsByKey, "IVR_MISSING_CONTACT_FLOW", "template_config", c.getId(),
                    "IVR config has no contactFlowId");
            }
        }

        Map<Long, Long> childCountByParent = templates.stream()
            .filter(t -> !t.isParent() && t.getParentTemplateId() != null)
            .collect(Collectors.groupingBy(TemplateEntity::getParentTemplateId, Collectors.counting()));
        for (TemplateEntity t : templates) {
            if (t.isParent()) {
                long count = childCountByParent.getOrDefault(t.getId(), 0L);
                if (count < 8 || count > 20) {
                    record(detected, detailsByKey, "CHILD_COUNT_ANOMALY", "template", t.getId(),
                        "parent template has " + count + " children - outside expected range of 8-20");
                }
            }
        }

        Map<Long, List<TemplateEntity>> childrenByParent = templates.stream()
            .filter(t -> !t.isParent() && t.getParentTemplateId() != null)
            .collect(Collectors.groupingBy(TemplateEntity::getParentTemplateId));
        for (List<TemplateEntity> siblings : childrenByParent.values()) {
            for (TemplateEntity a : siblings) {
                for (TemplateEntity b : siblings) {
                    if (a.getId().equals(b.getId())) continue;
                    if (isShadowedBy(a, b)) {
                        record(detected, detailsByKey, "SHADOWED_CHILD_TEMPLATE", "template", a.getId(),
                            "unreachable - sibling template " + b.getId() + " has broader targeting "
                                + "(customerType=" + b.getCustomerType() + ", language=" + b.getLanguage()
                                + ", priority=" + b.getPriority() + " vs this template's priority=" + a.getPriority() + ")");
                        break;
                    }
                }
            }
        }

        Map<Long, List<CommWindowEntity>> windowsByConfig = windows.stream()
            .collect(Collectors.groupingBy(CommWindowEntity::getConfigId));
        Map<Long, List<CommWindowEntity>> windowsByTemplate = new HashMap<>();
        for (TemplateConfigEntity c : configs) {
            windowsByTemplate.computeIfAbsent(c.getTemplateId(), k -> new ArrayList<>())
                .addAll(windowsByConfig.getOrDefault(c.getId(), List.of()));
        }

        Map<Long, List<TemplateEntity>> childrenByCampaign = templates.stream()
            .filter(t -> !t.isParent())
            .collect(Collectors.groupingBy(TemplateEntity::getCampaignId));
        List<CampaignEntity> activeCampaigns = campaigns.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()))
            .toList();

        for (int i = 0; i < activeCampaigns.size(); i++) {
            for (int j = i + 1; j < activeCampaigns.size(); j++) {
                CampaignEntity c1 = activeCampaigns.get(i);
                CampaignEntity c2 = activeCampaigns.get(j);
                List<TemplateEntity> t1s = childrenByCampaign.getOrDefault(c1.getId(), List.of());
                List<TemplateEntity> t2s = childrenByCampaign.getOrDefault(c2.getId(), List.of());

                String overlapDetail = findOverlap(t1s, t2s, windowsByTemplate);
                if (overlapDetail != null) {
                    record(detected, detailsByKey, "CAMPAIGN_TARGET_OVERLAP", "campaign", c1.getId(),
                        "overlaps with campaign " + c2.getId() + " (\"" + c2.getName() + "\") - " + overlapDetail);
                    record(detected, detailsByKey, "CAMPAIGN_TARGET_OVERLAP", "campaign", c2.getId(),
                        "overlaps with campaign " + c1.getId() + " (\"" + c1.getName() + "\") - " + overlapDetail);
                }
            }
        }

        return reconcile(targetSchema, detected, detailsByKey);
    }

    private boolean isShadowedBy(TemplateEntity a, TemplateEntity b) {
        boolean customerTypeBroaderOrEqual = b.getCustomerType() == null || Objects.equals(b.getCustomerType(), a.getCustomerType());
        boolean languageBroaderOrEqual = b.getLanguage() == null || Objects.equals(b.getLanguage(), a.getLanguage());
        boolean identicalTargeting = Objects.equals(a.getCustomerType(), b.getCustomerType()) && Objects.equals(a.getLanguage(), b.getLanguage());
        return customerTypeBroaderOrEqual && languageBroaderOrEqual && !identicalTargeting;
    }

    private String findOverlap(List<TemplateEntity> t1s, List<TemplateEntity> t2s, Map<Long, List<CommWindowEntity>> windowsByTemplate) {
        for (TemplateEntity t1 : t1s) {
            for (TemplateEntity t2 : t2s) {
                boolean customerTypeOverlaps = t1.getCustomerType() == null || t2.getCustomerType() == null
                    || Objects.equals(t1.getCustomerType(), t2.getCustomerType());
                boolean languageOverlaps = t1.getLanguage() == null || t2.getLanguage() == null
                    || Objects.equals(t1.getLanguage(), t2.getLanguage());

                if (customerTypeOverlaps && languageOverlaps
                    && windowsOverlap(windowsByTemplate.getOrDefault(t1.getId(), List.of()),
                                       windowsByTemplate.getOrDefault(t2.getId(), List.of()))) {
                    return "template " + t1.getId() + " and template " + t2.getId()
                        + " target overlapping segments in overlapping windows";
                }
            }
        }
        return null;
    }

    private boolean windowsOverlap(List<CommWindowEntity> aWindows, List<CommWindowEntity> bWindows) {
        // No windows defined = treated as unrestricted/always-active, not "no data to compare."
        if (aWindows.isEmpty() || bWindows.isEmpty()) {
            return true;
        }
        for (CommWindowEntity a : aWindows) {
            for (CommWindowEntity b : bWindows) {
                boolean disjoint = !a.getEndWindow().isAfter(b.getStartWindow()) || !b.getEndWindow().isAfter(a.getStartWindow());
                if (!disjoint) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<IntegrityViolationEntity> findFiltered(String status, String checkType, String targetSchema) {
        Specification<IntegrityViolationEntity> spec = Specification
            .where(IntegrityViolationSpecifications.hasStatus(status))
            .and(IntegrityViolationSpecifications.hasCheckType(checkType))
            .and(IntegrityViolationSpecifications.hasTargetSchema(targetSchema));

        return integrityViolationRepository.findAll(spec);
    }

    private void record(Set<ViolationKey> detected, Map<ViolationKey, String> detailsByKey,
                         String checkType, String tableName, Long recordId, String details) {
        ViolationKey key = new ViolationKey(checkType, tableName, recordId);
        detected.add(key);
        detailsByKey.put(key, details);
    }

    private List<IntegrityViolationEntity> reconcile(String targetSchema, Set<ViolationKey> detected,
                                                       Map<ViolationKey, String> detailsByKey) {
        List<IntegrityViolationEntity> openExisting = integrityViolationRepository.findByStatusAndTargetSchema("OPEN", targetSchema);
        Map<ViolationKey, IntegrityViolationEntity> existingByKey = openExisting.stream()
            .collect(Collectors.toMap(v -> new ViolationKey(v.getCheckType(), v.getTableName(), v.getRecordId()), v -> v));

        OffsetDateTime now = OffsetDateTime.now();
        List<IntegrityViolationEntity> result = new ArrayList<>();

        for (ViolationKey key : detected) {
            IntegrityViolationEntity existing = existingByKey.get(key);
            if (existing != null) {
                existing.setLastDetectedAt(now);
                result.add(integrityViolationRepository.save(existing));
            } else {
                IntegrityViolationEntity created = new IntegrityViolationEntity(
                    key.checkType(), key.tableName(), key.recordId(),
                    SEVERITY_BY_CHECK_TYPE.getOrDefault(key.checkType(), "MEDIUM"),
                    detailsByKey.get(key), targetSchema
                );
                result.add(integrityViolationRepository.save(created));
            }
        }

        for (IntegrityViolationEntity existing : openExisting) {
            ViolationKey key = new ViolationKey(existing.getCheckType(), existing.getTableName(), existing.getRecordId());
            if (!detected.contains(key)) {
                existing.setStatus("RESOLVED");
                existing.setResolvedAt(now);
                integrityViolationRepository.save(existing);
            }
        }

        return result;
    }
}
