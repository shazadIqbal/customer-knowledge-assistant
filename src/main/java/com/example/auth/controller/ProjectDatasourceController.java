package com.example.auth.controller;

import com.example.auth.dto.ConnectDatasourceRequest;
import com.example.auth.dto.DatasourceConnectResponse;
import com.example.auth.service.ProjectDatasourceIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/project-datasource")
@Tag(name = "Project Datasource Integration", description = "Connect datasources to projects and fetch external data")
public class ProjectDatasourceController {

    private final ProjectDatasourceIntegrationService integrationService;

    public ProjectDatasourceController(ProjectDatasourceIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /**
     * Connects a datasource to a project, saves the mapping, and immediately
     * fetches live data from the external API (Jira / GitHub / …).
     *
     * Fetched data is returned in the response body and is never stored in the DB.
     */
    @Operation(
            summary = "Connect datasource to project",
            description = "Validates project & datasource, saves the mapping, fetches live data from the external API using the appropriate connector strategy, and returns the result."
    )
    @PostMapping("/connect")
    public ResponseEntity<DatasourceConnectResponse> connect(
            @Valid @RequestBody ConnectDatasourceRequest request) {

        DatasourceConnectResponse response = integrationService.connectAndFetch(request);

        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
