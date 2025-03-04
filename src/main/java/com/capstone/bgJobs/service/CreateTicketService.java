package com.capstone.bgJobs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.capstone.bgJobs.dto.UpdateAcknowledgement;
import com.capstone.bgJobs.model.AcknowledgementPayload;
import com.capstone.bgJobs.model.AcknowledgementStatus;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.model.TenantTicket;
import com.capstone.bgJobs.producer.AcknowledgementProducer;
import com.capstone.bgJobs.repository.TenantRepository;
import com.capstone.bgJobs.repository.TenantTicketRepository;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CreateTicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTicketService.class);


    private final TenantRepository tenantRepository;
    private final TenantTicketRepository tenantTicketRepository;
    private final ElasticsearchService elasticsearchService;
    private final WebClient.Builder webClientBuilder;
    private final AcknowledgementProducer acknowledgementProducer;


    @Value("${jira.api.base-path:/rest/api/2}")
    private String jiraApiBasePath;

    /**
     * We inject a WebClient.Builder to create a dedicated WebClient instance for this service.
     * You can configure timeouts, etc., if needed.
     */
    public CreateTicketService(
            TenantRepository tenantRepository,
            TenantTicketRepository tenantTicketRepository,
            ElasticsearchService elasticsearchService,
            WebClient.Builder webClientBuilder,
            AcknowledgementProducer acknowledgementProducer
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantTicketRepository = tenantTicketRepository;
        this.elasticsearchService = elasticsearchService;
        // Build a default WebClient. Adjust baseUrl, timeouts, etc. as desired.
        this.webClientBuilder = webClientBuilder;
        this.acknowledgementProducer = acknowledgementProducer;
    }

    /**
     * Creates a Jira ticket for a given finding.
     */
    public String handleCreateTicketEvent(String tenantId, String findingId, String summary, String description, String jobId) {

        Optional<TenantTicket> existingTicket = tenantTicketRepository.findByEsFindingId(findingId);
        if (existingTicket.isPresent()) {
            String existingTicketId = existingTicket.get().getTicketId();
            LOGGER.info("Ticket already exists for finding id {}: returning existing ticket id {}", findingId, existingTicketId);
            sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);
            return existingTicketId;
        }

        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Invalid tenantId: " + tenantId));

        String url = String.format("https://%s%s/issue", tenant.getAccountUrl(), jiraApiBasePath);

        // Prepare the request body
        Map<String, Object> projectMap = new HashMap<>();
        projectMap.put("key", tenant.getProjectKey());

        Map<String, Object> issueTypeMap = new HashMap<>();
        issueTypeMap.put("name", "Bug"); // or "Task" depending on your needs

        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("project", projectMap);
        fieldsMap.put("summary", summary);
        fieldsMap.put("description", description);
        fieldsMap.put("issuetype", issueTypeMap);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("fields", fieldsMap);

        // Authorization
        String authString = tenant.getUsername() + ":" + tenant.getApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response =
            webClientBuilder.build().post()
                            .uri(url)
                            .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .bodyValue(bodyMap)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response == null) {
                throw new RuntimeException("Failed to create ticket: Jira response was null.");
            }

            String ticketId = (String) response.get("key");
            if (ticketId == null) {
                throw new RuntimeException("Missing ticket key in Jira creation response.");
            }

            // Update the finding in Elasticsearch with the new ticket ID.
            elasticsearchService.updateFindingWithTicketId(tenantId, findingId, ticketId);

            // Save mapping in tenant_ticket table.
            TenantTicket tenantTicket = new TenantTicket();
            tenantTicket.setTenantId(tenantId);
            tenantTicket.setTicketId(ticketId);
            tenantTicket.setEsFindingId(findingId);
            tenantTicketRepository.save(tenantTicket);

            sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);

            return ticketId;
        } catch (WebClientResponseException wce) {
            sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
            throw new RuntimeException(
                    "Jira create ticket API returned error: " + wce.getStatusCode() + " " + wce.getResponseBodyAsString(),
                    wce);
        } catch (Exception ex) {
            sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
            throw new RuntimeException("Failed to create Jira ticket: " + ex.getMessage(), ex);
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