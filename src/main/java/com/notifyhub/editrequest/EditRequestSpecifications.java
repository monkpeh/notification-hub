package com.notifyhub.editrequest;

import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class EditRequestSpecifications {

    private EditRequestSpecifications() {
        // static helpers only
    }

    public static Specification<EditRequestEntity> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<EditRequestEntity> hasRecordId(Long recordId) {
        return (root, query, cb) -> recordId == null ? null : cb.equal(root.get("recordId"), recordId);
    }

    public static Specification<EditRequestEntity> requestedAfter(OffsetDateTime dateFrom) {
        return (root, query, cb) -> dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("requestedAt"), dateFrom);
    }

    public static Specification<EditRequestEntity> requestedBefore(OffsetDateTime dateTo) {
        return (root, query, cb) -> dateTo == null ? null : cb.lessThanOrEqualTo(root.get("requestedAt"), dateTo);
    }
}
