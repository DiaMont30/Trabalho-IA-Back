package com.plataforma.conversacional.dto.response;

import com.plataforma.conversacional.enums.DocumentType;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String fileName,
    String originalName,
    DocumentType type,
    Long size,
    String storagePath,
    UUID sessionId,
    String uploadedAt
) {}
