package com.rahul.resumematcher.repository;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class DocumentChunkRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final DocumentChunkRepository repository = new DocumentChunkRepository(jdbcTemplate);

    @Test
    void scopedSearchFiltersByResumeIdEvenWhenFilenamesAreTheSame() {
        List<Object[]> capturedArguments = new ArrayList<>();
        doAnswer(invocation -> {
            capturedArguments.add(new Object[]{
                    invocation.getArgument(1),
                    invocation.getArgument(2),
                    invocation.getArgument(3),
                    invocation.getArgument(4)
            });
            return List.of();
        }).when(jdbcTemplate).queryForList(
            anyString(), any(Object.class), any(Object.class), any(Object.class), any(Object.class));

        repository.findSimilar(new float[]{0.1f}, "resume-a", 5);
        repository.findSimilar(new float[]{0.1f}, "resume-b", 5);

        assertEquals("resume-a", capturedArguments.get(0)[1]);
        assertEquals("resume-b", capturedArguments.get(1)[1]);
    }

    @Test
    void scopedSearchContainsResumePredicateAndPreservesDocumentName() {
        doAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            assertTrue(sql.contains("WHERE resume_id = ?"));
            assertTrue(sql.contains("document_name"));
            return List.of(Map.of("distance", 0.2));
        }).when(jdbcTemplate).queryForList(
            anyString(), any(Object.class), any(Object.class), any(Object.class), any(Object.class));

        repository.findSimilar(new float[]{0.1f}, "resume-a", 5);
    }
}