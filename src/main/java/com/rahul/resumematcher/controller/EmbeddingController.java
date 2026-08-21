package com.rahul.resumematcher.controller;

import com.rahul.resumematcher.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping("/api/embedding/test")
    public Map<String, Object> testEmbedding(
            @RequestParam(defaultValue = "Java Spring Boot developer") String text) {

        float[] embedding = embeddingService.createEmbedding(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("dimensions", embedding.length);
        response.put("embedding", embedding);

        return response;
    }

    @PostMapping("/api/embedding/store-test")
    public ResponseEntity<?> storeTestEmbedding() {

        embeddingService.createAndStoreEmbedding(
                "test-resume.txt",
                "Java Spring Boot developer with PostgreSQL experience"
        );

        return ResponseEntity.ok(
                Map.of("message", "Embedding stored successfully")
        );
    }

    @GetMapping("/api/embedding/search")
    public ResponseEntity<?> searchSimilar(
            @RequestParam String text,
            @RequestParam(defaultValue = "5") int limit) {

        return ResponseEntity.ok(
                embeddingService.searchSimilar(text, limit)
        );
    }
}