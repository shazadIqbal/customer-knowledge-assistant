package com.example.auth.controller;

import com.example.auth.dto.GitHubIngestionRequest;
import com.example.auth.dto.GitHubIngestionResponse;
import com.example.auth.ingestion.VectorStoreLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ingestion/github")
@Tag(name = "GitHub vector ingestion", description = "Load repository text into the vector store")
public class GitHubIngestionController {

    private final VectorStoreLoader vectorStoreLoader;

    public GitHubIngestionController(VectorStoreLoader vectorStoreLoader) {
        this.vectorStoreLoader = vectorStoreLoader;
    }

    @Operation(summary = "Ingest GitHub folder into VectorStore", description = "Recursively fetches text files, embeds, and stores them (same data path as the PDF DocumentLoader, but for Git).")
    @PostMapping("/vector-store")
    public ResponseEntity<GitHubIngestionResponse> ingest(@Valid @RequestBody GitHubIngestionRequest request) {
        VectorStoreLoader.IngestionResult result = vectorStoreLoader.loadFromGitHub(
                request.getApiKey(),
                request.getFolderUrl()
        );
        return ResponseEntity.ok(new GitHubIngestionResponse(
                result.filesFetched(),
                result.documentsAdded(),
                "Ingestion completed"
        ));
    }
}
