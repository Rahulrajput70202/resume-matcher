package com.rahul.resumematcher.service;

import com.rahul.resumematcher.repository.DocumentChunkRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;
    private final TextChunkingService textChunkingService;

    public EmbeddingService(
            EmbeddingModel embeddingModel,
            DocumentChunkRepository documentChunkRepository,
            TextChunkingService textChunkingService) {

        this.embeddingModel = embeddingModel;
        this.documentChunkRepository = documentChunkRepository;
        this.textChunkingService = textChunkingService;
    }

    public float[] createEmbedding(String text) {
        return embeddingModel.embed(text);
    }

    public void createAndStoreEmbedding(
            String documentName,
            String text) {

        float[] embedding = createEmbedding(text);

        documentChunkRepository.save(
                documentName,
                text,
                embedding
        );
    }

    public void createAndStoreEmbedding(
            String resumeId,
            String documentName,
            String text) {

        float[] embedding = createEmbedding(text);

        documentChunkRepository.save(
                resumeId,
                documentName,
                text,
                embedding
        );
    }

    public List<Map<String, Object>> searchSimilar(
            String text,
            int limit) {

        float[] queryEmbedding = createEmbedding(text);

        return documentChunkRepository.findSimilar(
                queryEmbedding,
                limit
        );
    }

    public List<Map<String, Object>> searchSimilar(
            String text,
            String resumeId,
            int limit) {

        float[] queryEmbedding = createEmbedding(text);

        return documentChunkRepository.findSimilar(
                queryEmbedding,
                resumeId,
                limit
        );
    }

    public int processAndStoreDocument(
            String documentName,
            String text) {

        List<String> chunks =
                textChunkingService.splitIntoChunks(text);

        for (String chunk : chunks) {
            createAndStoreEmbedding(
                    documentName,
                    chunk
            );
        }

        return chunks.size();
    }

    public int processAndStoreDocument(
            String resumeId,
            String documentName,
            String text) {

        List<String> chunks =
                textChunkingService.splitIntoChunks(text);

        for (String chunk : chunks) {
            createAndStoreEmbedding(
                    resumeId,
                    documentName,
                    chunk
            );
        }

        return chunks.size();
    }
}