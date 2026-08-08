package com.notifyhub.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notifyhub.common.FieldValidationService;
import com.notifyhub.security.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateRepository templateRepository;
    private final FieldValidationService fieldValidationService;
    private final ObjectMapper objectMapper;

    public TemplateController(TemplateRepository templateRepository, FieldValidationService fieldValidationService, ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.fieldValidationService = fieldValidationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "ADMIN", "TEMPLATE_BUILDER", "AI_AGENT"})
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
    public TemplateResponse update(@PathVariable Long id, @RequestBody Map<String, Object> rawBody) {
        fieldValidationService.validateNoImmutableFieldsInUpdate(rawBody);

        UpdateTemplateRequest request = objectMapper.convertValue(rawBody, UpdateTemplateRequest.class);
        fieldValidationService.validateTemplateUpdate(request);

        TemplateEntity entity = templateRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));

        entity.setTemplateName(request.templateName());
        entity.setTemplateDescription(request.templateDescription());
        entity.setCustomerType(request.customerType());
        entity.setLanguage(request.language());
        entity.setPriority(request.priority());
        entity.setStatus(request.status());

        TemplateEntity saved = templateRepository.save(entity);
        return TemplateResponse.from(saved);
    }
}
