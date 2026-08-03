package com.notifyhub.editrequest;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "edit_request")
public class EditRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "target_schema", nullable = false, length = 20)
    private String targetSchema;

    @Column(name = "approved_with_override", nullable = false)
    private boolean approvedWithOverride;

    protected EditRequestEntity() {
        // JPA requires a no-arg constructor
    }

    public EditRequestEntity(String tableName, Long recordId, String fieldName,
                              String oldValue, String newValue, Long requestedBy,
                              String reason, String targetSchema) {
        this.tableName = tableName;
        this.recordId = recordId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.requestedBy = requestedBy;
        this.reason = reason;
        this.targetSchema = targetSchema;
        this.status = "PENDING";
        this.approvedWithOverride = false;
    }

    @PrePersist
    protected void onCreate() {
        this.requestedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getTableName() { return tableName; }
    public Long getRecordId() { return recordId; }
    public String getFieldName() { return fieldName; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public Long getRequestedBy() { return requestedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public Long getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Long resolvedBy) { this.resolvedBy = resolvedBy; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getTargetSchema() { return targetSchema; }
    public boolean isApprovedWithOverride() { return approvedWithOverride; }
    public void setApprovedWithOverride(boolean approvedWithOverride) { this.approvedWithOverride = approvedWithOverride; }
}
