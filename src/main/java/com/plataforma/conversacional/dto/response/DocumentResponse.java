package com.plataforma.conversacional.dto.response;

import com.plataforma.conversacional.enums.DocumentType;

public record DocumentResponse(
    Long id,
    String fileName,
    String originalName,
    DocumentType type,
    Long size,
    String storagePath,
    Long sessionId,
    String uploadedAt
) {}