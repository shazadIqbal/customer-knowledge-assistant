package com.example.auth.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateProjectRequest {

    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Path must not exceed 255 characters")
    private String path;

    @Size(max = 100, message = "Database table name must not exceed 100 characters")
    private String databaseTableName;

    @Pattern(regexp = "Active|InActive", message = "Status must be 'Active' or 'InActive'")
    private String status;
}
