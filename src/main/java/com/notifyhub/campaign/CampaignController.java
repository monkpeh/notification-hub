package com.notifyhub.campaign;

import com.notifyhub.security.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignRepository campaignRepository;

    public CampaignController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "ADMIN"})
    public ResponseEntity<CampaignResponse> create(@RequestBody CreateCampaignRequest request) {
        CampaignEntity entity = new CampaignEntity(
            request.name(),
            request.businessPurpose(),
            request.status(),
            request.owner()
        );
        CampaignEntity saved = campaignRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(CampaignResponse.from(saved));
    }

    @GetMapping("/{id}")
    public CampaignResponse getById(@PathVariable Long id) {
        CampaignEntity entity = campaignRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found: " + id));
        return CampaignResponse.from(entity);
    }
}
