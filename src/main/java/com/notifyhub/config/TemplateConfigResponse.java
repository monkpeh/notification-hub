package com.notifyhub.config;

import java.time.Instant;

public record TemplateConfigResponse(
    Long id,
    Long templateId,
    String communicationMedium,
    String externalTemplateId,
    boolean useActive,
    String contactFlowId,
    Instant createdAt,
    Instant updatedAt
) {
    public static TemplateConfigResponse from(TemplateConfigEntity entity) {
        return new TemplateConfigResponse(
            entity.getId(),
            entity.getTemplateId(),
            entity.getCommunicationMedium(),
            entity.getExternalTemplateId(),
            entity.isUseActive(),
            entity.getContactFlowId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
