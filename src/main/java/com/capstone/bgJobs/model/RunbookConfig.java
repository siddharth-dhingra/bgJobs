package com.capstone.bgJobs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "runbook_config")
public class RunbookConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-increment primary key

    @Column(name = "runbook_id", length = 36, nullable = false)
    private String runbookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger")
    private TriggerType trigger;

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "actions_json", columnDefinition = "TEXT")
    private String actionsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public RunbookConfig() {}

    public RunbookConfig(String runbookId) {
        this.runbookId = runbookId;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getRunbookId() {
        return runbookId;
    }

    public TriggerType getTrigger() {
        return trigger;
    }

    public String getFiltersJson() {
        return filtersJson;
    }

    public String getActionsJson() {
        return actionsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTrigger(TriggerType trigger) {
        this.trigger = trigger;
    }

    public void setFiltersJson(String filtersJson) {
        this.filtersJson = filtersJson;
    }

    public void setActionsJson(String actionsJson) {
        this.actionsJson = actionsJson;
    }
}