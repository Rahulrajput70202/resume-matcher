package com.rahul.resumematcher.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Service
public class SemanticMatchingService {

    private final EmbeddingService embeddingService;
    private final MatchingProperties matchingProperties;

    public SemanticMatchingService(
            EmbeddingService embeddingService,
            MatchingProperties matchingProperties) {
        this.embeddingService = embeddingService;
        this.matchingProperties = matchingProperties;
    }

    public OptionalDouble calculateScore(String resumeId, String jobDescription) {
        List<Map<String, Object>> chunks = embeddingService.searchSimilar(
                jobDescription,
                resumeId,
                matchingProperties.getSemanticTopK()
        );

        double totalSimilarity = 0.0;
        int validSimilarityCount = 0;

        for (Map<String, Object> chunk : chunks) {
            Object distanceValue = chunk.get("distance");
            if (!(distanceValue instanceof Number distance)) {
                continue;
            }

            // Average the retrieved top-K similarities so one short chunk does not dominate.
            double similarity = 1.0 - distance.doubleValue();
            similarity = Math.max(0.0, Math.min(1.0, similarity));
            totalSimilarity += similarity;
            validSimilarityCount++;
        }

        if (validSimilarityCount == 0) {
            return OptionalDouble.empty();
        }

        double score = totalSimilarity / validSimilarityCount * 100.0;
        return OptionalDouble.of(roundToOneDecimal(score));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}