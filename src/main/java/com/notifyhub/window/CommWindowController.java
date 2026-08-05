package com.notifyhub.window;

import com.notifyhub.common.FieldValidationService;
import com.notifyhub.security.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/configs/{configId}/windows")
public class CommWindowController {

    private final CommWindowRepository commWindowRepository;
    private final FieldValidationService fieldValidationService;

    public CommWindowController(CommWindowRepository commWindowRepository, FieldValidationService fieldValidationService) {
        this.commWindowRepository = commWindowRepository;
        this.fieldValidationService = fieldValidationService;
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "ADMIN", "TEMPLATE_BUILDER", "AI_AGENT"})
    public ResponseEntity<CommWindowResponse> create(@PathVariable Long configId,
                                                       @RequestBody CreateCommWindowRequest request) {
        fieldValidationService.validateCommWindowCreate(request);

        CommWindowEntity entity = new CommWindowEntity(
            configId,
            request.startWindow(),
            request.endWindow(),
            request.occurrence()
        );
        CommWindowEntity saved = commWindowRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommWindowResponse.from(saved));
    }
}
