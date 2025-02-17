package com.capstone.bgJobs.dto;

import java.util.UUID;

import com.capstone.bgJobs.model.Acknowledgement;
import com.capstone.bgJobs.model.AcknowledgementPayload;

public class UpdateAcknowledgement implements Acknowledgement<AcknowledgementPayload> {
    
    private String acknowledgementId;
    private AcknowledgementPayload payload;

    public UpdateAcknowledgement() {}   

    public UpdateAcknowledgement(String acknowledgementId, AcknowledgementPayload payload) {
        this.acknowledgementId = (acknowledgementId == null || acknowledgementId.isEmpty()) ? UUID.randomUUID().toString() : acknowledgementId;
        this.payload = payload;
    }

    public void setAcknowledgementId(String acknowledgementId) {
        this.acknowledgementId = acknowledgementId;
    }

    public void setPayload(AcknowledgementPayload payload) {
        this.payload = payload;
    }

    @Override
    public String getAcknowledgementId() {
        return acknowledgementId;
    }

    @Override
    public AcknowledgementPayload getPayload() {
        return payload;
    }
}