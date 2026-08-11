package com.notifyhub.integrity;

import com.notifyhub.config.TemplateConfigEntity;
import com.notifyhub.config.TemplateConfigRepository;
import com.notifyhub.security.TenantContext;
import com.notifyhub.template.TemplateEntity;
import com.notifyhub.template.TemplateRepository;
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
        "CHILD_COUNT_ANOMALY", "LOW"
    );

    private record ViolationKey(String checkType, String tableName, Long recordId) {}

    private final TemplateRepository templateRepository;
    private final TemplateConfigRepository templateConfigRepository;
    private final IntegrityViolationRepository integrityViolationRepository;

    public IntegrityCheckService(TemplateRepository templateRepository,
                                  TemplateConfigRepository templateConfigRepository,
                                  IntegrityViolationRepository integrityViolationRepository) {
        this.templateRepository = templateRepository;
        this.templateConfigRepository = templateConfigRepository;
        this.integrityViolationRepository = integrityViolationRepository;
    }

    public List<IntegrityViolationEntity> runScan() {
        String targetSchema = TenantContext.get();

        List<TemplateEntity> templates = templateRepository.findAll();
        List<TemplateConfigEntity> configs = templateConfigRepository.findAll();

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

        return reconcile(targetSchema, detected, detailsByKey);
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
