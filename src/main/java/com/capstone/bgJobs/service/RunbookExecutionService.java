package com.capstone.bgJobs.service;

import com.capstone.bgJobs.dto.RunbookDTO;
import com.capstone.bgJobs.dto.RunbookEvent;
import com.capstone.bgJobs.dto.UpdateAcknowledgement;
import com.capstone.bgJobs.model.AcknowledgementPayload;
import com.capstone.bgJobs.model.AcknowledgementStatus;
import com.capstone.bgJobs.model.Finding;
import com.capstone.bgJobs.model.RunbookAction;
import com.capstone.bgJobs.model.RunbookFilter;
import com.capstone.bgJobs.model.UpdateEvent;
import com.capstone.bgJobs.producer.AcknowledgementProducer;
import com.capstone.bgJobs.model.RunbookPayload;
import com.capstone.bgJobs.model.ToolType;
import com.capstone.bgJobs.model.TriggerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
public class RunbookExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunbookExecutionService.class);

    private final ObjectMapper objectMapper;
    private final ElasticsearchService elasticsearchService;
    private final UpdateAlertService updateAlertService;
    private final CreateTicketService createTicketService;
    private final RunbookConfigurationService runbookConfigurationService;
    private final AcknowledgementProducer acknowledgementProducer;

    public RunbookExecutionService(ObjectMapper objectMapper,
                                   ElasticsearchService elasticsearchService,
                                   UpdateAlertService updateAlertService,
                                   CreateTicketService createTicketService,
                                   RunbookConfigurationService runbookConfigurationService, AcknowledgementProducer acknowledgementProducer) {
        this.objectMapper = objectMapper;
        this.elasticsearchService = elasticsearchService;
        this.updateAlertService = updateAlertService;
        this.createTicketService = createTicketService;
        this.runbookConfigurationService = runbookConfigurationService;
        this.acknowledgementProducer = acknowledgementProducer;
    }

    /**
     * Handles a RUNBOOK event.
     * Steps:
     * 1. Deserialize the event payload.
     * 2. Query Auth Server for runbooks matching (tenantId, triggerType).
     * 3. Retrieve Finding objects from Elasticsearch based on the provided IDs.
     * 4. For each runbook, apply filter criteria and execute configured actions.
     */
    public void handleRunbookEvent(RunbookEvent runbookEvent) throws Exception {
        // Deserialize the RUNBOOK event into a RunbookEvent object.
        RunbookPayload payload = runbookEvent.getPayload();
        String tenantId = payload.getTenantId();
        TriggerType triggerType = payload.getTriggerType();
        List<String> findingIds = payload.getFindingIds();
        String jobId = payload.getJobId();

        LOGGER.info("Processing RUNBOOK event: tenantId={}, triggerType={}, jobId={}, #findings={}",
                tenantId, triggerType, jobId, (findingIds != null ? findingIds.size() : 0));

        // 1. Query runbooks from Auth Server based on tenantId and triggerType.
        List<RunbookDTO> runbooks = runbookConfigurationService.getRunbooksByTenantAndTrigger(tenantId, triggerType.toString());
        if (runbooks == null || runbooks.isEmpty()) {
            sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);
            LOGGER.info("No runbooks found for tenant={} and triggerType={}", tenantId, triggerType);
            return;
        }

        // 2. Retrieve Finding objects from Elasticsearch for each provided findingId.
        List<Finding> findings = new ArrayList<>();
        for (String findingId : findingIds) {
            Finding f = elasticsearchService.getFindingById(tenantId, findingId);
            if (f != null) {
                findings.add(f);
            }
        }
        LOGGER.info("Retrieved {} findings from Elasticsearch.", findings.size());

        // 3. For each runbook, apply filters and execute actions.
        for (RunbookDTO runbook : runbooks) {

            if (runbook.getEnabled() == null || !runbook.getEnabled()) {
                LOGGER.info("Skipping runbook {} as it is disabled.", runbook.getRunbookId());
                continue;
            }

            if (runbook.getTrigger() == null || !runbook.getTrigger().equalsIgnoreCase(triggerType.toString())) {
                continue;
            }

            // Deserialize filters and actions JSON (if provided).
            Map<String, Object> filters = null;
            Map<String, Object> actions = null;
            if (runbook.getFiltersJson() != null && !runbook.getFiltersJson().isEmpty()) {
                filters = objectMapper.readValue(runbook.getFiltersJson(), Map.class);
            }
            if (runbook.getActionsJson() != null && !runbook.getActionsJson().isEmpty()) {
                actions = objectMapper.readValue(runbook.getActionsJson(), Map.class);
            }

            // Apply filters to the findings.
            List<Finding> matchedFindings = filterFindings(findings, filters);
            LOGGER.info("Runbook {}: {} findings matched filters", runbook.getRunbookId(), matchedFindings.size());
            if(matchedFindings.size()==0){
                sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
            }

            // For each matching finding, execute configured actions.
            for (Finding finding : matchedFindings) {
                
                // If create ticket action is configured:
                if (actions != null && Boolean.TRUE.equals(actions.get(RunbookAction.CREATE_TICKET.getKey()))) {
                    String summary = safeSubstring(finding.getTitle(), 200);
                    String description = safeSubstring(finding.getDescription(), 200);
                    createTicketService.handleCreateTicketEvent(tenantId, finding.getId(), summary, description, jobId);
                }

                // If update finding action is configured:
                if (actions != null && actions.containsKey(RunbookAction.UPDATE_FINDING.getKey())) {
                    Object updateActionObj = actions.get("update_finding");
                    if (updateActionObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> updateAction = (Map<String, String>) updateActionObj;
                        String toState = updateAction.get("to");
                        updateFindingState(finding, toState, jobId, tenantId);
                    }
                }
            }
            sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);
        }
    }

    private List<Finding> filterFindings(List<Finding> findings, Map<String, Object> filters) {
        List<Finding> matched = new ArrayList<>();
        if (filters == null || filters.isEmpty()) {
            return findings; // No filters; all findings match.
        }
        for (Finding f : findings) {
            boolean match = true;
            // Example: filter by severity.
            if (filters.containsKey(RunbookFilter.SEVERITY.getKey()) && filters.get(RunbookFilter.SEVERITY.getKey()) != null &&
            !filters.get(RunbookFilter.SEVERITY.getKey()).toString().trim().isEmpty()) {
                String requiredSeverity = filters.get("severity").toString().toLowerCase();
                if (!f.getSeverity().toString().toLowerCase().equals(requiredSeverity)) {
                    match = false;
                }
            }
            // Example: filter by state.
            if (filters.containsKey(RunbookFilter.STATE.getKey()) && filters.get(RunbookFilter.STATE.getKey()) != null &&
            !filters.get(RunbookFilter.STATE.getKey()).toString().trim().isEmpty()) {
                String requiredState = filters.get("state").toString().toLowerCase();
                if (!f.getStatus().toString().toLowerCase().equals(requiredState)) {
                    match = false;
                }
            }
            if (match) {
                matched.add(f);
            }
        }
        return matched;
    }

    private void updateFindingState(Finding finding, String newAppState, String jobId, String tenantId) {

        UpdateEvent event = new UpdateEvent();
        event.setTenantId(tenantId);
        event.setToolType(finding.getToolType());
        
        int alertNumber = 0;
        if (finding.getAdditionalData() != null && finding.getAdditionalData().containsKey("number")) {
            try {
                alertNumber = Integer.parseInt(finding.getAdditionalData().get("number").toString());
            } catch (NumberFormatException e) {
                // Log the error and handle as needed.
                System.err.println("Error parsing alert number from additionalData: " + e.getMessage());
            }
        }
        event.setAlertNumber(alertNumber);
        
        event.setNewState(mapAppStateToGitHubState(newAppState, finding.getToolType()));
        event.setReason(mapAppStateToReason(newAppState, finding.getToolType()));
        event.setJobId(jobId);
        
        updateAlertService.handleUpdateEvent(event, jobId);
    }

    private String mapAppStateToGitHubState(String appState, ToolType toolType) {
        if (appState == null || toolType == null) {
            return "open";
        }
        String state = appState.toUpperCase();
        switch (toolType) {
            case DEPENDABOT:
                if ("OPEN".equals(state)) {
                    return "open";
                } else if ("FALSE_POSITIVE".equals(state) || "SUPPRESSED".equals(state)) {
                    return "dismissed";
                }
                break;
            case CODESCAN:
                if ("OPEN".equals(state)) {
                    return "open";
                } else if ("FALSE_POSITIVE".equals(state) || "SUPPRESSED".equals(state)) {
                    return "dismissed";
                } else if ("FIXED".equals(state)) {
                    return "resolved";
                }
                break;
            case SECRETSCAN:
                if ("OPEN".equals(state)) {
                    return "open";
                } else if ("FALSE_POSITIVE".equals(state) || "SUPPRESSED".equals(state) || "FIXED".equals(state)) {
                    return "resolved";
                }
                break;
            default:
                break;
        }
        return "open"; // default
    }


    private String mapAppStateToReason(String appState, ToolType toolType) {
        if (appState == null || toolType == null) {
            return "";
        }
        String state = appState.toUpperCase();
        switch (toolType) {
            case DEPENDABOT:
                if ("FALSE_POSITIVE".equals(state)) {
                    return "inaccurate";
                }
                return "fix_started";
            case CODESCAN:
                if ("FALSE_POSITIVE".equals(state)) {
                    return "false positive";
                }
                return "used in tests";
            case SECRETSCAN:
                if ("FALSE_POSITIVE".equals(state)) {
                    return "false_positive";
                }
                return "wont_fix";
            default:
                break;
        }
        return "";
    }

    private String safeSubstring(String input, int maxLen) {
        if (input == null) return "";
        return input.length() <= maxLen ? input : input.substring(0, maxLen);
    }

    private void sendAcknowledgement(String jobId, AcknowledgementStatus status) {
        AcknowledgementPayload ackEvent = new AcknowledgementPayload(jobId);
        ackEvent.setStatus(status);
        UpdateAcknowledgement ack = new UpdateAcknowledgement(UUID.randomUUID().toString(), ackEvent);
        acknowledgementProducer.sendAcknowledgement(ack);
        LOGGER.info("Sent acknowledgement for jobId {}: {}", jobId, ack);
    }
}
