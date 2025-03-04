package com.capstone.bgJobs.consumer;

import com.capstone.bgJobs.handlers.CreateTicketEventHandler;
import com.capstone.bgJobs.handlers.RunbookEventHandler;
import com.capstone.bgJobs.handlers.UpdateAlertEventHandler;
import com.capstone.bgJobs.handlers.UpdateTicketEventHandler;
import com.capstone.bgJobs.model.EventTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UnifiedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnifiedConsumer.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CreateTicketEventHandler createTicketEventHandler;
    private final UpdateAlertEventHandler updateAlertEventHandler;
    private final UpdateTicketEventHandler updateTicketEventHandler;
    private final RunbookEventHandler runbookEventHandler;

    public UnifiedConsumer(
            CreateTicketEventHandler createTicketEventHandler,
            UpdateAlertEventHandler updateAlertEventHandler,
            UpdateTicketEventHandler updateTicketEventHandler, RunbookEventHandler runbookEventHandler) {
        this.createTicketEventHandler = createTicketEventHandler;
        this.updateAlertEventHandler = updateAlertEventHandler;
        this.updateTicketEventHandler = updateTicketEventHandler;
        this.runbookEventHandler = runbookEventHandler;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.jfc-bgjobs}",
        groupId = "bgjobs-group-unified",
        containerFactory = "unifiedListenerContainerFactory"
    )
    public void onMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String typeString = root.get("type").asText(); 
            EventTypes eventType = EventTypes.valueOf(typeString);
            
            LOGGER.info("Received event of type: {}", eventType);
            
            switch (eventType) {
                case UPDATE_FINDING:
                    updateAlertEventHandler.handle(message);
                    break;
                case TICKETING_CREATE:
                    createTicketEventHandler.handle(message);
                    break;
                case TICKETING_UPDATE:
                    updateTicketEventHandler.handle(message);
                    break;
                case RUNBOOK:
                    runbookEventHandler.handle(message);
                    break;
                default:
                    LOGGER.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing message: {}", message, e);
        }
    }
}