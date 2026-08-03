package com.notifyhub.campaign;

public record CreateCampaignRequest(
    String name,
    String businessPurpose,
    String status,
    String owner
) {}
