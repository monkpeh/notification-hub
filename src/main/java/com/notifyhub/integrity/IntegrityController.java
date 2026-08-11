package com.notifyhub.integrity;

import com.notifyhub.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/integrity")
public class IntegrityController {

    private final IntegrityCheckService integrityCheckService;

    public IntegrityController(IntegrityCheckService integrityCheckService) {
        this.integrityCheckService = integrityCheckService;
    }

    @PostMapping("/scan")
    @RequireRole({"SUPER_ADMIN", "ADMIN"})
    public List<IntegrityViolationResponse> scan() {
        return integrityCheckService.runScan().stream().map(IntegrityViolationResponse::from).toList();
    }

    @GetMapping("/violations")
    public List<IntegrityViolationResponse> list(@RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String checkType,
                                                   @RequestParam(required = false) String targetSchema) {
        return integrityCheckService.findFiltered(status, checkType, targetSchema).stream()
            .map(IntegrityViolationResponse::from)
            .toList();
    }
}
