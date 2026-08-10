package com.notifyhub.audit;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
    Long id,
    String tableName,
    Long recordId,
    String fieldName,
    String oldValue,
    String newValue,
    Long actorId,
    String reason,
    Long editRequestId,
    OffsetDateTime changedAt,
    UUID changeBatchId,
    String targetSchema
) {
    public static AuditLogResponse from(AuditLogEntity entity) {
        return new AuditLogResponse(
            entity.getId(),
            entity.getTableName(),
            entity.getRecordId(),
            entity.getFieldName(),
            entity.getOldValue(),
            entity.getNewValue(),
            entity.getActorId(),
            entity.getReason(),
            entity.getEditRequestId(),
            entity.getChangedAt(),
            entity.getChangeBatchId(),
            entity.getTargetSchema()
        );
    }
}
