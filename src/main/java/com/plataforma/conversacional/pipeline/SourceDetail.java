package com.plataforma.conversacional.pipeline;

public record SourceDetail(
    Long chunkId,
    Long documentId,
    String documentName,
    String excerpt,
    double relevanceScore
) {}
