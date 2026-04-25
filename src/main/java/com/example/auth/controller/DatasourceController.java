package com.example.auth.controller;

import com.example.auth.dto.CreateDatasourceRequest;
import com.example.auth.dto.DatasourceResponse;
import com.example.auth.dto.PagedResponse;
import com.example.auth.dto.UpdateDatasourceRequest;
import com.example.auth.service.DatasourceService;
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
@RequestMapping("/api/management/datasources")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Datasource Management", description = "CRUD operations for datasources with pagination")
@SecurityRequirement(name = "bearerAuth")
public class DatasourceController {

    @Autowired
    private DatasourceService datasourceService;

    @PostMapping
    @Operation(summary = "Create a new datasource",
            description = "Creates a datasource that can be linked to projects. " +
                    "Datasources represent data repositories for retrieval-augmented generation (RAG) operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Datasource created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatasourceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    public ResponseEntity<DatasourceResponse> createDatasource(@Valid @RequestBody CreateDatasourceRequest request) {
        DatasourceResponse response = datasourceService.createDatasource(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all datasources with pagination",
            description = "Returns a paginated list of all available datasources. Supports sorting and pagination parameters.")
    @ApiResponse(responseCode = "200", description = "Datasources retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
    public ResponseEntity<PagedResponse<DatasourceResponse>> getAllDatasources(
            @Parameter(description = "Zero-based page index (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by (default: id)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc' (default: asc)") @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<DatasourceResponse> response = datasourceService.getAllDatasources(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get datasource by ID",
            description = "Retrieve a specific datasource's details including its associated projects.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datasource found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatasourceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Datasource not found", content = @Content)
    })
    public ResponseEntity<DatasourceResponse> getDatasourceById(
            @Parameter(description = "Datasource ID") @PathVariable Long id) {
        DatasourceResponse response = datasourceService.getDatasourceById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a datasource",
            description = "Partially updates datasource fields (name, status). " +
                    "Updating a datasource will affect all projects linked to it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datasource updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatasourceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "Datasource not found", content = @Content)
    })
    public ResponseEntity<DatasourceResponse> updateDatasource(
            @Parameter(description = "Datasource ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDatasourceRequest request) {
        DatasourceResponse response = datasourceService.updateDatasource(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a datasource",
            description = "Deletes the datasource and all associated Project_Datasource records. " +
                    "Projects linked to this datasource will have their associations removed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Datasource deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Datasource not found", content = @Content)
    })
    public ResponseEntity<Void> deleteDatasource(
            @Parameter(description = "Datasource ID") @PathVariable Long id) {
        datasourceService.deleteDatasource(id);
        return ResponseEntity.noContent().build();
    }
}
