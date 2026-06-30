package com.plataforma.conversacional.dto.response;

public record DocumentStatusResponse(
    Long documentId,
    String status,
    Integer chunksCount,
    String errorMessage
) {}
