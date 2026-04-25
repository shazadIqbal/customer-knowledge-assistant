package com.example.auth.controller;

import com.example.auth.dto.CreateProjectRequest;
import com.example.auth.dto.PagedResponse;
import com.example.auth.dto.ProjectResponse;
import com.example.auth.dto.UpdateProjectRequest;
import com.example.auth.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/management/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Project Management", description = "CRUD operations for projects with pagination")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project",
            description = "Creates a project and links it to a datasource (Project_Datasource association) with API credentials (api_key) and folder URL. " +
                    "Projects represent data sources that users can access for retrieval-augmented generation (RAG).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid path pattern", content = @Content),
            @ApiResponse(responseCode = "404", description = "Datasource not found", content = @Content)
    })
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all projects with pagination",
            description = "Returns a paginated list of all projects. Supports sorting and pagination parameters.")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
    public ResponseEntity<PagedResponse<ProjectResponse>> getAllProjects(
            @Parameter(description = "Zero-based page index (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by (default: id)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc' (default: asc)") @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<ProjectResponse> response = projectService.getAllProjects(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID",
            description = "Retrieve a specific project's details including its datasource associations and configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
    })
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project",
            description = "Partially updates project fields (name, path, database_table_name, status). " +
                    "The path field should match the pattern for folder URLs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid path pattern", content = @Content),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
    })
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project",
            description = "Deletes the project and all associated Project_Datasource records. " +
                    "Users assigned to this project will have their User_Project associations removed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
