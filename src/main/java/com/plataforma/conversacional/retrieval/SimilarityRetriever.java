package com.plataforma.conversacional.retrieval;

import com.plataforma.conversacional.embedding.EmbeddingStrategy;
import com.plataforma.conversacional.storage.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SimilarityRetriever implements Retriever {

    private final EmbeddingStrategy embeddingStrategy;
    private final VectorStore vectorStore;
    private final double minScore;

    public SimilarityRetriever(
            EmbeddingStrategy embeddingStrategy,
            VectorStore vectorStore,
            @Value("${app.rag.retrieval.min-score}") double minScore) {
        this.embeddingStrategy = embeddingStrategy;
        this.vectorStore = vectorStore;
        this.minScore = minScore;
    }

    @Override
    public List<ScoredChunk> retrieve(String query, int topK) {
        float[] queryEmbedding = embeddingStrategy.embed(query);

        List<ScoredChunk> results = vectorStore.searchSimilar(queryEmbedding, topK, minScore);

        results.sort(Comparator.comparingDouble(ScoredChunk::getScore).reversed());

        return results;
    }
}
