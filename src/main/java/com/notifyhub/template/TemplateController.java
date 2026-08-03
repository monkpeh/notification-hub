package com.notifyhub.template;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateRepository templateRepository;

    public TemplateController(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostMapping
    public ResponseEntity<TemplateResponse> create(@RequestBody CreateTemplateRequest request) {
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
    public TemplateResponse update(@PathVariable Long id, @RequestBody UpdateTemplateRequest request) {
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
