package com.notifyhub.audit;

import com.notifyhub.common.FieldValidationException;
import com.notifyhub.common.ValidationError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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

    public Page<AuditLogEntity> findFiltered(String tableName, Long recordId, String targetSchema,
                                              OffsetDateTime dateFrom, OffsetDateTime dateTo, Pageable pageable) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new FieldValidationException(List.of(
                new ValidationError("dateFrom", dateFrom, "must not be after dateTo")
            ));
        }

        Specification<AuditLogEntity> spec = Specification
            .where(AuditLogSpecifications.hasTableName(tableName))
            .and(AuditLogSpecifications.hasRecordId(recordId))
            .and(AuditLogSpecifications.hasTargetSchema(targetSchema))
            .and(AuditLogSpecifications.changedAfter(dateFrom))
            .and(AuditLogSpecifications.changedBefore(dateTo));

        return auditLogRepository.findAll(spec, pageable);
    }
}
