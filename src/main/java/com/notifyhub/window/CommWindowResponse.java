package com.notifyhub.window;

import java.time.Instant;
import java.time.LocalTime;

public record CommWindowResponse(
    Long id,
    Long configId,
    LocalTime startWindow,
    LocalTime endWindow,
    String occurrence,
    Instant createdAt,
    Instant updatedAt
) {
    public static CommWindowResponse from(CommWindowEntity entity) {
        return new CommWindowResponse(
            entity.getId(),
            entity.getConfigId(),
            entity.getStartWindow(),
            entity.getEndWindow(),
            entity.getOccurrence(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
