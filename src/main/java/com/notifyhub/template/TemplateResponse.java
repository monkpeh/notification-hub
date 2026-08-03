package com.notifyhub.template;

import java.time.Instant;

public record TemplateResponse(
    Long id,
    Long campaignId,
    Long parentTemplateId,
    boolean isParent,
    String templateName,
    String templateDescription,
    String customerType,
    String language,
    Integer priority,
    String eventType,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
    public static TemplateResponse from(TemplateEntity entity) {
        return new TemplateResponse(
            entity.getId(),
            entity.getCampaignId(),
            entity.getParentTemplateId(),
            entity.isParent(),
            entity.getTemplateName(),
            entity.getTemplateDescription(),
            entity.getCustomerType(),
            entity.getLanguage(),
            entity.getPriority(),
            entity.getEventType(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
