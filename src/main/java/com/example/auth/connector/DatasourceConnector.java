package com.example.auth.connector;

import com.example.auth.dto.FetchedItem;

import java.util.List;

/**
 * Strategy interface for fetching data from external datasources.
 * Each implementation handles a specific datasource type (e.g. Jira, GitHub).
 */
public interface DatasourceConnector {

    /**
     * Returns the datasource type this connector handles (e.g. "JIRA", "GITHUB").
     * Must match the normalized (uppercase, trimmed) value of Datasource.name.
     */
    String getType();

    /**
     * Fetches items from the external API. Implementations must NOT persist any data.
     *
     * @param apiKey    API key / personal access token for the external service
     * @param folderUrl Base URL or folder URL for the external service
     * @return list of fetched items (name + url pairs)
     */
    List<FetchedItem> fetch(String apiKey, String folderUrl);
}
