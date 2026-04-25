package com.example.auth.ingestion.model;

import com.example.auth.dto.FetchedItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Text files + PDF binaries fetched in one repository walk.
 */
public record GitHubIngestionBatch(
        List<GitHubTextFile> textFiles,
        List<GitHubPdfFile> pdfFiles
) {
    public int sourceFileCount() {
        return textFiles.size() + pdfFiles.size();
    }

    public List<FetchedItem> toFetchedItems() {
        List<FetchedItem> items = new ArrayList<>();
        for (GitHubTextFile f : textFiles) {
            items.add(new FetchedItem(f.path(), f.htmlUrl()));
        }
        for (GitHubPdfFile f : pdfFiles) {
            items.add(new FetchedItem(f.path(), f.htmlUrl()));
        }
        return items;
    }
}
