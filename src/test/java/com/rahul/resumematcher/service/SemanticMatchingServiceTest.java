package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SemanticMatchingServiceTest {

    private final EmbeddingService embeddingService = mock(EmbeddingService.class);
    private final MatchingProperties matchingProperties = new MatchingProperties();
    private final SemanticMatchingService semanticMatchingService =
            new SemanticMatchingService(embeddingService, matchingProperties);

    @Test
    void convertsCosineDistanceToAverageTopKSemanticScore() {
        matchingProperties.setSemanticTopK(2);
        when(embeddingService.searchSimilar("Java role", "resume-a", 2))
                .thenReturn(List.of(
                        Map.of("distance", 0.2),
                        Map.of("distance", 0.4)
                ));

        assertEquals(70.0,
                semanticMatchingService.calculateScore("resume-a", "Java role").getAsDouble());
        verify(embeddingService).searchSimilar("Java role", "resume-a", 2);
    }

    @Test
    void clampsSimilarityOutsideExpectedRange() {
        when(embeddingService.searchSimilar("role", "resume-a", 5))
                .thenReturn(List.of(Map.of("distance", 1.5)));

        assertEquals(0.0,
                semanticMatchingService.calculateScore("resume-a", "role").getAsDouble());
        assertTrue(semanticMatchingService.calculateScore("resume-a", "role").isPresent());
    }
}