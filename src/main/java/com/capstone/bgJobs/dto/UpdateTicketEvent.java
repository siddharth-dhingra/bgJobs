package com.capstone.bgJobs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

import com.capstone.bgJobs.model.Event;
import com.capstone.bgJobs.model.EventTypes;
import com.capstone.bgJobs.model.UpdateTicketPayload;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTicketEvent implements Event<UpdateTicketPayload> {
    
    private String eventId;
    public static EventTypes TYPE = EventTypes.TICKETING_UPDATE;
    private UpdateTicketPayload payload;

    public UpdateTicketEvent(String eventId, UpdateTicketPayload payload) {
        this.eventId = (eventId == null || eventId.isEmpty()) ? UUID.randomUUID().toString() : eventId;
        this.payload = payload;
    }

    public UpdateTicketEvent() {}

    @Override
    public UpdateTicketPayload getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public EventTypes getType() {
        return TYPE;
    }

    public void setPayload(UpdateTicketPayload payload) {
        this.payload = payload;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public static void setTYPE(EventTypes tYPE) {
        TYPE = tYPE;
    }
}