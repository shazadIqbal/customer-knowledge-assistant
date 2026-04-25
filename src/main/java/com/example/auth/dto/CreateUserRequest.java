package com.example.auth.dto;

import com.example.auth.entity.AppSource;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullname;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "Active|InActive", message = "Status must be 'Active' or 'InActive'")
    private String status = "Active";

    @NotNull(message = "Role ID is required")
    private Long roleId;

    //@NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "App source is required")
    private AppSource appSource;

    // Auth credentials — required to create the underlying auth account
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
