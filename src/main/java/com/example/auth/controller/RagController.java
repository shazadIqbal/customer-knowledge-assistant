package com.example.auth.controller;

import com.example.auth.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "RAG (Retrieval-Augmented Generation)", description = "AI-powered document retrieval and generation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/rag")
    @Operation(summary = "Retrieve and generate response using RAG",
            description = "Sends a user query and retrieves relevant documents from configured datasources, " +
                    "then generates a response using the OpenAI model based on the retrieved context. " +
                    "This implements Retrieval-Augmented Generation (RAG) pattern for accurate AI responses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "RAG response generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "Generated response based on retrieved documents"))),
            @ApiResponse(responseCode = "400", description = "Invalid request format or empty message",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Error during document retrieval or response generation",
                    content = @Content)
    })
    public ResponseEntity<String> generate(@RequestBody MessageRequest request) {
        String response = ragService.retrieveAndGenerate(request.message());
        return ResponseEntity.ok(response);
    }

    public static record MessageRequest(String message) {
    }
}
