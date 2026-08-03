package com.notifyhub.config;

public record CreateTemplateConfigRequest(
    String communicationMedium,
    String externalTemplateId,
    boolean useActive,
    String contactFlowId
) {}
