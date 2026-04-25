package com.example.auth.controller;

import com.example.auth.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ai/rag")
    public String generate(@RequestBody MessageRequest request) {
        return ragService.retrieveAndGenerate(request.message());
    }

    public static record MessageRequest(String message) {
    }
}
