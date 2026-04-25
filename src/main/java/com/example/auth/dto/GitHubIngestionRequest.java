package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GitHubIngestionRequest {

    @NotBlank
    private String apiKey;

    @NotBlank
    private String folderUrl;
}
