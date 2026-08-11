package com.notifyhub.integrity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "integrity_violation")
public class IntegrityViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_type", nullable = false, length = 50)
    private String checkType;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "details", nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column(name = "target_schema", nullable = false, length = 20)
    private String targetSchema;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "first_detected_at", nullable = false, updatable = false)
    private OffsetDateTime firstDetectedAt;

    @Column(name = "last_detected_at", nullable = false)
    private OffsetDateTime lastDetectedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    protected IntegrityViolationEntity() {
        // JPA requires a no-arg constructor
    }

    public IntegrityViolationEntity(String checkType, String tableName, Long recordId,
                                     String severity, String details, String targetSchema) {
        this.checkType = checkType;
        this.tableName = tableName;
        this.recordId = recordId;
        this.severity = severity;
        this.details = details;
        this.targetSchema = targetSchema;
        this.status = "OPEN";
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.firstDetectedAt = now;
        this.lastDetectedAt = now;
    }

    public Long getId() { return id; }
    public String getCheckType() { return checkType; }
    public String getTableName() { return tableName; }
    public Long getRecordId() { return recordId; }
    public String getSeverity() { return severity; }
    public String getDetails() { return details; }
    public String getTargetSchema() { return targetSchema; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getFirstDetectedAt() { return firstDetectedAt; }
    public OffsetDateTime getLastDetectedAt() { return lastDetectedAt; }
    public void setLastDetectedAt(OffsetDateTime lastDetectedAt) { this.lastDetectedAt = lastDetectedAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
