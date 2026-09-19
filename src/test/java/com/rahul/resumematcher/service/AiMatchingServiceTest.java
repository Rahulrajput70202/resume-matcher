package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class AiMatchingServiceTest {

    @Test
    void missingClaudeApiKeyLeavesQualitativeSuggestionsDisabled() {
        AiMatchingService service = new AiMatchingService();
        ReflectionTestUtils.setField(service, "apiKey", "");

        assertFalse(service.isEnabled());
        assertNull(service.getSuggestions("resume", "job", List.of("java")));
    }
}