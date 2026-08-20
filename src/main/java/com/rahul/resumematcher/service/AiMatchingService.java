package com.rahul.resumematcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Optional layer that asks an LLM for qualitative resume-improvement suggestions.
 *
 * This is intentionally OFF by default (no api key configured => no network call).
 * The core matching endpoint (MatchingService) works fully without this class, so the
 * project runs and is demoable with zero external dependencies. Set app.ai.api-key in
 * application.properties (or an env var) to enable it.
 *
 * Written against the Anthropic Messages API shape. Swap the URL/model/request body
 * if you plug in a different provider.
 */
@Service
public class AiMatchingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:claude-sonnet-4-6}")
    private String model;

    @Value("${app.ai.api-url:https://api.anthropic.com/v1/messages}")
    private String apiUrl;

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Returns a short, actionable improvement suggestion string, or null if AI matching
     * is disabled or the call fails (callers should treat null as "skip, use rule-based only").
     */
    public String getSuggestions(String resumeText, String jobDescription, List<String> missingKeywords) {
        if (!isEnabled()) {
            return null;
        }

        String prompt = """
                You are reviewing a resume against a job description.
                Missing keywords the candidate should consider addressing: %s

                Job description:
                %s

                Resume:
                %s

                In under 120 words, give the candidate 3-4 concrete, specific suggestions for how
                to improve their resume for this role. Be direct and practical, not generic.
                """.formatted(String.join(", ", missingKeywords), truncate(jobDescription, 3000), truncate(resumeText, 4000));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", 400,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String rawResponse = restTemplate.postForObject(apiUrl, request, String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentArray = root.path("content");
            if (contentArray.isArray() && contentArray.size() > 0) {
                return contentArray.get(0).path("text").asText(null);
            }
            return null;
        } catch (Exception ex) {
            // Fail soft: AI suggestions are a bonus, never block the core response
            return null;
        }
    }

    private String truncate(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, maxChars);
    }
}
