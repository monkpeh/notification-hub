package com.notifyhub.editrequest;

import java.time.OffsetDateTime;

public record EditRequestResponse(
    Long id,
    String tableName,
    Long recordId,
    String fieldName,
    String oldValue,
    String newValue,
    Long requestedBy,
    String status,
    String reason,
    OffsetDateTime requestedAt,
    String targetSchema
) {
    public static EditRequestResponse from(EditRequestEntity entity) {
        return new EditRequestResponse(
            entity.getId(),
            entity.getTableName(),
            entity.getRecordId(),
            entity.getFieldName(),
            entity.getOldValue(),
            entity.getNewValue(),
            entity.getRequestedBy(),
            entity.getStatus(),
            entity.getReason(),
            entity.getRequestedAt(),
            entity.getTargetSchema()
        );
    }
}
