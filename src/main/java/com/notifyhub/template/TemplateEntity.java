package com.notifyhub.template;

import com.notifyhub.common.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "template")
public class TemplateEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "parent_template_id")
    private Long parentTemplateId;

    @Column(name = "is_parent", nullable = false)
    private boolean isParent;

    @Column(name = "template_name", nullable = false, length = 150)
    private String templateName;

    @Column(name = "template_description", length = 255)
    private String templateDescription;

    @Column(name = "customer_type", length = 20)
    private String customerType;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    protected TemplateEntity() {
        // JPA requires a no-arg constructor
    }

    public TemplateEntity(Long campaignId, Long parentTemplateId, boolean isParent,
                           String templateName, String templateDescription,
                           String customerType, String language, Integer priority,
                           String eventType, String status) {
        this.campaignId = campaignId;
        this.parentTemplateId = parentTemplateId;
        this.isParent = isParent;
        this.templateName = templateName;
        this.templateDescription = templateDescription;
        this.customerType = customerType;
        this.language = language;
        this.priority = priority;
        this.eventType = eventType;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getParentTemplateId() { return parentTemplateId; }
    public void setParentTemplateId(Long parentTemplateId) { this.parentTemplateId = parentTemplateId; }
    public boolean isParent() { return isParent; }
    public void setIsParent(boolean isParent) { this.isParent = isParent; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateDescription() { return templateDescription; }
    public void setTemplateDescription(String templateDescription) { this.templateDescription = templateDescription; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
