package com.notifyhub.campaign;

import com.notifyhub.common.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "campaign")
public class CampaignEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "business_purpose", length = 255)
    private String businessPurpose;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "owner", length = 100)
    private String owner;

    protected CampaignEntity() {
        // JPA requires a no-arg constructor
    }

    public CampaignEntity(String name, String businessPurpose, String status, String owner) {
        this.name = name;
        this.businessPurpose = businessPurpose;
        this.status = status;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBusinessPurpose() { return businessPurpose; }
    public void setBusinessPurpose(String businessPurpose) { this.businessPurpose = businessPurpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}
