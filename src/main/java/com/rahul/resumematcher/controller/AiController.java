package com.rahul.resumematcher.controller;

import com.rahul.resumematcher.service.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/api/ai/test")
    public String testAI(
            @RequestParam(defaultValue = "Explain RAG in one sentence") String question) {

        return aiService.ask(question);
    }
}