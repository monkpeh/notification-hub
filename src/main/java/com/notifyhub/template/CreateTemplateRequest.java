package com.notifyhub.template;

public record CreateTemplateRequest(
    Long campaignId,
    Long parentTemplateId,
    boolean isParent,
    String templateName,
    String templateDescription,
    String customerType,
    String language,
    Integer priority,
    String eventType,
    String status
) {}
