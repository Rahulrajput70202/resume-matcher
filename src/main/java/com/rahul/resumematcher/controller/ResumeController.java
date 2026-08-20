package com.rahul.resumematcher.controller;

import com.rahul.resumematcher.dto.AnalysisResponse;
import com.rahul.resumematcher.entity.ResumeAnalysis;
import com.rahul.resumematcher.entity.User;
import com.rahul.resumematcher.repository.ResumeAnalysisRepository;
import com.rahul.resumematcher.repository.UserRepository;
import com.rahul.resumematcher.service.AiMatchingService;
import com.rahul.resumematcher.service.MatchingService;
import com.rahul.resumematcher.service.PdfParserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final PdfParserService pdfParserService;
    private final MatchingService matchingService;
    private final AiMatchingService aiMatchingService;
    private final ResumeAnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    public ResumeController(PdfParserService pdfParserService, MatchingService matchingService,
                             AiMatchingService aiMatchingService, ResumeAnalysisRepository analysisRepository,
                             UserRepository userRepository) {
        this.pdfParserService = pdfParserService;
        this.matchingService = matchingService;
        this.aiMatchingService = aiMatchingService;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<?> analyze(@RequestParam("resume") MultipartFile resumeFile,
                                      @RequestParam("jobTitle") String jobTitle,
                                      @RequestParam("jobDescription") String jobDescription,
                                      Authentication authentication) {
        User user = currentUser(authentication);

        String resumeText;
        try {
            resumeText = pdfParserService.extractText(resumeFile);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Could not read PDF: " + ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }

        MatchingService.MatchResult result = matchingService.match(resumeText, jobDescription);

        String aiSuggestions = aiMatchingService.getSuggestions(resumeText, jobDescription, result.getMissingKeywords());

        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setUser(user);
        analysis.setFileName(resumeFile.getOriginalFilename());
        analysis.setJobTitle(jobTitle);
        analysis.setMatchScore(result.getScore());
        analysis.setMatchedKeywords(result.getMatchedKeywords());
        analysis.setMissingKeywords(result.getMissingKeywords());
        analysis.setAiSuggestions(aiSuggestions);

        ResumeAnalysis saved = analysisRepository.save(analysis);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResponse>> history(Authentication authentication) {
        User user = currentUser(authentication);
        List<AnalysisResponse> history = analysisRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, Authentication authentication) {
        User user = currentUser(authentication);
        return analysisRepository.findByIdAndUser(id, user)
                .map(a -> ResponseEntity.ok(toResponse(a)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        User user = currentUser(authentication);
        return analysisRepository.findByIdAndUser(id, user)
                .map(a -> {
                    analysisRepository.delete(a);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private User currentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    private AnalysisResponse toResponse(ResumeAnalysis a) {
        return new AnalysisResponse(
                a.getId(), a.getFileName(), a.getJobTitle(), a.getMatchScore(),
                a.getMatchedKeywords(), a.getMissingKeywords(), a.getAiSuggestions(), a.getCreatedAt()
        );
    }
}
