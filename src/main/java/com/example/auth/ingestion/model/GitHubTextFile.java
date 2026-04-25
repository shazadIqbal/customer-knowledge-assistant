package com.example.auth.ingestion.model;

/**
 * Plain text file material loaded from the GitHub Contents API (ready for embedding).
 */
public record GitHubTextFile(
        String path,
        String text,
        String repoUrl,
        String htmlUrl
) {
}
