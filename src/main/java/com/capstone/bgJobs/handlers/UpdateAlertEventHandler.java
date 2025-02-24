package com.capstone.bgJobs.handlers;

import com.capstone.bgJobs.dto.UpdateAlertEvent;
import com.capstone.bgJobs.model.UpdateEvent;
import com.capstone.bgJobs.service.UpdateAlertService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateAlertEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAlertEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UpdateAlertService updateAlertService;

    public UpdateAlertEventHandler(UpdateAlertService updateAlertService) {
        this.updateAlertService = updateAlertService;
    }

    public void handle(String message) {
        try {
            UpdateAlertEvent wrapper = objectMapper.readValue(message, UpdateAlertEvent.class);
            UpdateEvent event = wrapper.getPayload();
            String jobId = event.getJobId();
            
            LOGGER.info("Processing UpdateAlertEvent => eventId={}, payload={}", 
                wrapper.getEventId(), event);
                
            updateAlertService.handleUpdateEvent(event, jobId);
        } catch (Exception e) {
            LOGGER.error("Error handling update alert event: {}", message, e);
        }
    }
}