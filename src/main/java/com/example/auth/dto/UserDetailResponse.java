package com.example.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDetailResponse {

    private Long id;
    private String username;
    private String fullname;
    private String jobTitle;
    private String email;
    private String status;
    private String role;
    private List<ProjectSummary> projects;

    @Data
    public static class ProjectSummary {
        private Long id;
        private String name;
        private String status;
    }
}
