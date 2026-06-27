package com.plataforma.conversacional.dto.response;

public record IngestionStatusResponse(
    Long jobId,
    Long documentId,
    String status,
    Integer chunksCount,
    String errorMessage
) {}
