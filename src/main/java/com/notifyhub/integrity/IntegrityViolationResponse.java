package com.notifyhub.integrity;

import java.time.OffsetDateTime;

public record IntegrityViolationResponse(
    Long id,
    String checkType,
    String tableName,
    Long recordId,
    String severity,
    String details,
    String targetSchema,
    String status,
    OffsetDateTime firstDetectedAt,
    OffsetDateTime lastDetectedAt,
    OffsetDateTime resolvedAt
) {
    public static IntegrityViolationResponse from(IntegrityViolationEntity entity) {
        return new IntegrityViolationResponse(
            entity.getId(), entity.getCheckType(), entity.getTableName(), entity.getRecordId(),
            entity.getSeverity(), entity.getDetails(), entity.getTargetSchema(), entity.getStatus(),
            entity.getFirstDetectedAt(), entity.getLastDetectedAt(), entity.getResolvedAt()
        );
    }
}
