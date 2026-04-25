package com.example.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
public class ConnectDatasourceRequest {

    @NotNull(message = "projectId is required")
    private Long projectId;

    @NotNull(message = "datasourceId is required")
    private Long datasourceId;

    @NotBlank(message = "apiKey is required")
    private String apiKey;

    @NotBlank(message = "folderUrl is required")
    private String folderUrl;
}
