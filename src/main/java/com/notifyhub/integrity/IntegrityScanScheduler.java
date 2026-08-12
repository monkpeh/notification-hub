package com.notifyhub.integrity;

import com.notifyhub.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntegrityScanScheduler {

    private static final Logger log = LoggerFactory.getLogger(IntegrityScanScheduler.class);
    private static final List<String> SCHEMAS_TO_SCAN = List.of("public", "dev");

    private final IntegrityCheckService integrityCheckService;

    public IntegrityScanScheduler(IntegrityCheckService integrityCheckService) {
        this.integrityCheckService = integrityCheckService;
    }

    @Scheduled(initialDelay = 0, fixedRate = 1800000)
    public void scheduledScan() {
        for (String schema : SCHEMAS_TO_SCAN) {
            try {
                TenantContext.set(schema);
                List<IntegrityViolationEntity> result = integrityCheckService.runScan();
                log.info("Scheduled integrity scan completed for schema '{}': {} open violation(s)", schema, result.size());
            } finally {
                TenantContext.clear();
            }
        }
    }
}
