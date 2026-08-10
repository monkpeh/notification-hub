package com.notifyhub.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/audit-history")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Page<AuditLogResponse> list(@RequestParam(required = false) String tableName,
                                        @RequestParam(required = false) Long templateId,
                                        @RequestParam(required = false) String targetSchema,
                                        @RequestParam(required = false) OffsetDateTime dateFrom,
                                        @RequestParam(required = false) OffsetDateTime dateTo,
                                        @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return auditLogService.findFiltered(tableName, templateId, targetSchema, dateFrom, dateTo, pageable)
            .map(AuditLogResponse::from);
    }
}
