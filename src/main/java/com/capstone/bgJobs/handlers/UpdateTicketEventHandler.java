package com.capstone.bgJobs.handlers;

import com.capstone.bgJobs.dto.UpdateTicketEvent;
import com.capstone.bgJobs.model.UpdateTicketPayload;
import com.capstone.bgJobs.service.UpdateTicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateTicketEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTicketEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UpdateTicketService updateTicketService;

    public UpdateTicketEventHandler(UpdateTicketService updateTicketService) {
        this.updateTicketService = updateTicketService;
    }

    public void handle(String message) {
        try {
            UpdateTicketEvent wrapper = objectMapper.readValue(message, UpdateTicketEvent.class);
            UpdateTicketPayload event = wrapper.getPayload();
            String jobId = event.getJobId();
            
            LOGGER.info("Processing UpdateTicketEvent => eventId={}, payload={}", 
                wrapper.getEventId(), event);
                
            updateTicketService.updateTicketStatusToDone(
                event.getTenantId(), 
                event.getTicketId(), 
                jobId
            );
        } catch (Exception e) {
            LOGGER.error("Error handling update ticket event: {}", message, e);
        }
    }
}