package com.plataforma.conversacional.storage;

import com.plataforma.conversacional.entity.DocumentChunk;
import com.plataforma.conversacional.retrieval.ScoredChunk;
import com.plataforma.conversacional.util.CosineSimilarity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
public class MockVectorStore implements VectorStore {

    private final Map<Long, float[]> store = new ConcurrentHashMap<>();
    private final Map<Long, DocumentChunk> chunks = new ConcurrentHashMap<>();

    @Override
    public void storeChunk(DocumentChunk chunk, float[] embedding) {
        chunks.put(chunk.getId(), chunk);
        store.put(chunk.getId(), embedding);
    }

    @Override
    public void storeChunks(List<DocumentChunk> chunkList, List<float[]> embeddings) {
        for (int i = 0; i < chunkList.size(); i++) {
            DocumentChunk chunk = chunkList.get(i);
            chunks.put(chunk.getId(), chunk);
            store.put(chunk.getId(), embeddings.get(i));
        }
    }

    @Override
    public List<ScoredChunk> searchSimilar(float[] queryEmbedding, int topK, double minScore) {
        List<ScoredChunk> scored = new ArrayList<>();
        for (Map.Entry<Long, float[]> entry : store.entrySet()) {
            double score = CosineSimilarity.calculate(queryEmbedding, entry.getValue());
            if (score >= minScore) {
                DocumentChunk chunk = chunks.get(entry.getKey());
                if (chunk != null) {
                    scored.add(new ScoredChunk(chunk, score));
                }
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredChunk::getScore).reversed());
        return scored.stream().limit(topK).collect(Collectors.toList());
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        List<Long> toRemove = chunks.entrySet().stream()
                .filter(e -> e.getValue().getDocument().getId().equals(documentId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        toRemove.forEach(id -> {
            chunks.remove(id);
            store.remove(id);
        });
    }
}
