package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubIngestionResponse {

    private int filesFetched;
    private int documentsAdded;
    private String message;
}
