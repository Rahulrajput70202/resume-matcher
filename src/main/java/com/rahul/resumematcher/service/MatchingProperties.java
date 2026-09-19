package com.rahul.resumematcher.service;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.matching")
public class MatchingProperties {

    private double keywordWeight = 0.50;
    private double semanticWeight = 0.50;
    private int semanticTopK = 5;

    @PostConstruct
    public void validate() {
        if (keywordWeight < 0.0 || semanticWeight < 0.0
                || keywordWeight > 1.0 || semanticWeight > 1.0
                || Math.abs((keywordWeight + semanticWeight) - 1.0) > 0.000001) {
            throw new IllegalStateException(
                    "app.matching.keyword-weight and app.matching.semantic-weight must be non-negative and sum to 1.0");
        }
        if (semanticTopK <= 0) {
            throw new IllegalStateException("app.matching.semantic-top-k must be greater than zero");
        }
    }

    public double getKeywordWeight() {
        return keywordWeight;
    }

    public void setKeywordWeight(double keywordWeight) {
        this.keywordWeight = keywordWeight;
    }

    public double getSemanticWeight() {
        return semanticWeight;
    }

    public void setSemanticWeight(double semanticWeight) {
        this.semanticWeight = semanticWeight;
    }

    public int getSemanticTopK() {
        return semanticTopK;
    }

    public void setSemanticTopK(int semanticTopK) {
        this.semanticTopK = semanticTopK;
    }
}