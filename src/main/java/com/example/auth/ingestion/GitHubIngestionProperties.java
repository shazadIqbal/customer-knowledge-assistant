package com.example.auth.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github-ingestion")
public class GitHubIngestionProperties {

    /**
     * Maximum directory depth when recursing into folders.
     */
    private int maxDepth = 15;

    /**
     * Safety cap on number of text files ingested in one run.
     */
    private int maxFiles = 500;

    /**
     * Skip files larger than this (bytes).
     */
    private long maxFileSizeBytes = 5_000_000L;

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}
