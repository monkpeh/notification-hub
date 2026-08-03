package com.notifyhub.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/templates/{templateId}/configs")
public class TemplateConfigController {

    private final TemplateConfigRepository templateConfigRepository;

    public TemplateConfigController(TemplateConfigRepository templateConfigRepository) {
        this.templateConfigRepository = templateConfigRepository;
    }

    @PostMapping
    public ResponseEntity<TemplateConfigResponse> create(@PathVariable Long templateId,
                                                           @RequestBody CreateTemplateConfigRequest request) {
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
