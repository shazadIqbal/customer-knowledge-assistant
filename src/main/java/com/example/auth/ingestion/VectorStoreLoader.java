package com.example.auth.ingestion;

import com.example.auth.dto.FetchedItem;
import com.example.auth.ingestion.model.GitHubIngestionBatch;
import com.example.auth.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates GitHub download → {@link Document} conversion → {@link VectorStore#add},
 * following the same pattern as {@code DocumentLoader} for classpath PDFs.
 */
@Service
public class VectorStoreLoader {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreLoader.class);

    private final GitHubService gitHubService;
    private final DocumentConverter documentConverter;
    private final VectorStore vectorStore;

    public VectorStoreLoader(
            GitHubService gitHubService,
            DocumentConverter documentConverter,
            VectorStore vectorStore) {
        this.gitHubService = gitHubService;
        this.documentConverter = documentConverter;
        this.vectorStore = vectorStore;
    }

    /**
     * Same as {@link DocumentLoader} for PDF: fetch → {@link Document} → {@link VectorStore#add}.
     * Used by {@code POST /api/v1/ingestion/github/vector-store}.
     */
    public IngestionResult loadFromGitHub(String apiKey, String folderUrl) {
        GitHubConnectIngestionResult result = ingestGitHubToVectorStore(apiKey, folderUrl);
        return new IngestionResult(result.filesFetched(), result.documentsAdded());
    }

    /**
     * Invoked when a GITHUB datasource connects: one recursive pass that both embeds into the vector store
     * and supplies {@link FetchedItem} rows for the connect response (path + html URL per text file ingested).
     */
    public GitHubConnectIngestionResult loadFromGitHubForConnect(String apiKey, String folderUrl) {
        return ingestGitHubToVectorStore(apiKey, folderUrl);
    }

    private GitHubConnectIngestionResult ingestGitHubToVectorStore(String apiKey, String folderUrl) {
        log.info("VectorStoreLoader: GitHub fetch + embed for folderUrl={}", folderUrl);
        GitHubIngestionBatch batch = gitHubService.fetchForIngestion(apiKey, folderUrl);
        log.info("VectorStoreLoader: batch — {} text file(s), {} PDF file(s)", batch.textFiles().size(), batch.pdfFiles().size());

        List<Document> documents = new ArrayList<>();
        documents.addAll(documentConverter.fromGitHubFiles(batch.textFiles()));
        documents.addAll(documentConverter.fromGitHubPdfs(batch.pdfFiles()));

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        log.info("VectorStoreLoader: added {} document(s) to vector store (embeddings, PDFs = one row per page)", documents.size());

        List<FetchedItem> items = batch.toFetchedItems();
        return new GitHubConnectIngestionResult(items, batch.sourceFileCount(), documents.size());
    }

    public record IngestionResult(int filesFetched, int documentsAdded) {
    }

    public record GitHubConnectIngestionResult(
            List<FetchedItem> items,
            int filesFetched,
            int documentsAdded
    ) {
    }
}
