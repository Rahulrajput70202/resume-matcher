package com.rahul.resumematcher.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisResponse {

    private Long id;
    private String fileName;
    private String jobTitle;
    private double matchScore;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private String aiSuggestions;
    private LocalDateTime createdAt;

    public AnalysisResponse() {
    }

    public AnalysisResponse(Long id, String fileName, String jobTitle, double matchScore,
                             List<String> matchedKeywords, List<String> missingKeywords,
                             String aiSuggestions, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.jobTitle = jobTitle;
        this.matchScore = matchScore;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
        this.aiSuggestions = aiSuggestions;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public String getAiSuggestions() {
        return aiSuggestions;
    }

    public void setAiSuggestions(String aiSuggestions) {
        this.aiSuggestions = aiSuggestions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
