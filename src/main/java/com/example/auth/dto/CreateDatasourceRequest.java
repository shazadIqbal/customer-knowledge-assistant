package com.example.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class CreateDatasourceRequest {

    @NotBlank(message = "Datasource name is required")
    @Size(max = 100, message = "Datasource name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "Active|InActive", message = "Status must be 'Active' or 'InActive'")
    private String status = "Active";
}
