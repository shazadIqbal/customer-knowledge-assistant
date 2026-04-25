package com.example.auth.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateDatasourceRequest {

    @Size(max = 100, message = "Datasource name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "Active|InActive", message = "Status must be 'Active' or 'InActive'")
    private String status;
}
