package com.notifyhub.window;

import com.notifyhub.common.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "comm_window")
public class CommWindowEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_id", nullable = false)
    private Long configId;

    @Column(name = "start_window", nullable = false)
    private LocalTime startWindow;

    @Column(name = "end_window", nullable = false)
    private LocalTime endWindow;

    @Column(name = "occurrence", length = 20)
    private String occurrence;

    protected CommWindowEntity() {
        // JPA requires a no-arg constructor
    }

    public CommWindowEntity(Long configId, LocalTime startWindow, LocalTime endWindow, String occurrence) {
        this.configId = configId;
        this.startWindow = startWindow;
        this.endWindow = endWindow;
        this.occurrence = occurrence;
    }

    public Long getId() { return id; }
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public LocalTime getStartWindow() { return startWindow; }
    public void setStartWindow(LocalTime startWindow) { this.startWindow = startWindow; }
    public LocalTime getEndWindow() { return endWindow; }
    public void setEndWindow(LocalTime endWindow) { this.endWindow = endWindow; }
    public String getOccurrence() { return occurrence; }
    public void setOccurrence(String occurrence) { this.occurrence = occurrence; }
}
