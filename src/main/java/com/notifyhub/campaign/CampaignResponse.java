package com.notifyhub.campaign;

import java.time.Instant;

public record CampaignResponse(
    Long id,
    String name,
    String businessPurpose,
    String status,
    String owner,
    Instant createdAt,
    Instant updatedAt
) {
    public static CampaignResponse from(CampaignEntity entity) {
        return new CampaignResponse(
            entity.getId(),
            entity.getName(),
            entity.getBusinessPurpose(),
            entity.getStatus(),
            entity.getOwner(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
