package com.notifyhub.window;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/configs/{configId}/windows")
public class CommWindowController {

    private final CommWindowRepository commWindowRepository;

    public CommWindowController(CommWindowRepository commWindowRepository) {
        this.commWindowRepository = commWindowRepository;
    }

    @PostMapping
    public ResponseEntity<CommWindowResponse> create(@PathVariable Long configId,
                                                       @RequestBody CreateCommWindowRequest request) {
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
