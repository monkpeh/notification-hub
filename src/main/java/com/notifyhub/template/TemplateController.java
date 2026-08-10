package com.notifyhub.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notifyhub.audit.AuditLogService;
import com.notifyhub.common.FieldValidationService;
import com.notifyhub.editrequest.EditRequestEntity;
import com.notifyhub.editrequest.EditRequestResponse;
import com.notifyhub.editrequest.EditRequestService;
import com.notifyhub.security.CurrentUser;
import com.notifyhub.security.CurrentUserContext;
import com.notifyhub.security.RateLimited;
import com.notifyhub.security.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    private static final Set<String> TIER_1_ROLES = Set.of("SUPER_ADMIN", "ADMIN");

    private final TemplateRepository templateRepository;
    private final FieldValidationService fieldValidationService;
    private final EditRequestService editRequestService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public TemplateController(TemplateRepository templateRepository, FieldValidationService fieldValidationService,
                               EditRequestService editRequestService, AuditLogService auditLogService,
                               ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.fieldValidationService = fieldValidationService;
        this.editRequestService = editRequestService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{id}")
    public TemplateResponse getById(@PathVariable Long id) {
        TemplateEntity entity = templateRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));
        return TemplateResponse.from(entity);
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "ADMIN", "TEMPLATE_BUILDER", "AI_AGENT"})
    @RateLimited
    public ResponseEntity<TemplateResponse> create(@RequestBody CreateTemplateRequest request) {
        fieldValidationService.validateTemplateCreate(request);

        TemplateEntity entity = new TemplateEntity(
            request.campaignId(),
            request.parentTemplateId(),
            request.isParent(),
            request.templateName(),
            request.templateDescription(),
            request.customerType(),
            request.language(),
            request.priority(),
            request.eventType(),
            request.status()
        );
        TemplateEntity saved = templateRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(TemplateResponse.from(saved));
    }

    @PutMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "ADMIN", "TEMPLATE_BUILDER", "AI_AGENT"})
    @RateLimited
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> rawBody) {
        fieldValidationService.validateNoImmutableFieldsInUpdate(rawBody);

        UpdateTemplateRequest request = objectMapper.convertValue(rawBody, UpdateTemplateRequest.class);
        fieldValidationService.validateTemplateUpdate(request);

        TemplateEntity entity = templateRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));

        CurrentUser currentUser = CurrentUserContext.get();

        if (TIER_1_ROLES.contains(currentUser.roleName())) {
            List<AuditLogService.FieldChange> changes = new ArrayList<>();
            addIfChanged(changes, "templateName", entity.getTemplateName(), request.templateName());
            addIfChanged(changes, "templateDescription", entity.getTemplateDescription(), request.templateDescription());
            addIfChanged(changes, "customerType", entity.getCustomerType(), request.customerType());
            addIfChanged(changes, "language", entity.getLanguage(), request.language());
            addIfChanged(changes, "priority", entity.getPriority(), request.priority());
            addIfChanged(changes, "status", entity.getStatus(), request.status());

            entity.setTemplateName(request.templateName());
            entity.setTemplateDescription(request.templateDescription());
            entity.setCustomerType(request.customerType());
            entity.setLanguage(request.language());
            entity.setPriority(request.priority());
            entity.setStatus(request.status());

            TemplateEntity saved = templateRepository.save(entity);

            auditLogService.recordBatch("template", saved.getId(), changes, currentUser.userId(), request.reason(), null, "public");

            return ResponseEntity.ok(TemplateResponse.from(saved));
        }

        List<EditRequestEntity> pending = editRequestService.createPendingTemplateEdits(entity, request, currentUser);

        if (pending.isEmpty()) {
            return ResponseEntity.ok(TemplateResponse.from(entity));
        }

        List<EditRequestResponse> response = pending.stream().map(er -> EditRequestResponse.from(er, false)).toList();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    private void addIfChanged(List<AuditLogService.FieldChange> changes, String fieldName, Object oldValue, Object newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        changes.add(new AuditLogService.FieldChange(fieldName,
            oldValue == null ? null : oldValue.toString(),
            newValue == null ? null : newValue.toString()));
    }
}
