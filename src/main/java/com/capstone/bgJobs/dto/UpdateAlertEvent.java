package com.capstone.bgJobs.dto;

import java.util.UUID;

import com.capstone.bgJobs.model.Event;
import com.capstone.bgJobs.model.EventTypes;
import com.capstone.bgJobs.model.UpdateEvent;


public class UpdateAlertEvent implements Event<UpdateEvent> {
    
    private String eventId;
    public static EventTypes TYPE = EventTypes.SCAN_PARSE;
    private UpdateEvent payload;

    public UpdateAlertEvent() {}

    public UpdateAlertEvent(String eventId, UpdateEvent payload) {
        this.payload = payload;
        this.eventId = (eventId == null || eventId.isEmpty()) ? UUID.randomUUID().toString() : eventId;
    }

    public static EventTypes getTYPE() {
        return TYPE;
    }

    public static void setTYPE(EventTypes tYPE) {
        TYPE = tYPE;
    }

    public void setPayload(UpdateEvent payload) {
        this.payload = payload;
    }

    @Override
    public EventTypes getType() {
        return TYPE;
    }

    @Override
    public UpdateEvent getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}