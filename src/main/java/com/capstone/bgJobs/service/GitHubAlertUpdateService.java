package com.capstone.bgJobs.service;

import com.capstone.bgJobs.model.ToolType;
import com.capstone.bgJobs.model.UpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GitHubAlertUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAlertUpdateService.class);

    private final WebClient.Builder webClientBuilder;

    public GitHubAlertUpdateService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Calls GitHub API to update the alert's status.
     * 
     * @param event the UpdateEvent specifying new state/reason
     * @param pat the GitHub personal access token for this (owner, repo)
     */
    public void updateAlert(UpdateEvent event, String pat, String owner, String repo) {
        ToolType tool = event.getToolType();
        String url = buildPatchUrl(tool, owner, repo, event.getAlertNumber());
        String body = buildPatchBody(tool, event.getNewState(), event.getReason());

        String response = webClientBuilder.build()
                .patch()
                .uri(url)
                .header("Authorization", "Bearer " + pat)
                .header("Accept", "application/vnd.github+json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        LOGGER.info("GitHub PATCH response: " + response);
    }

    private String buildPatchUrl(ToolType toolType, String owner, String repo, long alertNumber) {
        String base = "https://api.github.com/repos/" + owner + "/" + repo;
        switch (toolType) {
            case CODESCAN:
                // Code scanning
                return base + "/code-scanning/alerts/" + alertNumber;
            case DEPENDABOT:
                // Dependabot
                return base + "/dependabot/alerts/" + alertNumber;
            case SECRETSCAN:
                // Secret scanning
                return base + "/secret-scanning/alerts/" + alertNumber;
            default:
                throw new IllegalArgumentException("Unsupported tool type: " + toolType);
        }
    }

    private String buildPatchBody(ToolType toolType, String newState, String reason) {
        // If "open", no reason
        if ("open".equalsIgnoreCase(newState)) {
            return "{\"state\":\"open\"}";
        }

        switch (toolType) {
            case CODESCAN:
                String mappedReason = mapDismissedReasonForCodeScan(reason);
                return "{\"state\":\"dismissed\",\"dismissed_reason\":\"" + mappedReason + "\"}";
            case DEPENDABOT:
                // GitHub expects "state= dismissed" + "dismissed_reason=xxx"
                // return "{\"state\":\"dismissed\",\"dismissed_reason\":\"" + reason + "\"}";
                String mappedReasonDependabot = mapDismissedReasonForDependabot(reason);
                return "{\"state\":\"dismissed\",\"dismissed_reason\":\"" + mappedReasonDependabot + "\"}";
            case SECRETSCAN:
                // Secret scanning expects "state= resolved" + "resolution=xxx"
                // return "{\"state\":\"resolved\",\"resolution\":\"" + reason + "\"}";
                String mappedResolution = mapResolutionForSecretscan(reason);
                return "{\"state\":\"resolved\",\"resolution\":\"" + mappedResolution + "\"}";
            default:
                throw new IllegalArgumentException("Unexpected combination: " + toolType + " & " + newState);
        }
    }

    private String mapDismissedReasonForCodeScan(String reason) {
        if (reason == null) {
            return "";
        }
        String normalized = reason.trim().toLowerCase();
        if (normalized.equals("false_positive") || normalized.equals("false positive")) {
            return "false positive";
        } else if (normalized.equals("wont_fix") || normalized.equals("won't fix") || normalized.equals("wont fix")) {
            return "won't fix";
        } else if (normalized.equals("used_in_tests") || normalized.equals("used in tests")) {
            return "used in tests";
        }
        // If the provided reason does not match expected values, throw an error or return as is.
        // Here, we return it as is; however, you might want to validate further.
        return normalized;
    }

    private String mapDismissedReasonForDependabot(String reason) {
        if (reason == null) return "";
        String normalized = reason.trim().toLowerCase();
        // For Dependabot, if the reason is "inaccurate" then it maps to false positive; otherwise use allowed values.
        if (normalized.equals("inaccurate")) {
            return "inaccurate";
        } else if (normalized.equals("no_bandwidth") || normalized.equals("no bandwidth")) {
            // GitHub expects "no bandwidth" (with space)
            return "no_bandwidth";
        } else if (normalized.equals("fix_started") || normalized.equals("fix started")) {
            return "fix_started";
        } else if (normalized.equals("not_used") || normalized.equals("not used")) {
            return "not_used";
        } else if (normalized.equals("tolerable_risk") || normalized.equals("tolerable risk")) {
            return "tolerable_risk";
        }
        return normalized;
    }

    private String mapResolutionForSecretscan(String reason) {
        if (reason == null) return "";
        String normalized = reason.trim().toLowerCase();
        if (normalized.equals("false positive") || normalized.equals("false_positive")) {
            return "false_positive";
        }
        return normalized;
    }
}