package com.example.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Path must not exceed 255 characters")
    private String path;

    @Size(max = 100, message = "Database table name must not exceed 100 characters")
    private String databaseTableName;

    @Pattern(regexp = "Active|InActive", message = "Status must be 'Active' or 'InActive'")
    private String status = "Active";

    @NotNull(message = "Datasource ID is required")
    private Long datasourceId;

    @Size(max = 500, message = "API key must not exceed 500 characters")
    private String apiKey;

    @Size(max = 1000, message = "Folder URL must not exceed 1000 characters")
    private String folderUrl;
}
