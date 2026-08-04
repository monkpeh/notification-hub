package com.notifyhub.common;

import com.notifyhub.campaign.CreateCampaignRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FieldValidationService {

    private static final Set<String> CAMPAIGN_STATUSES = Set.of("ACTIVE", "INACTIVE");

    public void validateCampaignCreate(CreateCampaignRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.name() == null || request.name().isBlank()) {
            errors.add(new ValidationError("name", request.name(), "must not be blank"));
        } else if (request.name().length() > 150) {
            errors.add(new ValidationError("name", request.name(), "must be 150 characters or fewer"));
        }

        if (request.status() == null || !CAMPAIGN_STATUSES.contains(request.status())) {
            errors.add(new ValidationError("status", request.status(), "must be one of " + CAMPAIGN_STATUSES));
        }

        if (request.businessPurpose() != null && request.businessPurpose().length() > 255) {
            errors.add(new ValidationError("businessPurpose", request.businessPurpose(), "must be 255 characters or fewer"));
        }

        if (request.owner() != null && request.owner().length() > 100) {
            errors.add(new ValidationError("owner", request.owner(), "must be 100 characters or fewer"));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
