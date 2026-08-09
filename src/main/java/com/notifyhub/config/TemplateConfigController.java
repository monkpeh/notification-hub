package com.notifyhub.config;

import com.notifyhub.common.FieldValidationService;
import com.notifyhub.security.RateLimited;
import com.notifyhub.security.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/templates/{templateId}/configs")
public class TemplateConfigController {

    private final TemplateConfigRepository templateConfigRepository;
    private final FieldValidationService fieldValidationService;

    public TemplateConfigController(TemplateConfigRepository templateConfigRepository, FieldValidationService fieldValidationService) {
        this.templateConfigRepository = templateConfigRepository;
        this.fieldValidationService = fieldValidationService;
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "ADMIN", "TEMPLATE_BUILDER", "AI_AGENT"})
    @RateLimited
    public ResponseEntity<TemplateConfigResponse> create(@PathVariable Long templateId,
                                                           @RequestBody CreateTemplateConfigRequest request) {
        fieldValidationService.validateTemplateConfigCreate(request);

        TemplateConfigEntity entity = new TemplateConfigEntity(
            templateId,
            request.communicationMedium(),
            request.externalTemplateId(),
            request.useActive(),
            request.contactFlowId()
        );
        TemplateConfigEntity saved = templateConfigRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(TemplateConfigResponse.from(saved));
    }
}
