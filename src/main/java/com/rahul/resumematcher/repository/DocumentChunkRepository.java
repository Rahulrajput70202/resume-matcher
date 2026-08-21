package com.rahul.resumematcher.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DocumentChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save a document chunk and its embedding
    public void save(String documentName, String chunkText, float[] embedding) {

        String sql = """
                INSERT INTO document_chunks
                (document_name, chunk_text, embedding)
                VALUES (?, ?, ?::vector)
                """;

        String vector = toVectorString(embedding);

        jdbcTemplate.update(
                sql,
                documentName,
                chunkText,
                vector
        );
    }

    // Find the most similar document chunks
    public List<Map<String, Object>> findSimilar(
            float[] queryEmbedding,
            int limit) {

        String sql = """
                SELECT
                    id,
                    document_name,
                    chunk_text,
                    embedding <=> ?::vector AS distance
                FROM document_chunks
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        String vector = toVectorString(queryEmbedding);

        return jdbcTemplate.queryForList(
                sql,
                vector,
                vector,
                limit
        );
    }

    // Convert float[] to pgvector format: [0.1,0.2,0.3,...]
    private String toVectorString(float[] embedding) {

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                sb.append(",");
            }

            sb.append(embedding[i]);
        }

        sb.append("]");

        return sb.toString();
    }
}