package com.notifyhub.common;

import com.notifyhub.campaign.CreateCampaignRequest;
import com.notifyhub.config.CreateTemplateConfigRequest;
import com.notifyhub.template.CreateTemplateRequest;
import com.notifyhub.template.TemplateEntity;
import com.notifyhub.template.TemplateRepository;
import com.notifyhub.template.UpdateTemplateRequest;
import com.notifyhub.window.CreateCommWindowRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class FieldValidationService {

    private static final Set<String> CAMPAIGN_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> TEMPLATE_STATUSES = Set.of("Y", "N");
    private static final Set<String> COMMUNICATION_MEDIUMS = Set.of("EMAIL", "SMS", "IVR", "PUSH", "RCS");
    private static final Set<String> IMMUTABLE_TEMPLATE_FIELDS = Set.of("isParent", "eventType", "campaignId", "parentTemplateId");

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

    public void validateNoImmutableFieldsInUpdate(Map<String, Object> rawBody) {
        List<ValidationError> errors = new ArrayList<>();

        for (String field : IMMUTABLE_TEMPLATE_FIELDS) {
            if (rawBody.containsKey(field)) {
                errors.add(new ValidationError(field, rawBody.get(field),
                    "field is immutable after template creation and cannot be modified via update"));
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    public void validateTemplateUpdate(UpdateTemplateRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.templateName() == null || request.templateName().isBlank()) {
            errors.add(new ValidationError("templateName", request.templateName(), "must not be blank"));
        } else if (request.templateName().length() > 150) {
            errors.add(new ValidationError("templateName", request.templateName(), "must be 150 characters or fewer"));
        }

        if (request.status() == null || !TEMPLATE_STATUSES.contains(request.status())) {
            errors.add(new ValidationError("status", request.status(), "must be one of " + TEMPLATE_STATUSES));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    public void validateTemplateConfigCreate(CreateTemplateConfigRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.communicationMedium() == null || !COMMUNICATION_MEDIUMS.contains(request.communicationMedium())) {
            errors.add(new ValidationError("communicationMedium", request.communicationMedium(),
                "must be one of " + COMMUNICATION_MEDIUMS));
        }

        if ("IVR".equals(request.communicationMedium())
            && (request.contactFlowId() == null || request.contactFlowId().isBlank())) {
            errors.add(new ValidationError("contactFlowId", request.contactFlowId(),
                "required when communicationMedium is IVR"));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    public void validateCommWindowCreate(CreateCommWindowRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.startWindow() == null) {
            errors.add(new ValidationError("startWindow", null, "must not be null"));
        }

        if (request.endWindow() == null) {
            errors.add(new ValidationError("endWindow", null, "must not be null"));
        }

        if (request.startWindow() != null && request.endWindow() != null
            && !request.startWindow().isBefore(request.endWindow())) {
            errors.add(new ValidationError("startWindow", request.startWindow(),
                "must be before endWindow"));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
