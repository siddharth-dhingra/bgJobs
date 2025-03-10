package com.capstone.bgJobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;

import com.capstone.bgJobs.model.Finding;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.model.ToolType;
import com.capstone.bgJobs.repository.TenantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);
    private final ElasticsearchClient esClient;
    private final TenantRepository tenantRepository;

    public ElasticsearchService(ElasticsearchClient esClient, TenantRepository tenantRepository) {
        this.esClient = esClient;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Updates the status field (and updatedAt) of the Finding document(s) in the specified index.
     * The document is matched using:
     *  - The top-level field "toolType.keyword" matching the given toolType.
     *  - The field "additionalData.number" matching the provided alertNumber.
     *
     * @param esIndex    the Elasticsearch index where Finding documents are stored.
     * @param alertNumber the alert number (from additionalData.number) to match.
     * @param toolType   the tool type.
     * @param newStatus  the new status to update in the document.
     * @throws Exception if the update fails.
     */
    public void updateAlertStatus(String esIndex, long alertNumber, ToolType toolType, String newStatus) throws Exception {
        // Build term query for toolType (top-level field)
        Query toolTypeQuery = Query.of(q -> q.term(t -> t.field("toolType.keyword").value(toolType.name())));
        // Build term query for the alert number stored under additionalData.number
        Query numberQuery = Query.of(q -> q.term(t -> t.field("additionalData.number").value(alertNumber)));

        // Combine queries with a boolean AND
        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(toolTypeQuery)
                .must(numberQuery)
                .build();

        SearchRequest searchReq = SearchRequest.of(s -> s
                .index(esIndex)
                .query(Query.of(b -> b.bool(boolQuery)))
                .size(10) // adjust size if you expect more matches
        );

        SearchResponse<Finding> searchResponse = esClient.search(searchReq, Finding.class);
        List<Finding> hits = searchResponse.hits().hits().stream()
                .map(h -> h.source())
                .collect(Collectors.toList());

        System.out.println(hits);
        System.out.println(hits.size());

        if (hits.isEmpty()) {
            LOGGER.warn("No Finding documents found in index {} matching toolType={} and additionalData.number={}", 
                        esIndex, toolType.name(), alertNumber);
            return;
        }

        // Prepare a partial update document with new "status" and updated "updatedAt"
        Map<String, Object> partialDoc = Map.of(
                "status", newStatus,
                "updatedAt", LocalDateTime.now().toString()
        );

        for (Finding doc : hits) {
            if (doc == null || doc.getId() == null) continue;
            String docId = doc.getId();
            System.out.println(docId);
            UpdateRequest<Map<String, Object>, Map<String, Object>> updateReq = UpdateRequest.of(u -> u
                    .index(esIndex)
                    .id(docId)
                    .doc(partialDoc)
            );
            esClient.update(updateReq, Map.class);
            LOGGER.info("Updated document (id={}) in index {} with newStatus={}", docId, esIndex, newStatus);
        }
    }

    private String getFindingsIndex(String tenantId) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Invalid tenantId: " + tenantId));
        return tenant.getEsIndex();
    }

    public void updateFindingWithTicketId(String tenantId, String findingId, String ticketId) {
        String indexName = getFindingsIndex(tenantId);

        Finding findingUpdate = new Finding();
        findingUpdate.setTicketId(ticketId);
        findingUpdate.setUpdatedAt(LocalDateTime.now().toString());

        System.out.println("Updating finding with ticket ID: " + ticketId);
        System.out.println("Finding ID: " + findingId);
        System.out.println("Index name: " + indexName);
        
        UpdateRequest<Finding, Finding> updateRequest = UpdateRequest.of(u -> u
            .index(indexName)
            .id(findingId)
            .doc(findingUpdate)
        );

        try {
            esClient.update(updateRequest, Finding.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update finding with ticket ID", e);
        }
    }

    public Finding getFindingByAlertNumber(String esIndex, ToolType toolType, long alertNumber) {
        try {
            SearchResponse<Finding> response = esClient.search(s -> s
                .index(esIndex)
                .query(q -> q.bool(b -> b
                    .must(m -> m.match(t -> t.field("additionalData.number").query(String.valueOf(alertNumber))))
                    .must(m -> m.match(t -> t.field("toolType").query(toolType.name())))
                )), Finding.class);
            if (response.hits().hits().size() > 0) {
                Hit<Finding> hit = response.hits().hits().get(0);
                return hit.source();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching finding by alert number", e);
        }
    }

    public Finding getFindingById(String tenantId, String findingId) {
        String indexName = getFindingsIndex(tenantId);
        try {
            GetRequest getRequest = GetRequest.of(r -> r.index(indexName).id(findingId));
            GetResponse<Finding> getResponse = esClient.get(getRequest, Finding.class);
            if (getResponse.found()) {
                return getResponse.source();
            } else {
                LOGGER.warn("No Finding document found in index {} for findingId={}", indexName, findingId);
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving finding by id: " + findingId, e);
        }
    }

}