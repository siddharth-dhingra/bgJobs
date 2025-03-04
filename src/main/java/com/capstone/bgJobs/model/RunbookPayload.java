package com.capstone.bgJobs.model;

import java.util.List;

public class RunbookPayload {
    private String tenantId;
    private String jobId;
    private List<String> findingIds;
    private TriggerType triggerType;

    public RunbookPayload() {}

    public RunbookPayload(String tenantId, String jobId, List<String> findingIds, TriggerType triggerType) {
        this.tenantId = tenantId;
        this.jobId = jobId;
        this.findingIds = findingIds;
        this.triggerType = triggerType;
    }

    // Getters and setters

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public List<String> getFindingIds() { return findingIds; }
    public void setFindingIds(List<String> findingIds) { this.findingIds = findingIds; }

    public TriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(TriggerType triggerType) { this.triggerType = triggerType; }
}