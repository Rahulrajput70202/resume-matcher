package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridMatchingServiceTest {

    private final MatchingService matchingService = mock(MatchingService.class);
    private final SemanticMatchingService semanticMatchingService = mock(SemanticMatchingService.class);

    @Test
    void combinesKeywordAndSemanticScoresUsingConfiguredWeights() {
        MatchingProperties properties = new MatchingProperties();
        properties.setKeywordWeight(0.25);
        properties.setSemanticWeight(0.75);
        when(matchingService.match("resume", "job"))
                .thenReturn(new MatchingService.MatchResult(80.0, List.of("java"), List.of("kafka")));
        when(semanticMatchingService.calculateScore("resume-a", "job"))
                .thenReturn(OptionalDouble.of(60.0));

        HybridMatchingService service =
                new HybridMatchingService(matchingService, semanticMatchingService, properties);

        HybridMatchingService.HybridMatchResult result = service.match("resume-a", "resume", "job");

        assertEquals(80.0, result.getKeywordScore());
        assertEquals(60.0, result.getSemanticScore());
        assertEquals(65.0, result.getFinalScore());
        assertEquals(List.of("java"), result.getMatchedKeywords());
        assertEquals(List.of("kafka"), result.getMissingKeywords());
    }

    @Test
    void fallsBackToKeywordScoreWhenSemanticMatchingFails() {
        MatchingProperties properties = new MatchingProperties();
        when(matchingService.match("resume", "job"))
                .thenReturn(new MatchingService.MatchResult(72.5, List.of(), List.of("java")));
        when(semanticMatchingService.calculateScore("resume-a", "job"))
                .thenThrow(new IllegalStateException("vector database unavailable"));

        HybridMatchingService service =
                new HybridMatchingService(matchingService, semanticMatchingService, properties);

        HybridMatchingService.HybridMatchResult result = service.match("resume-a", "resume", "job");

        assertNull(result.getSemanticScore());
        assertEquals(72.5, result.getFinalScore());
    }

    @Test
    void rejectsWeightsThatDoNotSumToOne() {
        MatchingProperties properties = new MatchingProperties();
        properties.setKeywordWeight(0.6);
        properties.setSemanticWeight(0.6);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> new HybridMatchingService(matchingService, semanticMatchingService, properties));
    }
}