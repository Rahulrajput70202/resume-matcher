package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchingServiceTest {

    private final MatchingService matchingService = new MatchingService();

    @Test
    void fullMatchScoresHigh() {
        String jobDescription = "We need a Java developer with Spring Boot, REST API, SQL and Git experience.";
        String resumeText = "Experienced in Java, Spring Boot, building REST API services, SQL databases, and Git.";

        MatchingService.MatchResult result = matchingService.match(resumeText, jobDescription);

        assertTrue(result.getScore() > 50.0, "Expected a high match score, got " + result.getScore());
        assertTrue(result.getMatchedKeywords().contains("java"));
        assertTrue(result.getMatchedKeywords().contains("spring boot"));
    }

    @Test
    void noOverlapScoresZero() {
        String jobDescription = "We need a Java developer with Spring Boot and SQL experience.";
        String resumeText = "Skilled painter and landscape photographer with a passion for travel.";

        MatchingService.MatchResult result = matchingService.match(resumeText, jobDescription);

        assertEquals(0.0, result.getScore());
        assertTrue(result.getMissingKeywords().contains("java"));
    }

    @Test
    void emptyJobDescriptionReturnsZeroScore() {
        MatchingService.MatchResult result = matchingService.match("Some resume text", "");
        assertEquals(0.0, result.getScore());
    }
}
