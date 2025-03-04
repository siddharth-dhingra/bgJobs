package com.capstone.bgJobs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

import com.capstone.bgJobs.model.Event;
import com.capstone.bgJobs.model.EventTypes;
import com.capstone.bgJobs.model.RunbookPayload;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RunbookEvent implements Event<RunbookPayload> {
    private String eventId;
    public static final EventTypes TYPE = EventTypes.RUNBOOK;
    private RunbookPayload payload;

    public RunbookEvent() {}

    public RunbookEvent(RunbookPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public EventTypes getType() {
        return TYPE;
    }

    @Override
    public RunbookPayload getPayload() {
        return payload;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setPayload(RunbookPayload payload) {
        this.payload = payload;
    }
}