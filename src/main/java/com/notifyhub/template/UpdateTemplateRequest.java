package com.notifyhub.template;

public record UpdateTemplateRequest(
    String templateName,
    String templateDescription,
    String customerType,
    String language,
    Integer priority,
    String status
) {}
