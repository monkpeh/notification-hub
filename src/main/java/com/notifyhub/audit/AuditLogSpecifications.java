package com.notifyhub.audit;

import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class AuditLogSpecifications {

    private AuditLogSpecifications() {
        // static helpers only
    }

    public static Specification<AuditLogEntity> hasTableName(String tableName) {
        return (root, query, cb) -> tableName == null ? null : cb.equal(root.get("tableName"), tableName);
    }

    public static Specification<AuditLogEntity> hasRecordId(Long recordId) {
        return (root, query, cb) -> recordId == null ? null : cb.equal(root.get("recordId"), recordId);
    }

    public static Specification<AuditLogEntity> hasTargetSchema(String targetSchema) {
        return (root, query, cb) -> targetSchema == null ? null : cb.equal(root.get("targetSchema"), targetSchema);
    }

    public static Specification<AuditLogEntity> changedAfter(OffsetDateTime dateFrom) {
        return (root, query, cb) -> dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("changedAt"), dateFrom);
    }

    public static Specification<AuditLogEntity> changedBefore(OffsetDateTime dateTo) {
        return (root, query, cb) -> dateTo == null ? null : cb.lessThanOrEqualTo(root.get("changedAt"), dateTo);
    }
}
