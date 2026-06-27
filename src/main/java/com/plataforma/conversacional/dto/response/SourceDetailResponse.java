package com.plataforma.conversacional.dto.response;

public record SourceDetailResponse(
    Long documentId,
    String documentName,
    String excerpt,
    double relevanceScore
) {}
