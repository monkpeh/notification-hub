package com.notifyhub.audit;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

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

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "edit_request_id")
    private Long editRequestId;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    @Column(name = "change_batch_id", nullable = false)
    private UUID changeBatchId;

    @Column(name = "target_schema", nullable = false, length = 20)
    private String targetSchema;

    protected AuditLogEntity() {
        // JPA requires a no-arg constructor
    }

    public AuditLogEntity(String tableName, Long recordId, String fieldName,
                           String oldValue, String newValue, Long actorId,
                           String reason, Long editRequestId, UUID changeBatchId,
                           String targetSchema) {
        this.tableName = tableName;
        this.recordId = recordId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.actorId = actorId;
        this.reason = reason;
        this.editRequestId = editRequestId;
        this.changeBatchId = changeBatchId;
        this.targetSchema = targetSchema;
    }

    @PrePersist
    protected void onCreate() {
        this.changedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getTableName() { return tableName; }
    public Long getRecordId() { return recordId; }
    public String getFieldName() { return fieldName; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public Long getActorId() { return actorId; }
    public String getReason() { return reason; }
    public Long getEditRequestId() { return editRequestId; }
    public OffsetDateTime getChangedAt() { return changedAt; }
    public UUID getChangeBatchId() { return changeBatchId; }
    public String getTargetSchema() { return targetSchema; }
}
