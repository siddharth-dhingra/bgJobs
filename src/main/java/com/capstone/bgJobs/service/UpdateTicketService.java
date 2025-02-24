package com.capstone.bgJobs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.capstone.bgJobs.dto.UpdateAcknowledgement;
import com.capstone.bgJobs.model.AcknowledgementPayload;
import com.capstone.bgJobs.model.AcknowledgementStatus;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.producer.AcknowledgementProducer;
import com.capstone.bgJobs.repository.TenantRepository;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UpdateTicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTicketService.class);

    private final TenantRepository tenantRepository;
    private final WebClient.Builder webClientBuilder;
    private final AcknowledgementProducer acknowledgementProducer;


    @Value("${jira.api.base-path:/rest/api/2}")
    private String jiraApiBasePath;

    /**
     * We inject a WebClient.Builder to create a dedicated WebClient instance for this service.
     * You can configure timeouts, etc., if needed.
     */
    public UpdateTicketService(
            TenantRepository tenantRepository,
            WebClient.Builder webClientBuilder,
            AcknowledgementProducer acknowledgementProducer
    ) {
        this.tenantRepository = tenantRepository;
        this.webClientBuilder = webClientBuilder;
        this.acknowledgementProducer = acknowledgementProducer;
    }

    public void updateTicketStatusToDone(String tenantId, String ticketId, String jobId) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Invalid tenantId: " + tenantId));
    
        String transitionsUrl = String.format("https://%s%s/issue/%s/transitions?expand=transitions.fields",
                tenant.getAccountUrl(), jiraApiBasePath, ticketId);
    
        String authString = tenant.getUsername() + ":" + tenant.getApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    
        // Loop until no transitions are available.
        while (true) {
            Map<String, Object> transitionsBody;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = webClientBuilder.build().get()
                        .uri(transitionsUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
    
                if (body == null) {
                    // No response from Jira – break out of the loop.
                    break;
                }
                transitionsBody = body;
            } catch (Exception ex) {
                sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
                throw new RuntimeException("Failed to fetch Jira transitions: " + ex.getMessage(), ex);
            }
    
            // Extract the transitions list.
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) transitionsBody.get("transitions");
            if (transitions == null || transitions.isEmpty()) {
                // No transitions available means we've reached the terminal state.
                break;
            }
    
            // Take the first available transition.
            Map<String, Object> firstTransition = transitions.get(0);
            String transitionId = (String) firstTransition.get("id");
    
            // Build the transition payload.
            Map<String, Object> transitionObj = new HashMap<>();
            transitionObj.put("id", transitionId);
            Map<String, Object> transitionData = new HashMap<>();
            transitionData.put("transition", transitionObj);
    
            // Perform the transition.
            try {
                webClientBuilder.build().post()
                        .uri(transitionsUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(transitionData)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            } catch (Exception ex) {
                sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
                throw new RuntimeException("Failed to transition Jira ticket: " + ex.getMessage(), ex);
            }
        }

        sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);
    }    

    private void sendAcknowledgement(String jobId, AcknowledgementStatus status) {
        AcknowledgementPayload ackEvent = new AcknowledgementPayload(jobId);
        ackEvent.setStatus(status);
        UpdateAcknowledgement ack = new UpdateAcknowledgement(UUID.randomUUID().toString(), ackEvent);
        acknowledgementProducer.sendAcknowledgement(ack);
        LOGGER.info("Sent acknowledgement for jobId {}: {}", jobId, ack);
    }
}