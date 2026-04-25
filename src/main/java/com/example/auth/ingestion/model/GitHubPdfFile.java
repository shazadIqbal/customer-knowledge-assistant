package com.example.auth.ingestion.model;

/**
 * PDF bytes from a GitHub repository (parsed with {@code PagePdfDocumentReader} like {@code DocumentLoader}).
 */
public record GitHubPdfFile(
        String path,
        String repoUrl,
        String htmlUrl,
        byte[] content
) {
}
