package com.notifyhub.audit;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public record FieldChange(String fieldName, String oldValue, String newValue) {}

    public List<AuditLogEntity> recordBatch(String tableName, Long recordId, List<FieldChange> changes,
                                             Long actorId, String reason, Long editRequestId, String targetSchema) {
        if (changes.isEmpty()) {
            return List.of();
        }

        UUID changeBatchId = UUID.randomUUID();
        List<AuditLogEntity> saved = new ArrayList<>();

        for (FieldChange change : changes) {
            AuditLogEntity entity = new AuditLogEntity(
                tableName, recordId, change.fieldName(), change.oldValue(), change.newValue(),
                actorId, reason, editRequestId, changeBatchId, targetSchema
            );
            saved.add(auditLogRepository.save(entity));
        }

        return saved;
    }
}
