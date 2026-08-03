package com.notifyhub.config;

import com.notifyhub.common.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "template_config")
public class TemplateConfigEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "communication_medium", nullable = false, length = 20)
    private String communicationMedium;

    @Column(name = "external_template_id", length = 100)
    private String externalTemplateId;

    @Column(name = "use_active", nullable = false)
    private boolean useActive;

    @Column(name = "contact_flow_id", length = 100)
    private String contactFlowId;

    protected TemplateConfigEntity() {
        // JPA requires a no-arg constructor
    }

    public TemplateConfigEntity(Long templateId, String communicationMedium,
                                 String externalTemplateId, boolean useActive,
                                 String contactFlowId) {
        this.templateId = templateId;
        this.communicationMedium = communicationMedium;
        this.externalTemplateId = externalTemplateId;
        this.useActive = useActive;
        this.contactFlowId = contactFlowId;
    }

    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getCommunicationMedium() { return communicationMedium; }
    public void setCommunicationMedium(String communicationMedium) { this.communicationMedium = communicationMedium; }
    public String getExternalTemplateId() { return externalTemplateId; }
    public void setExternalTemplateId(String externalTemplateId) { this.externalTemplateId = externalTemplateId; }
    public boolean isUseActive() { return useActive; }
    public void setUseActive(boolean useActive) { this.useActive = useActive; }
    public String getContactFlowId() { return contactFlowId; }
    public void setContactFlowId(String contactFlowId) { this.contactFlowId = contactFlowId; }
}
