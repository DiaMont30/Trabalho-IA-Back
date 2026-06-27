package com.plataforma.conversacional.storage;

import com.plataforma.conversacional.entity.DocumentChunk;
import com.plataforma.conversacional.retrieval.ScoredChunk;
import java.util.List;

public interface VectorStore {

    void storeChunk(DocumentChunk chunk, float[] embedding);
    void storeChunks(List<DocumentChunk> chunks, List<float[]> embeddings);
    List<ScoredChunk> searchSimilar(float[] queryEmbedding, int topK, double minScore);
    void deleteByDocumentId(Long documentId);
}
