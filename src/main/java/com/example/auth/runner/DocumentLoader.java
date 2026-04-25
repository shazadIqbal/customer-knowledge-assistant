package com.example.auth.runner;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentLoader implements CommandLineRunner {

    private final VectorStore vectorStore;

    public DocumentLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
        // Check if documents are already loaded to avoid re-embedding on every startup
        List<Document> existing = vectorStore.similaritySearch(
                SearchRequest.builder().query("story").topK(1).build());
        if (existing != null && !existing.isEmpty()) {
            System.out.println("Documents already loaded in VectorStore. Skipping ingestion.");
            return;
        }

        ClassPathResource pdfResource = new ClassPathResource("docs/Story.pdf");
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
        List<Document> documents = pdfReader.get();
        vectorStore.add(documents);
        System.out.println("Loaded " + documents.size() + " page(s) from Story.pdf into VectorStore.");
    }
}
