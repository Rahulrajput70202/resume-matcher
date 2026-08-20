package com.rahul.resumematcher.controller;

import com.rahul.resumematcher.service.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}