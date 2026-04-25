package com.example.auth.service;

import com.example.auth.connector.DatasourceConnector;
import com.example.auth.connector.DatasourceConnectorFactory;
import com.example.auth.dto.ConnectDatasourceRequest;
import com.example.auth.dto.DatasourceConnectResponse;
import com.example.auth.dto.FetchedItem;
import com.example.auth.entity.Datasource;
import com.example.auth.entity.Project;
import com.example.auth.entity.ProjectDatasource;
import com.example.auth.entity.ProjectDatasourceId;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.repository.DatasourceRepository;
import com.example.auth.repository.ProjectDatasourceRepository;
import com.example.auth.ingestion.VectorStoreLoader;
import com.example.auth.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectDatasourceIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(ProjectDatasourceIntegrationService.class);

    private final ProjectRepository projectRepository;
    private final DatasourceRepository datasourceRepository;
    private final ProjectDatasourceRepository projectDatasourceRepository;
    private final DatasourceConnectorFactory connectorFactory;
    private final VectorStoreLoader vectorStoreLoader;

    public ProjectDatasourceIntegrationService(
            ProjectRepository projectRepository,
            DatasourceRepository datasourceRepository,
            ProjectDatasourceRepository projectDatasourceRepository,
            DatasourceConnectorFactory connectorFactory,
            VectorStoreLoader vectorStoreLoader) {
        this.projectRepository = projectRepository;
        this.datasourceRepository = datasourceRepository;
        this.projectDatasourceRepository = projectDatasourceRepository;
        this.connectorFactory = connectorFactory;
        this.vectorStoreLoader = vectorStoreLoader;
    }

    /**
     * Validates project and datasource, saves the mapping, then fetches live data
     * from the external API using the appropriate connector strategy.
     * For {@code GITHUB}, text files are also embedded into the Spring AI {@code VectorStore}
     * (same pattern as {@code DocumentLoader} for PDFs). Relational DB stores only the mapping, not file bodies.
     */
    @Transactional
    public DatasourceConnectResponse connectAndFetch(ConnectDatasourceRequest request) {

        // Step 1: Validate project exists
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + request.getProjectId()));

        // Step 2: Validate datasource exists
        Datasource datasource = datasourceRepository.findById(request.getDatasourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Datasource not found with id: " + request.getDatasourceId()));

        // Step 3: Save or update the ProjectDatasource mapping
        saveOrUpdateMapping(project, datasource, request.getApiKey(), request.getFolderUrl());

        // Step 4: Select connector strategy based on datasource name (e.g. "JIRA", "GITHUB")
        DatasourceConnector connector;
        try {
            connector = connectorFactory.getConnector(datasource.getName());
        } catch (IllegalArgumentException e) {
            log.warn("Unsupported datasource type '{}': {}", datasource.getName(), e.getMessage());
            return DatasourceConnectResponse.failure(e.getMessage());
        }

        // Step 5: Fetch from external API; GITHUB also ingests text into VectorStore (like DocumentLoader for PDF).
        try {
            List<FetchedItem> items;
            if ("GITHUB".equalsIgnoreCase(datasource.getName().trim())) {
                VectorStoreLoader.GitHubConnectIngestionResult ingested =
                        vectorStoreLoader.loadFromGitHubForConnect(request.getApiKey(), request.getFolderUrl());
                items = ingested.items();
                log.info("GitHub connect: {} file(s) fetched, {} document(s) added to vector store",
                        ingested.filesFetched(), ingested.documentsAdded());
            } else {
                items = connector.fetch(request.getApiKey(), request.getFolderUrl());
            }
            log.info("Successfully fetched {} items from {} datasource (id={})",
                    items.size(), datasource.getName(), datasource.getId());

            return DatasourceConnectResponse.success(items);
        } catch (Exception e) {
            log.error("Failed to fetch data from {} datasource: {}", datasource.getName(), e.getMessage(), e);
            return DatasourceConnectResponse.failure(
                    "Failed to fetch data from " + datasource.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Creates a new ProjectDatasource record or updates the existing one
     * (upsert by composite key projectId + datasourceId).
     */
    private void saveOrUpdateMapping(Project project, Datasource datasource,
                                     String apiKey, String folderUrl) {
        ProjectDatasourceId compositeId = new ProjectDatasourceId(project.getId(), datasource.getId());

        ProjectDatasource mapping = projectDatasourceRepository.findById(compositeId)
                .orElse(new ProjectDatasource(project, datasource, apiKey, folderUrl));

        mapping.setApiKey(apiKey);
        mapping.setFolderUrl(folderUrl);

        projectDatasourceRepository.save(mapping);
        log.info("Saved ProjectDatasource mapping for projectId={} datasourceId={}",
                project.getId(), datasource.getId());
    }
}
