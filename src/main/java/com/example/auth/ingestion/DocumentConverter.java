package com.example.auth.ingestion;

import com.example.auth.ingestion.model.GitHubPdfFile;
import com.example.auth.ingestion.model.GitHubTextFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps GitHub file payloads to Spring AI {@link Document} instances with consistent metadata
 * (same role as the PDF path that uses {@code PagePdfDocumentReader#get()} + metadata).
 */
@Component
public class DocumentConverter {

    private static final Logger log = LoggerFactory.getLogger(DocumentConverter.class);

    public List<Document> fromGitHubFiles(List<GitHubTextFile> files) {
        return files.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    public Document toDocument(GitHubTextFile file) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "github");
        metadata.put("fileName", file.path());
        metadata.put("repoUrl", file.repoUrl());
        if (file.htmlUrl() != null && !file.htmlUrl().isEmpty()) {
            metadata.put("htmlUrl", file.htmlUrl());
        }
        return new Document(file.text(), metadata);
    }

    /**
     * Same approach as {@link com.example.auth.runner.DocumentLoader}: {@link PagePdfDocumentReader} per PDF resource.
     */
    public List<Document> fromGitHubPdfs(List<GitHubPdfFile> pdfs) {
        List<Document> out = new ArrayList<>();
        for (GitHubPdfFile pdf : pdfs) {
            try {
                String fileName = pdf.path().contains("/")
                        ? pdf.path().substring(pdf.path().lastIndexOf('/') + 1)
                        : pdf.path();
                ByteArrayResource resource = new ByteArrayResource(pdf.content()) {
                    @Override
                    public String getFilename() {
                        return fileName.toLowerCase().endsWith(".pdf") ? fileName : fileName + ".pdf";
                    }
                };
                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
                for (Document page : reader.get()) {
                    Map<String, Object> metadata = new HashMap<>();
                    if (page.getMetadata() != null) {
                        metadata.putAll(page.getMetadata());
                    }
                    metadata.put("source", "github");
                    metadata.put("fileName", pdf.path());
                    metadata.put("repoUrl", pdf.repoUrl());
                    metadata.put("contentType", "application/pdf");
                    if (pdf.htmlUrl() != null && !pdf.htmlUrl().isEmpty()) {
                        metadata.put("htmlUrl", pdf.htmlUrl());
                    }
                    out.add(new Document(page.getText(), metadata));
                }
            } catch (Exception e) {
                log.error("DocumentConverter: failed to parse PDF from GitHub path={}", pdf.path(), e);
            }
        }
        return out;
    }
}
