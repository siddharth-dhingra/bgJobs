package com.capstone.bgJobs.dto;

public class RunbookDTO {
    private String runbookId;
    private String tenantId;
    private String name;
    private String description;
    private Boolean enabled;
    private String trigger;
    private String filtersJson;
    private String actionsJson;
    // Getters and setters...
    public String getRunbookId() {
        return runbookId;
    }
    public void setRunbookId(String runbookId) {
        this.runbookId = runbookId;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public String getTrigger() {
        return trigger;
    }
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
    public String getFiltersJson() {
        return filtersJson;
    }
    public void setFiltersJson(String filtersJson) {
        this.filtersJson = filtersJson;
    }
    public String getActionsJson() {
        return actionsJson;
    }
    public void setActionsJson(String actionsJson) {
        this.actionsJson = actionsJson;
    }
    
}
