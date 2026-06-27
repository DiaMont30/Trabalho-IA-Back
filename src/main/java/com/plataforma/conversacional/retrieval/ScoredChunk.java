package com.plataforma.conversacional.retrieval;

import com.plataforma.conversacional.entity.DocumentChunk;

public class ScoredChunk {

    private final DocumentChunk chunk;
    private final double score;

    public ScoredChunk(DocumentChunk chunk, double score) {
        this.chunk = chunk;
        this.score = score;
    }

    public DocumentChunk getChunk() { return chunk; }
    public double getScore() { return score; }
}
