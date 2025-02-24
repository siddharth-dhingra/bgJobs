package com.capstone.bgJobs.model;

public class CreateTicketPayload {
    
    private String tenantId;
    private String findingId;
    private String summary;
    private String description;
    private String jobId;
    
    public CreateTicketPayload() {
    }

    public CreateTicketPayload(String tenantId, String findingId, String summary, String description) {
        this.tenantId = tenantId;
        this.findingId = findingId;
        this.summary = summary;
        this.description = description;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getFindingId() {
        return findingId;
    }

    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}