package com.example.auth.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullname;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "Active|InActive", message = "Status must be 'Active' or 'InActive'")
    private String status;

    private Long roleId;

    private Long projectId;
}
