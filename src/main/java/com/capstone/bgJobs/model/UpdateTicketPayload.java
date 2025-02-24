package com.capstone.bgJobs.model;

public class UpdateTicketPayload {
    
    private String tenantId;
    private String ticketId;
    private String jobId;
    
    public UpdateTicketPayload() {
    }

    public UpdateTicketPayload(String tenantId, String ticketId) {
        this.tenantId = tenantId;
        this.ticketId = ticketId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}