package com.example.auth.connector;

import com.example.auth.dto.FetchedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches issues from the Jira REST API (v3).
 *
 * Expected inputs:
 *   apiKey    – Jira API token (Bearer)
 *   folderUrl – Jira base URL, e.g. https://yourorg.atlassian.net
 *
 * Calls: GET {folderUrl}/rest/api/3/search
 * Maps each issue to FetchedItem { name = key, url = self }
 */
@Component
public class JiraConnector implements DatasourceConnector {

    private static final Logger log = LoggerFactory.getLogger(JiraConnector.class);

    private final RestTemplate restTemplate;

    public JiraConnector(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getType() {
        return "JIRA";
    }

    @Override
    public List<FetchedItem> fetch(String apiKey, String folderUrl) {
        String url = folderUrl.replaceAll("/+$", "") + "/rest/api/3/search";
        log.info("Fetching Jira issues from: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.set("Accept", "application/json");

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        List<FetchedItem> items = new ArrayList<>();

        if (response.getBody() == null) {
            return items;
        }

        Object issuesObj = response.getBody().get("issues");
        if (!(issuesObj instanceof List)) {
            return items;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> issues = (List<Map<String, Object>>) issuesObj;

        for (Map<String, Object> issue : issues) {
            String key  = String.valueOf(issue.getOrDefault("key", ""));
            String self = String.valueOf(issue.getOrDefault("self", ""));
            items.add(new FetchedItem(key, self));
        }

        log.info("Fetched {} Jira issues", items.size());
        return items;
    }
}
