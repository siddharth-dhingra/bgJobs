package com.capstone.bgJobs.dto;

import java.util.UUID;

import com.capstone.bgJobs.model.Acknowledgement;
import com.capstone.bgJobs.model.AcknowledgementEvent;

public class UpdateAcknowledgement implements Acknowledgement<AcknowledgementEvent> {
    
    private String acknowledgementId;
    private AcknowledgementEvent payload;

    public UpdateAcknowledgement() {}   

    public UpdateAcknowledgement(String acknowledgementId, AcknowledgementEvent payload) {
        this.acknowledgementId = (acknowledgementId == null || acknowledgementId.isEmpty()) ? UUID.randomUUID().toString() : acknowledgementId;
        this.payload = payload;
    }

    public void setAcknowledgementId(String acknowledgementId) {
        this.acknowledgementId = acknowledgementId;
    }

    public void setPayload(AcknowledgementEvent payload) {
        this.payload = payload;
    }

    @Override
    public String getAcknowledgementId() {
        return acknowledgementId;
    }

    @Override
    public AcknowledgementEvent getPayload() {
        return payload;
    }
}