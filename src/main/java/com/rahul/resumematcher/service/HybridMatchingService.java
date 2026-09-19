package com.rahul.resumematcher.service;

import java.util.List;
import java.util.OptionalDouble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HybridMatchingService {

    private static final Logger log = LoggerFactory.getLogger(HybridMatchingService.class);

    private final MatchingService matchingService;
    private final SemanticMatchingService semanticMatchingService;
    private final MatchingProperties matchingProperties;

    public HybridMatchingService(
            MatchingService matchingService,
            SemanticMatchingService semanticMatchingService,
            MatchingProperties matchingProperties) {
        this.matchingService = matchingService;
        this.semanticMatchingService = semanticMatchingService;
        this.matchingProperties = matchingProperties;
        matchingProperties.validate();
    }

    public HybridMatchResult match(
            String resumeId,
            String resumeText,
            String jobDescription) {

        MatchingService.MatchResult keywordResult =
                matchingService.match(resumeText, jobDescription);

        OptionalDouble semanticScore = OptionalDouble.empty();
        try {
            semanticScore = semanticMatchingService.calculateScore(resumeId, jobDescription);
        } catch (RuntimeException ex) {
            log.warn("Semantic matching failed for resume {}. Falling back to keyword score.", resumeId, ex);
        }

        double finalScore = keywordResult.getScore();
        Double semanticValue = null;
        if (semanticScore.isPresent()) {
            semanticValue = semanticScore.getAsDouble();
            finalScore = keywordResult.getScore() * matchingProperties.getKeywordWeight()
                    + semanticValue * matchingProperties.getSemanticWeight();
            finalScore = Math.max(0.0, Math.min(100.0, finalScore));
            finalScore = roundToOneDecimal(finalScore);
        }
       log.info(
        "Hybrid matching for resume {} -> keywordScore={}, semanticScore={}, finalScore={}",
        resumeId,
        keywordResult.getScore(),
        semanticValue,
        finalScore
);

        return new HybridMatchResult(
                keywordResult.getScore(),
                semanticValue,
                finalScore,
                keywordResult.getMatchedKeywords(),
                keywordResult.getMissingKeywords()
        );
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static class HybridMatchResult {
        private final double keywordScore;
        private final Double semanticScore;
        private final double finalScore;
        private final List<String> matchedKeywords;
        private final List<String> missingKeywords;

        public HybridMatchResult(
                double keywordScore,
                Double semanticScore,
                double finalScore,
                List<String> matchedKeywords,
                List<String> missingKeywords) {
            this.keywordScore = keywordScore;
            this.semanticScore = semanticScore;
            this.finalScore = finalScore;
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
        }

        public double getKeywordScore() {
            return keywordScore;
        }

        public Double getSemanticScore() {
            return semanticScore;
        }

        public double getFinalScore() {
            return finalScore;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public List<String> getMissingKeywords() {
            return missingKeywords;
        }
    }
}