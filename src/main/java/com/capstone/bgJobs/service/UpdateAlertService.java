package com.capstone.bgJobs.service;

import com.capstone.bgJobs.model.UpdateEvent;
import com.capstone.bgJobs.producer.AcknowledgementProducer;
import com.capstone.bgJobs.repository.TenantRepository;
import com.capstone.bgJobs.repository.TenantTicketRepository;
import com.capstone.bgJobs.dto.UpdateAcknowledgement;
import com.capstone.bgJobs.model.AcknowledgementPayload;
import com.capstone.bgJobs.model.AcknowledgementStatus;
import com.capstone.bgJobs.model.Finding;
import com.capstone.bgJobs.model.Status;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.model.TenantTicket;
import com.capstone.bgJobs.utils.BgJobsUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateAlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAlertService.class);

    private final TenantRepository tenantRepository;
    private final TenantTicketRepository tenantTicketRepository;
    private final GitHubAlertUpdateService gitHubAlertUpdateService;
    private final UpdateTicketService updateTicketService;
    private final ElasticsearchService elasticsearchService;
    private final AcknowledgementProducer acknowledgementProducer;

    public UpdateAlertService(TenantRepository tenantRepository,
                              GitHubAlertUpdateService gitHubAlertUpdateService,
                              ElasticsearchService elasticsearchService,
                              AcknowledgementProducer acknowledgementProducer,
                              TenantTicketRepository tenantTicketRepository, 
                              UpdateTicketService updateTicketService) {
        this.tenantRepository = tenantRepository;
        this.gitHubAlertUpdateService = gitHubAlertUpdateService;
        this.elasticsearchService = elasticsearchService;
        this.acknowledgementProducer = acknowledgementProducer;
        this.tenantTicketRepository = tenantTicketRepository;
        this.updateTicketService = updateTicketService;
    }

    /**
     * Handles an UpdateEvent by:
     * 1) Patching the alert status on GitHub.
     * 2) Mapping the new state and reason into an ES status using ParserUtils.mapStatus.
     * 3) Updating the alert document in Elasticsearch.
     */
    public void handleUpdateEvent(UpdateEvent event, String jobId) {
        String tenantId = event.getTenantId();
        Optional<Tenant> tenantOpt = tenantRepository.findByTenantId(tenantId);
        if (tenantOpt.isEmpty()) {
            LOGGER.error("No tenant found for tenantId={}", tenantId);
        }
        Tenant tenant = tenantOpt.get();
        try {
            // Step 1: Patch the alert on GitHub.
            gitHubAlertUpdateService.updateAlert(event, tenant.getPat(), tenant.getOwner(), tenant.getRepo());
            LOGGER.info("Successfully updated alert in GitHub: {}", event);

            // Step 2: Map new state and reason into an ES status.
            Status esStatus = BgJobsUtils.mapStatus(event.getNewState(), event.getReason(), event.getToolType());
            String newStatus = esStatus.name();  // e.g., "OPEN", "SUPPRESSED", etc.

            // Step 3: Update the document in Elasticsearch.
            String esIndex = tenant.getEsIndex();
            elasticsearchService.updateAlertStatus(esIndex, event.getAlertNumber(), event.getToolType(), newStatus);
            LOGGER.info("Updated document in Elasticsearch: index={}, alertNumber={}, newStatus={}",
                    esIndex, event.getAlertNumber(), newStatus);

            if (event.getNewState() != null &&
                (event.getNewState().equalsIgnoreCase("dismissed") || event.getNewState().equalsIgnoreCase("resolved"))) {
                // Retrieve the Finding document by alert number and tool type.
                try {
                    // Note: getFindingByAlertNumber uses alertNumber (e.g., 113) to locate the document
                    // and returns the complete Finding, including its id.
                    Finding finding = elasticsearchService.getFindingByAlertNumber(esIndex, event.getToolType(), event.getAlertNumber());
                    if (finding != null) {
                        String findingId = finding.getId(); // the actual ES document id (UUID)
                        // Look up the ticket using the ES finding id.
                        Optional<TenantTicket> ticketOpt = tenantTicketRepository.findByEsFindingId(findingId);
                        if (ticketOpt.isPresent()) {
                            String ticketId = ticketOpt.get().getTicketId();
                            LOGGER.info("Found ticket {} for finding {}. Updating ticket status to done.", ticketId, findingId);
                            updateTicketService.updateTicketStatusToDone(tenantId, ticketId, jobId);
                        } else {
                            LOGGER.warn("No ticket found for findingId={}", findingId);
                        }
                    } else {
                        LOGGER.warn("No finding document found for alert number {} and toolType {}", event.getAlertNumber(), event.getToolType());
                    }
                } catch(Exception ex) {
                    LOGGER.error("Error retrieving finding document for alert update", ex);
                }
            }
            
            sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);
        } catch (Exception e) {
            sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
            LOGGER.error("Error handling update event: {}", e.getMessage(), e);
        }
    }

    private void sendAcknowledgement(String jobId, AcknowledgementStatus status) {
        AcknowledgementPayload ackEvent = new AcknowledgementPayload(jobId);
        ackEvent.setStatus(status);
        UpdateAcknowledgement ack = new UpdateAcknowledgement(UUID.randomUUID().toString(), ackEvent);
        acknowledgementProducer.sendAcknowledgement(ack);
        LOGGER.info("Sent acknowledgement for jobId {}: {}", jobId, ack);
    }
}