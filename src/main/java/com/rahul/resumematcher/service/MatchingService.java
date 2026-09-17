package com.rahul.resumematcher.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Rule-based resume <-> job description matcher.
 *
 * Combines:
 
 *  1) A curated technical-skill dictionary (matched as whole phrases/words), which catches
 *     multi-word terms like "spring boot" or "rest api" that naive tokenization would break apart.
 *  2) Frequency-based generic keyword extraction from the job description, to catch role-specific
 *     terms that aren't in the curated dictionary.
 *
 * This runs entirely locally (no external API calls, no cost, no network dependency), which makes
 * the endpoint usable out of the box. AiMatchingService can optionally layer an LLM-based review
 * on top of this when an API key is configured.
 */
@Service
public class MatchingService {

    private static final Set<String> STOPWORDS = Set.of(
        "the", "and", "a", "an", "to", "of", "in", "for", "on", "with", "as", "is", "are",
        "be", "will", "we", "you", "your", "our", "this", "that", "at", "by", "or", "from",
        "have", "has", "including", "etc", "such", "into", "than", "then",
        "who", "what", "which", "their", "they", "it", "its", "these", "those", "role",
        "job", "team", "work", "working", "experience", "years", "year", "strong", "good",
        "ability", "skills", "knowledge", "must", "should", "can", "using", "use", "used"
);
    private static final List<String> SKILL_DICTIONARY = List.of(
            "java", "spring boot", "spring framework", "spring security", "spring data jpa",
            "hibernate", "jpa", "rest api", "restful", "microservices", "sql", "mysql",
            "postgresql", "mongodb", "nosql", "docker", "kubernetes", "aws", "azure", "gcp",
            "git", "github", "maven", "gradle", "junit", "mockito", "jenkins", "ci/cd",
            "agile", "scrum", "oop", "object-oriented", "multithreading", "collections",
            "design patterns", "system design", "data structures", "algorithms", "kafka",
            "rabbitmq", "redis", "html", "css", "javascript", "typescript", "react", "angular",
            "node.js", "python", "linux", "unix", "shell scripting", "api gateway", "jwt",
            "oauth", "swagger", "openapi", "unit testing", "integration testing", "tdd",
            "postman", "jira", "elasticsearch", "graphql", "load balancing", "caching"
    );

    public MatchResult match(String resumeText, String jobDescription) {
        String resumeLower = resumeText.toLowerCase(Locale.ROOT);
        String jobLower = jobDescription.toLowerCase(Locale.ROOT);

        // 1) Curated dictionary matching (phrase-aware)
        Set<String> requiredSkills = new LinkedHashSet<>();
        for (String skill : SKILL_DICTIONARY) {
            if (containsPhrase(jobLower, skill)) {
                requiredSkills.add(skill);
            }
        }

        // 2) Frequency-based generic keywords from the job description, excluding ones already covered
        List<String> genericKeywords = extractTopKeywords(jobLower, 20).stream()
                .filter(k -> requiredSkills.stream().noneMatch(s -> s.contains(k)))
                .collect(Collectors.toList());
        requiredSkills.addAll(genericKeywords);

        Set<String> matched = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();

        for (String skill : requiredSkills) {
            if (containsPhrase(resumeLower, skill)) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        double score = requiredSkills.isEmpty()
                ? 0.0
                : Math.round((matched.size() * 1000.0) / requiredSkills.size()) / 10.0;

        return new MatchResult(score, new ArrayList<>(matched), new ArrayList<>(missing));
    }

    private boolean containsPhrase(String text, String phrase) {
        String escaped = Pattern.quote(phrase);
        Pattern pattern = Pattern.compile("(?<![a-zA-Z0-9])" + escaped + "(?![a-zA-Z0-9])");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    private List<String> extractTopKeywords(String text, int limit) {
        String[] words = text.split("[^a-zA-Z0-9+#.]+");
        Map<String, Integer> freq = new HashMap<>();

        for (String word : words) {
            String w = word.trim();
            if (w.length() < 4 || STOPWORDS.contains(w)) {
                continue;
            }
            freq.merge(w, 1, Integer::sum);
        }

        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static class MatchResult {
        private final double score;
        private final List<String> matchedKeywords;
        private final List<String> missingKeywords;

        public MatchResult(double score, List<String> matchedKeywords, List<String> missingKeywords) {
            this.score = score;
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
        }

        public double getScore() {
            return score;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public List<String> getMissingKeywords() {
            return missingKeywords;
        }
    }
}
