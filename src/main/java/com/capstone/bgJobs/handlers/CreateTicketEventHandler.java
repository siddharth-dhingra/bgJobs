package com.capstone.bgJobs.handlers;

import com.capstone.bgJobs.dto.CreateTicketEvent;
import com.capstone.bgJobs.model.CreateTicketPayload;
import com.capstone.bgJobs.service.CreateTicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CreateTicketEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTicketEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CreateTicketService createTicketService;

    public CreateTicketEventHandler(CreateTicketService createTicketService) {
        this.createTicketService = createTicketService;
    }

    public void handle(String message) {
        try {
            CreateTicketEvent wrapper = objectMapper.readValue(message, CreateTicketEvent.class);
            CreateTicketPayload event = wrapper.getPayload();
            String jobId = event.getJobId();
            
            LOGGER.info("Processing CreateTicketEvent => eventId={}, payload={}", 
                wrapper.getEventId(), event);
                
            createTicketService.handleCreateTicketEvent(
                event.getTenantId(), 
                event.getFindingId(), 
                event.getSummary(), 
                event.getDescription(), 
                jobId
            );
        } catch (Exception e) {
            LOGGER.error("Error handling create ticket event: {}", message, e);
        }
    }
}