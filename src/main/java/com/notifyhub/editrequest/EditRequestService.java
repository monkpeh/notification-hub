package com.notifyhub.editrequest;

import com.notifyhub.common.FieldValidationException;
import com.notifyhub.common.ValidationError;
import com.notifyhub.security.CurrentUser;
import com.notifyhub.template.TemplateEntity;
import com.notifyhub.template.TemplateRepository;
import com.notifyhub.template.UpdateTemplateRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EditRequestService {

    private final EditRequestRepository editRequestRepository;
    private final TemplateRepository templateRepository;

    public EditRequestService(EditRequestRepository editRequestRepository, TemplateRepository templateRepository) {
        this.editRequestRepository = editRequestRepository;
        this.templateRepository = templateRepository;
    }

    public List<EditRequestEntity> createPendingTemplateEdits(TemplateEntity current, UpdateTemplateRequest request, CurrentUser requestedBy) {
        List<EditRequestEntity> created = new ArrayList<>();

        addIfChanged(created, current.getId(), "templateName", current.getTemplateName(), request.templateName(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "templateDescription", current.getTemplateDescription(), request.templateDescription(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "customerType", current.getCustomerType(), request.customerType(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "language", current.getLanguage(), request.language(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "priority", current.getPriority(), request.priority(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "status", current.getStatus(), request.status(), requestedBy, request.reason());

        return created;
    }

    public List<EditRequestEntity> findFiltered(String status, Long recordId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new FieldValidationException(List.of(
                new ValidationError("dateFrom", dateFrom, "must not be after dateTo")
            ));
        }

        Specification<EditRequestEntity> spec = Specification
            .where(EditRequestSpecifications.hasStatus(status))
            .and(EditRequestSpecifications.hasRecordId(recordId))
            .and(EditRequestSpecifications.requestedAfter(dateFrom))
            .and(EditRequestSpecifications.requestedBefore(dateTo));

        return editRequestRepository.findAll(spec);
    }

    public EditRequestResponse toResponse(EditRequestEntity request) {
        boolean stale = "PENDING".equals(request.getStatus())
            ? computeLiveStaleness(request)
            : request.isApprovedWithOverride();
        return EditRequestResponse.from(request, stale);
    }

    public EditRequestEntity approve(Long id, CurrentUser resolvedBy, boolean force) {
        EditRequestEntity request = getPendingOrThrow(id);

        if (!"template".equals(request.getTableName())) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Approval not yet supported for table: " + request.getTableName());
        }

        TemplateEntity entity = templateRepository.findById(request.getRecordId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + request.getRecordId()));

        String currentValue = extractTemplateFieldValue(entity, request.getFieldName());
        boolean stale = !Objects.equals(currentValue, request.getOldValue());

        if (stale && !force) {
            throw new StaleEditConflictException(currentValue, request.getOldValue(), request.getNewValue());
        }

        applyTemplateFieldChange(entity, request.getFieldName(), request.getNewValue());
        templateRepository.save(entity);

        request.setStatus("APPROVED");
        request.setResolvedBy(resolvedBy.userId());
        request.setResolvedAt(OffsetDateTime.now());
        request.setApprovedWithOverride(stale);
        return editRequestRepository.save(request);
    }

    public EditRequestEntity reject(Long id, CurrentUser resolvedBy) {
        EditRequestEntity request = getPendingOrThrow(id);

        request.setStatus("REJECTED");
        request.setResolvedBy(resolvedBy.userId());
        request.setResolvedAt(OffsetDateTime.now());
        return editRequestRepository.save(request);
    }

    private boolean computeLiveStaleness(EditRequestEntity request) {
        if (!"template".equals(request.getTableName())) {
            return false;
        }
        return templateRepository.findById(request.getRecordId())
            .map(entity -> !Objects.equals(extractTemplateFieldValue(entity, request.getFieldName()), request.getOldValue()))
            .orElse(false);
    }

    private EditRequestEntity getPendingOrThrow(Long id) {
        EditRequestEntity request = editRequestRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Edit request not found: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Edit request is already " + request.getStatus());
        }

        return request;
    }

    private String extractTemplateFieldValue(TemplateEntity entity, String fieldName) {
        Object value = switch (fieldName) {
            case "templateName" -> entity.getTemplateName();
            case "templateDescription" -> entity.getTemplateDescription();
            case "customerType" -> entity.getCustomerType();
            case "language" -> entity.getLanguage();
            case "priority" -> entity.getPriority();
            case "status" -> entity.getStatus();
            default -> null;
        };
        return value == null ? null : value.toString();
    }

    private void applyTemplateFieldChange(TemplateEntity entity, String fieldName, String newValue) {
        switch (fieldName) {
            case "templateName" -> entity.setTemplateName(newValue);
            case "templateDescription" -> entity.setTemplateDescription(newValue);
            case "customerType" -> entity.setCustomerType(newValue);
            case "language" -> entity.setLanguage(newValue);
            case "priority" -> entity.setPriority(newValue == null ? null : Integer.parseInt(newValue));
            case "status" -> entity.setStatus(newValue);
            default -> throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Unsupported field for approval: " + fieldName);
        }
    }

    private void addIfChanged(List<EditRequestEntity> created, Long recordId, String fieldName,
                               Object oldValue, Object newValue, CurrentUser requestedBy, String reason) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        EditRequestEntity entity = new EditRequestEntity(
            "template",
            recordId,
            fieldName,
            oldValue == null ? null : oldValue.toString(),
            newValue == null ? null : newValue.toString(),
            requestedBy.userId(),
            reason,
            "public"
        );

        created.add(editRequestRepository.save(entity));
    }
}
