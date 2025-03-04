package com.capstone.bgJobs.handlers;

import com.capstone.bgJobs.dto.RunbookEvent;
import com.capstone.bgJobs.service.RunbookExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RunbookEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunbookEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RunbookExecutionService runbookExecutionService;

    public RunbookEventHandler(RunbookExecutionService runbookExecutionService) {
        this.runbookExecutionService = runbookExecutionService;
    }

    public void handle(String message) {
        try {
            RunbookEvent runbookEvent = objectMapper.readValue(message, RunbookEvent.class);
            LOGGER.info("Processing RunbookEvent => tenantId={}, jobId={}", 
                        runbookEvent.getPayload().getTenantId(), 
                        runbookEvent.getPayload().getJobId());
            runbookExecutionService.handleRunbookEvent(runbookEvent);
        } catch (Exception e) {
            LOGGER.error("Error processing RunbookEvent: {}", message, e);
        }
    }
}