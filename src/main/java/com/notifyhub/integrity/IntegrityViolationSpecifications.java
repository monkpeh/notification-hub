package com.notifyhub.integrity;

import org.springframework.data.jpa.domain.Specification;

public class IntegrityViolationSpecifications {

    private IntegrityViolationSpecifications() {
        // static helpers only
    }

    public static Specification<IntegrityViolationEntity> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<IntegrityViolationEntity> hasCheckType(String checkType) {
        return (root, query, cb) -> checkType == null ? null : cb.equal(root.get("checkType"), checkType);
    }

    public static Specification<IntegrityViolationEntity> hasTargetSchema(String targetSchema) {
        return (root, query, cb) -> targetSchema == null ? null : cb.equal(root.get("targetSchema"), targetSchema);
    }
}
