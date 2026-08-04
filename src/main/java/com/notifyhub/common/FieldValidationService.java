package com.notifyhub.common;

import com.notifyhub.campaign.CreateCampaignRequest;
import com.notifyhub.template.CreateTemplateRequest;
import com.notifyhub.template.TemplateEntity;
import com.notifyhub.template.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FieldValidationService {

    private static final Set<String> CAMPAIGN_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> TEMPLATE_STATUSES = Set.of("Y", "N");

    private final TemplateRepository templateRepository;

    public FieldValidationService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

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

    public void validateTemplateCreate(CreateTemplateRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.campaignId() == null) {
            errors.add(new ValidationError("campaignId", null, "must not be null"));
        }

        if (request.templateName() == null || request.templateName().isBlank()) {
            errors.add(new ValidationError("templateName", request.templateName(), "must not be blank"));
        } else if (request.templateName().length() > 150) {
            errors.add(new ValidationError("templateName", request.templateName(), "must be 150 characters or fewer"));
        }

        if (request.status() == null || !TEMPLATE_STATUSES.contains(request.status())) {
            errors.add(new ValidationError("status", request.status(), "must be one of " + TEMPLATE_STATUSES));
        }

        // REQ-2: hierarchy rules
        if (request.isParent()) {
            if (request.parentTemplateId() != null) {
                errors.add(new ValidationError("parentTemplateId", request.parentTemplateId(),
                    "must be null when isParent is true"));
            }
        } else {
            if (request.parentTemplateId() == null) {
                errors.add(new ValidationError("parentTemplateId", null,
                    "required when isParent is false"));
            } else {
                Optional<TemplateEntity> parentOpt = templateRepository.findById(request.parentTemplateId());
                if (parentOpt.isEmpty()) {
                    errors.add(new ValidationError("parentTemplateId", request.parentTemplateId(),
                        "must reference an existing template"));
                } else if (!parentOpt.get().isParent()) {
                    errors.add(new ValidationError("parentTemplateId", request.parentTemplateId(),
                        "must reference a template where isParent is true — max hierarchy depth is 1"));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
